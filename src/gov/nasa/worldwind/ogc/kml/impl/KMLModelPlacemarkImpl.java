/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.collada.*;
import gov.nasa.worldwind.ogc.collada.impl.ColladaTraversalContext;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Class to load and render a COLLADA model as the geometry of a KML Placemark.
 *
 * @author pabercrombie
 * @version $Id: KMLModelPlacemarkImpl.java 1838 2014-02-05 20:48:12Z dcollins $
 */
public class KMLModelPlacemarkImpl extends WWObjectImpl implements KMLRenderable, ColladaResourceResolver
{
    /** Model rendered by this class. */
    protected KMLModel model;
    /** Placemark that contains the model. */
    protected KMLPlacemark parent;
    /** Reference to the COLLADA root that contains the parsed COLLADA file. */
    protected AtomicReference<ColladaRoot> colladaRoot = new AtomicReference<ColladaRoot>();
    /**
     * Time, in milliseconds since the Epoch, at which this placemark's model resource was last retrieved. Initially
     * <code>-1</code>, indicating that the resource has not been retrieved.
     */
    protected AtomicLong resourceRetrievalTime = new AtomicLong(-1);

    /**
     * Map specified by the KML Model's ResourceMap element. The map relates relative references within the COLLADA file
     * to paths relative to the KML document.
     */
    protected Map<String, String> resourceMap;

    /** Traversal context for rendering the ColladaRoot. */
    protected ColladaTraversalContext colladaTraversalContext = new ColladaTraversalContext();

    /**
     * Create an instance.
     *
     * @param tc        the current {@link KMLTraversalContext}.
     * @param placemark the <i>Placemark</i> element containing the <i>Point</i>.
     * @param geom      the {@link gov.nasa.worldwind.ogc.kml.KMLPoint} geometry.
     *
     * @throws NullPointerException     if the geometry is null.
     * @throws IllegalArgumentException if the parent placemark or the traversal context is null.
     */
    public KMLModelPlacemarkImpl(KMLTraversalContext tc, KMLPlacemark placemark, KMLAbstractGeometry geom)
    {
        if (tc == null)
        {
            String msg = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (placemark == null)
        {
            String msg = Logging.getMessage("nullValue.ParentIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (geom == null)
        {
            String msg = Logging.getMessage("nullValue.GeometryIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.model = (KMLModel) geom;
        this.parent = placemark;

        this.resourceMap = this.createResourceMap(this.model);
    }

    /**
     * Build the resource map from the KML Model's <i>ResourceMap</i> element.
     *
     * @param model Model from which to create the resource map.
     *
     * @return Map that relates relative paths in the COLLADA document to paths relative to the KML document.
     */
    protected Map<String, String> createResourceMap(KMLModel model)
    {
        Map<String, String> map = new HashMap<String, String>();

        KMLResourceMap resourceMap = model.getResourceMap();
        if (resourceMap == null)
            return Collections.emptyMap();

        for (KMLAlias alias : resourceMap.getAliases())
        {
            if (alias != null && !WWUtil.isEmpty(alias.getSourceRef()) && !WWUtil.isEmpty(alias.getTargetHref()))
            {
                map.put(alias.getSourceRef(), alias.getTargetHref());
            }
        }

        return map.size() > 0 ? map : Collections.<String, String>emptyMap();
    }

    /**
     * Specifies the Collada resource referenced by this placemark, or <code>null</code> if this placemark has no
     * resource.
     *
     * @param root the Collada resource referenced by this placemark. May be <code>null</code>.
     */
    protected void setColladaRoot(ColladaRoot root)
    {
        if (root != null)
            this.configureColladaRoot(root);

        this.colladaRoot.set(root);
    }

    /**
     * Indicates the Collada resource referenced by this placemark. This returns <code>null</code> if this placemark has
     * no resource.
     *
     * @return this placemark's Collada resource, or <code>null</code> to indicate that this placemark has no resource.
     *
     * @see #setColladaRoot(gov.nasa.worldwind.ogc.collada.ColladaRoot)
     */
    protected ColladaRoot getColladaRoot()
    {
        return this.colladaRoot.get();
    }

    /**
     * Apply the model's position, orientation, and scale to a COLLADA root.
     *
     * @param root COLLADA root to configure.
     */
    protected void configureColladaRoot(ColladaRoot root)
    {
        root.setResourceResolver(this);

        Position refPosition = this.model.getLocation().getPosition();
        root.setPosition(refPosition);
        root.setAltitudeMode(KMLUtil.convertAltitudeMode(this.model.getAltitudeMode(), WorldWind.CLAMP_TO_GROUND)); // KML default

        KMLOrientation orientation = this.model.getOrientation();
        if (orientation != null)
        {
            Double d = orientation.getHeading();
            if (d != null)
                root.setHeading(Angle.fromDegrees(d));

            d = orientation.getTilt();
            if (d != null)
                root.setPitch(Angle.fromDegrees(-d));

            d = orientation.getRoll();
            if (d != null)
                root.setRoll(Angle.fromDegrees(-d));
        }

        KMLScale scale = this.model.getScale();
        if (scale != null)
        {
            Double x = scale.getX();
            Double y = scale.getY();
            Double z = scale.getZ();

            Vec4 modelScale = new Vec4(
                x != null ? x : 1.0,
                y != null ? y : 1.0,
                z != null ? z : 1.0);

            root.setModelScale(modelScale);
        }
    }

    /** {@inheritDoc} */
    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.mustRetrieveResource())
            this.requestResource(dc);

        ColladaRoot root = this.getColladaRoot();
        if (root != null)
        {
            this.colladaTraversalContext.initialize();
            root.preRender(this.colladaTraversalContext, dc);
        }
    }

    /** {@inheritDoc} */
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        ColladaRoot root = this.getColladaRoot();
        if (root != null)
        {
            this.colladaTraversalContext.initialize();
            root.render(this.colladaTraversalContext, dc);
        }
    }

    /**
     * Resolve COLLADA references relative to the COLLADA document. If the reference is relative then it will resolved
     * relative to the .dae file, not the kml file. If the COLLADA document may be contained in a KMZ archive the
     * resources will be resolved relative to the .dae file within the archive. Normally references in a KMZ are
     * resolved relative to the root of the archive, but Model references are an exception. See
     * https://developers.google.com/kml/documentation/kmzarchives and https://developers.google.com/kml/documentation/kmlreference#model
     * <p>
     * {@inheritDoc}.
     */
    public String resolveFilePath(String path) throws IOException
    {
        KMLLink link = this.model.getLink();

        // Check the resource map to see if an alias is defined for this resource.
        String alias = this.resourceMap.get(path);
        if (alias != null)
            path = alias;

        // If the path is relative then resolve it relative to the COLLADA file.
        File f = new File(path);
        if (!f.isAbsolute() && link != null && link.getHref() != null)
        {
            try
            {
                URI base = new URI(null, link.getHref(), null);
                URI ref = new URI(null, path, null);

                path = base.resolve(ref).getPath();
            }
            catch (URISyntaxException ignored)
            {
                // Ignored
            }
        }

        Object o = this.parent.getRoot().resolveReference(path);
        if (o instanceof URL || o instanceof String)
            return o.toString();

        return null;
    }

    /**
     * Returns whether this placemark must retrieve its model resource. This always returns <code>false</code> if this
     * placemark has no <code>KMLLink</code>.
     *
     * @return <code>true</code> if this placemark must retrieve its model resource, otherwise <code>false</code>.
     */
    protected boolean mustRetrieveResource()
    {
        KMLLink link = this.model.getLink();
        if (link == null)
            return false;

        // The resource must be retrieved if the link has been updated since the resource was
        // last retrieved, or if the resource has never been retrieved.
        return this.getColladaRoot() == null || link.getUpdateTime() > this.resourceRetrievalTime.get();
    }

    /**
     * Thread's off a task to determine whether the resource is local or remote and then retrieves it either from disk
     * cache or a remote server.
     *
     * @param dc the current draw context.
     */
    protected void requestResource(DrawContext dc)
    {
        if (WorldWind.getTaskService().isFull())
            return;

        KMLLink link = this.model.getLink();
        if (link == null)
            return;

        String address = link.getAddress(dc);
        if (address != null)
            address = address.trim();

        if (WWUtil.isEmpty(address))
            return;

        WorldWind.getTaskService().addTask(new RequestTask(this, address));
    }

    /**
     * Initiates a retrieval of the model referenced by this placemark.Once the resource is retrieved and loaded, this
     * calls <code>{@link #setColladaRoot(ColladaRoot)}</code> to specify this link's new network resource, and sends an
     * <code>{@link gov.nasa.worldwind.avlist.AVKey#RETRIEVAL_STATE_SUCCESSFUL}</code> property change event to this
     * link's property change listeners.<p>
     * This does nothing if this <code>KMLNetworkLink</code> has no <code>KMLLink</code>.
     *
     * @param address the address of the resource to retrieve
     * @throws java.io.IOException if a reading error occurs.
     * @throws javax.xml.stream.XMLStreamException if a parsing error occurs.
     */
    protected void retrieveModel(String address) throws IOException, XMLStreamException
    {
        Object o = this.parent.getRoot().resolveReference(address);
        if (o == null)
            return;

        ColladaRoot root = ColladaRoot.createAndParse(o);
        if (root == null)
            return;

        this.setColladaRoot(root);
        this.resourceRetrievalTime.set(System.currentTimeMillis());
        this.parent.getRoot().requestRedraw();
    }

    /** Attempts to find this model link resource file locally, and if that fails attempts to find it remotely. */
    protected static class RequestTask implements Runnable
    {
        /** The link associated with this request. */
        protected final KMLModelPlacemarkImpl placemark;
        /** The resource's address. */
        protected final String address;

        /**
         * Construct a request task for a specified network link resource.
         *
         * @param placemark the placemark for which to construct the request task.
         * @param address   the address of the resource to request.
         */
        protected RequestTask(KMLModelPlacemarkImpl placemark, String address)
        {
            if (placemark == null)
            {
                String message = Logging.getMessage("nullValue.ObjectIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (address == null)
            {
                String message = Logging.getMessage("nullValue.PathIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.placemark = placemark;
            this.address = address;
        }

        public void run()
        {
            if (Thread.currentThread().isInterrupted())
                return; // the task was cancelled because it's a duplicate or for some other reason

            try
            {
                this.placemark.retrieveModel(this.address);
            }
            catch (IOException e)
            {
                String message = Logging.getMessage("generic.ExceptionWhileReading", e.getMessage());
                Logging.logger().warning(message);
            }
            catch (XMLStreamException e)
            {
                String message = Logging.getMessage("generic.ExceptionAttemptingToParseXml", e.getMessage());
                Logging.logger().warning(message);
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            RequestTask that = (RequestTask) o;

            if (!this.address.equals(that.address))
                return false;
            //noinspection RedundantIfStatement
            if (!this.placemark.equals(that.placemark))
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = placemark.hashCode();
            result = 31 * result + address.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return this.address;
        }
    }
}