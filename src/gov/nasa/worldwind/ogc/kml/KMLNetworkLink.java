/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.beans.*;
import java.util.concurrent.atomic.*;

/**
 * Represents the KML <i>NetworkLink</i> element and provides access to its contents.
 * <p>
 * During rendering, <code>KMLNetworkLink</code> retrieves and loads its network resource whenever necessary. Upon a
 * successful retrieval, <code>KMLNetworkLink</code> sends an <code>AVKey.RETRIEVAL_STATE_SUCCESSFUL</code> property
 * change event to this link's property change listeners. Once retrieved and loaded, <code>KMLNetworkLink</code> stores
 * its network resource by calling <code>setNetworkResource</code>, draws its network resource during preRendering and
 * rendering, and forwards property change events from the network resource to its property change listeners.
 * <p>
 * During retrieval, <code>KMLNetworkLink</code> attempts to use either the <code>Link</code> or the <code>Url</code>.
 * The <code>Link</code> is the preferred method for encoding a KML NetworkLink's address since KML version 2.1,
 * therefore we give it priority over <code>Url</code>.
 *
 * @author tag
 * @version $Id: KMLNetworkLink.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLNetworkLink extends KMLAbstractContainer implements PropertyChangeListener
{
    /** Indicates the network resource referenced by this <code>KMLNetworkLink</code>. Initially <code>null</code>. */
    protected AtomicReference<KMLRoot> networkResource = new AtomicReference<KMLRoot>();
    /**
     * Time, in milliseconds since the Epoch, at which this <code>KMLNetworkLink's</code> network resource was last
     * retrieved. Initially <code>-1</code>, indicating that the network resource has not been retrieved.
     */
    protected AtomicLong networkResourceRetrievalTime = new AtomicLong(-1);

    protected AtomicLong firstRetrievalTime;

    /** Flag to indicate that the Link has been fetched from the hash map. */
    protected boolean linkFetched = false;
    protected KMLLink link;

    /**
     * Flag that is raised if the link retrieves a files that is not a KML document. In this case, do not attempt to
     * retrieve the resource again until the link changes. If the link changes then target file may also have changed.
     */
    protected boolean invalidTarget;

    /**
     * Cache the root of this network link. Accessing the root by climbing up a deep network link tree can be a
     * performance bottleneck.
     */
    protected KMLRoot root;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLNetworkLink(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Forward property change events from the inner {@link KMLRoot} to this object's parent KMLRoot.
     *
     * @param evt Event to forward.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        this.getRoot().firePropertyChange(evt);
    }

    @Override
    public void onMessage(Message msg)
    {
        KMLLink link = this.getLinkOrUrl();
        if (link != null)
        {
            link.onMessage(msg);
        }

        KMLRoot networkResource = this.getNetworkResource();
        if (networkResource != null)
        {
            networkResource.onMessage(msg);
        }
    }

    /** {@inheritDoc} Overridden to cache the root instead of climbing the parse tree each time. */
    @Override
    public KMLRoot getRoot()
    {
        if (root == null)
            this.root = super.getRoot();

        return this.root;
    }

    public Boolean getRefreshVisibility()
    {
        return (Boolean) this.getField("refreshVisibility");
    }

    public Boolean getFlyToView()
    {
        return (Boolean) this.getField("flyToView");
    }

    public KMLLink getNetworkLink()
    {
        if (!this.linkFetched)
        {
            this.linkFetched = true;
            this.link = (KMLLink) this.getField("Link");
        }

        return this.link;
    }

    public KMLLink getUrl()
    {
        return (KMLLink) this.getField("Url");
    }

    /**
     * Returns this <code>NetworkLink's</code> <code>Link</code> element or its <code>Url</code> element, depending on
     * which is available. <code>Link</code> is the preferred method for encoding a <code>NetworkLink</code> since KML
     * version 2.1, therefore we give it priority. If a <code>Link</code> is not specified, the deprecated
     * <code>Url</code> element is returned.
     *
     * @return this <code>NetworkLink's</code> <code>Link</code> element, if one is specified. Otherwise, this
     *         <code>NetworkLink's</code> <code>Url</code> element. Returns <code>null</code> if neither
     *         <code>Link</code> or a <code>Url</code> are specified.
     */
    protected KMLLink getLinkOrUrl()
    {
        KMLLink link = this.getNetworkLink();
        if (link != null)
            return link;

        return this.getUrl();
    }

    /**
     * Indicates the network resource referenced by this <code>KMLNetworkLink</code>. This returns <code>null</code> if
     * this link has no resource.
     *
     * @return this link's network resource, or <code>null</code> to indicate that this link has no resource.
     *
     * @see #setNetworkResource(KMLRoot)
     */
    public KMLRoot getNetworkResource()
    {
        return networkResource.get();
    }

    /**
     * {@inheritDoc} Overridden to apply region culling according to the same rules as in KMLAbstractFeature. This
     * prevents retrieving network links in inactive regions.
     */
    @Override
    protected boolean isFeatureActive(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getVisibility() != null && !this.getVisibility())
            return false;

        KMLRegion region = this.getRegion();
        if (region == null)
            region = tc.peekRegion();

        return region == null || region.isActive(tc, dc);
    }

    protected boolean hasNetworkLinkControl()
    {
        return this.getRoot().getNetworkLinkControl() != null;
    }

    @Override
    public String getName()
    {
        if (this.hasNetworkLinkControl() && !WWUtil.isEmpty(this.getRoot().getNetworkLinkControl().getLinkName()))
            return this.getRoot().getNetworkLinkControl().getLinkName();

        return super.getName();
    }

    @Override
    public String getDescription()
    {
        if (this.hasNetworkLinkControl()
            && !WWUtil.isEmpty(this.getRoot().getNetworkLinkControl().getLinkDescription()))
            return this.getRoot().getNetworkLinkControl().getLinkDescription();

        return super.getDescription();
    }

    @Override
    public Object getSnippet()
    {
        if (this.hasNetworkLinkControl() && !WWUtil.isEmpty(this.getRoot().getNetworkLinkControl().getLinkSnippet()))
            return this.getRoot().getNetworkLinkControl().getLinkSnippet();

        return super.getSnippet();
    }

    /**
     * Specifies the network resource referenced by this <code>KMLNetworkLink</code>, or <code>null</code> if this link
     * has no resource. If the specified <code>kmlRoot</code> is not <code>null</code>, this link draws the
     * <code>kmlRoot</code> during preRendering and rendering, and forwards property change events from the
     * <code>kmlRoot</code> to this link's property change listeners.
     *
     * @param kmlRoot the network resource referenced by this <code>KMLNetworkLink</code>. May be <code>null</code>.
     *
     * @see #getNetworkResource()
     */
    public void setNetworkResource(final KMLRoot kmlRoot)
    {
        // Remove any property change listeners previously set on the KMLRoot. This eliminates dangling references from
        // the KMLNetworkLink to its previous KMLRoot.
        KMLRoot resource = this.getNetworkResource();
        if (resource != null)
            resource.removePropertyChangeListener(this);

        this.networkResource.set(kmlRoot);
        this.networkResourceRetrievalTime.set(System.currentTimeMillis());
        if (this.firstRetrievalTime == null)
            this.firstRetrievalTime = new AtomicLong(this.networkResourceRetrievalTime.get());

        // Set up to listen for property change events on the KMLRoot. KMLNetworkLink must forward REPAINT and REFRESH
        // property change events from its internal KMLRoot to its parent KMLRoot to support BrowserBalloon repaint
        // events and recursive KMLNetworkLink elements.
        if (kmlRoot != null)
        {
            kmlRoot.addPropertyChangeListener(this);

            // Apply any updates contained in the new root's optional network link control.
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (kmlRoot.getNetworkLinkControl() != null
                        && kmlRoot.getNetworkLinkControl().getUpdate() != null
                        && !kmlRoot.getNetworkLinkControl().getUpdate().isUpdatesApplied())
                        kmlRoot.getNetworkLinkControl().getUpdate().applyOperations();
                }
            });
        }
    }

    /**
     * Pre-renders the network resource referenced by this <code>KMLNetworkLink</code>. If this link must retrieve its
     * network resource, this initiates a retrieval and does nothing until the resource is retrieved and loaded. Once
     * the network resource is retrieved and loaded, this calls <code>{@link #setNetworkResource(KMLRoot)}</code> to
     * specify this link's new network resource, and sends an <code>{@link gov.nasa.worldwind.avlist.AVKey#RETRIEVAL_STATE_SUCCESSFUL}</code>
     * property change event to this link's property change listeners.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     *
     * @see #getNetworkResource()
     */
    @Override
    protected void doPreRender(KMLTraversalContext tc, DrawContext dc)
    {
        super.doPreRender(tc, dc);

        if (this.mustRetrieveNetworkResource())
            this.requestResource(dc);

        if (this.getNetworkResource() != null)
            this.getNetworkResource().preRender(tc, dc);
    }

    /**
     * Renders the network resource referenced by this <code>KMLNetworkLink</code>. This does nothing if this link has
     * no network resource.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @Override
    protected void doRender(KMLTraversalContext tc, DrawContext dc)
    {
        super.doRender(tc, dc);

        if (this.getNetworkResource() != null)
            this.getNetworkResource().render(tc, dc);
    }

    /**
     * Returns whether this <code>KMLNetworkLink</code> must retrieve its network resource. This always returns
     * <code>false</code> if this <code>KMLNetworkLink</code> has no <code>KMLLink</code>.
     *
     * @return <code>true</code> if this <code>KMLNetworkLink</code> must retrieve its network resource, otherwise
     *         <code>false</code>.
     */
    protected boolean mustRetrieveNetworkResource()
    {
        KMLLink link = this.getLinkOrUrl();
        if (link == null)
            return false; // If both the Link and the Url are null, then there's nothing to retrieve.

        // If the resource has already been retrieved, but is not a KML file, don't retrieve the resource again.
        if (this.invalidTarget)
            return false;

        // Make sure a refresh doesn't occur within the minimum refresh period, if one is specified.
        KMLNetworkLinkControl linkControl = this.getRoot().getNetworkLinkControl();
        if (linkControl != null && linkControl.getMinRefreshPeriod() != null)
        {
            long now = System.currentTimeMillis();
            if (this.firstRetrievalTime != null // be sure it gets retrieved a first time
                && this.networkResourceRetrievalTime.get() + linkControl.getMinRefreshPeriod() * 1000 > now)
                return false;
        }

        // Make sure a refresh doesn't occur after the max session length is reached, if one is specified.
        if (linkControl != null && linkControl.getMaxSessionLength() != null && this.firstRetrievalTime != null)
        {
            long now = System.currentTimeMillis();
            if (this.firstRetrievalTime.get() + linkControl.getMaxSessionLength() * 1000 > now)
                return false;
        }

        // The resource must be retrieved if the link has been updated since the resource was
        // last retrieved, or if the resource has never been retrieved.
        return this.getNetworkResource() == null || link.getUpdateTime() > this.networkResourceRetrievalTime.get();
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

        KMLLink link = this.getLinkOrUrl();
        if (link == null)
            return; // If both the Link and the Url are null, then there's nothing to retrieve.

        String address = link.getAddress(dc);
        if (address != null)
            address = address.trim();

        if (WWUtil.isEmpty(address))
            return;

        if (this.hasNetworkLinkControl() && this.getRoot().getNetworkLinkControl().getCookie() != null)
            address = address + this.getRoot().getNetworkLinkControl().getCookie();

        WorldWind.getTaskService().addTask(new RequestTask(this, address));
    }

    /**
     * Initiates a retrieval of the network resource referenced by this <code>KMLNetworkLink</code>. Once the network
     * resource is retrieved and loaded, this calls <code>{@link #setNetworkResource(KMLRoot)}</code> to specify this
     * link's new network resource, and sends an <code>{@link gov.nasa.worldwind.avlist.AVKey#RETRIEVAL_STATE_SUCCESSFUL}</code>
     * property change event to this link's property change listeners.
     * <p>
     * This does nothing if this <code>KMLNetworkLink</code> has no <code>KMLLink</code>.
     *
     * @param address the address of the resource to retrieve
     */
    protected void retrieveNetworkResource(String address)
    {
        // Treat the address as either a path to a local document, or as an absolute URL to a remote document. If the
        // address references a remote document, this attempts to retrieve it and loads the document once retrieval
        // succeeds. This does not handle absolute local file paths; absolute local file paths are not supported by the
        // KML specification. However, a NetworkLink may reference an absolute local file by specifying an absolute
        // URL with the "file:" protocol.
        //
        // Don't cache remote documents if this link's refreshMode is onInterval, or if this link's viewRefreshMode is
        // onStop. These documents are transient and therefore should be stored in a temporary location.

        long updateTime = 0L;

        KMLLink link = this.getLinkOrUrl();
        if (link != null)
        {
            updateTime = link.getUpdateTime();
        }

        Object o = this.getRoot().resolveNetworkLink(address, this.isLinkCacheable(), updateTime);
        if (o instanceof KMLRoot)
        {
            KMLRoot newRoot = (KMLRoot) o;
            this.setNetworkResource(newRoot);

            // Check for an expiration time set through HTTP header or NetworkLinkControl
            long expiration = this.computeExpiryRefreshTime(newRoot, address);
            this.getLinkOrUrl().setExpirationTime(expiration);

            this.getRoot().firePropertyChange(AVKey.RETRIEVAL_STATE_SUCCESSFUL, null, KMLNetworkLink.this);
        }
        // Anything other than a KMLRoot is not a valid link target
        else if (o != null)
        {
            String message = Logging.getMessage("KML.InvalidNetworkLinkTarget", address);
            Logging.logger().warning(message);
            this.invalidTarget = true; // Stop trying to retrieve this resource
        }
    }

    /**
     * Indicates the expiration time of a linked resource. The expiration time is specified by (in order of priority): a
     * NetworkLinkControl/expires element in the target document, a HTTP Cache-Control header, or an HTTP Expires
     * header.
     *
     * @param root    Root of target resource.
     * @param address Address of linked resource.
     *
     * @return The expiration time of the resource, in milliseconds since the Epoch. Zero indicates that there is no
     *         expiration time.
     */
    protected long computeExpiryRefreshTime(KMLRoot root, String address)
    {
        KMLNetworkLinkControl linkControl = root.getNetworkLinkControl();
        if (linkControl != null && linkControl.getExpires() != null)
        {
            Long time = WWUtil.parseTimeString(linkControl.getExpires());
            return time != null ? time : 0;
        }

        // Check for expiration in HTTP headers
        return this.getRoot().getExpiration(address);
    }

    /**
     * Indicates whether the network resource references by this <code>KMLNetworkLink</code> should be retrieved to the
     * WorldWind cache or to a temporary location. This returns <code>true</code> if all of the following conditions
     * are met, and <code>false</code> otherwise:
     * <ul> <li>This network link has either a <code>Link</code> or a <code>Url</code> element.</li> <li>The Link or Url
     * element's <code>refreshMode</code> is not <code>onInterval</code> or <code>onExpire</code>.</li> <li>The Link or
     * Url element's <code>viewRefreshMode</code> is not <code>onStop</code>.</li> </ul>
     *
     * @return <code>true</code> if this link's network resource can should be stored in a cache, or <code>false</code>
     *         if it should be stored in a temporary location.
     */
    public boolean isLinkCacheable()
    {
        KMLLink link = this.getLinkOrUrl();
        return link != null
            && !KMLConstants.ON_INTERVAL.equalsIgnoreCase(link.getRefreshMode())
            && !KMLConstants.ON_EXPIRE.equalsIgnoreCase(link.getRefreshMode())
            && !KMLConstants.ON_STOP.equalsIgnoreCase(link.getViewRefreshMode());
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLNetworkLink))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        KMLNetworkLink sourceLink = (KMLNetworkLink) sourceValues;

        // Reset this network link only if the change contains a new link
        if (sourceLink.getLinkOrUrl() != null)
            this.reset();

        super.applyChange(sourceValues);
    }

    @Override
    public void onChange(Message msg)
    {
        if (KMLAbstractObject.MSG_LINK_CHANGED.equals(msg.getName()))
            this.reset();

        super.onChange(msg);
    }

    protected void reset()
    {
        this.networkResource.set(null);
        this.networkResourceRetrievalTime.set(-1);
        this.firstRetrievalTime = null;
        this.linkFetched = false;
        this.link = null;
        this.invalidTarget = false;

        this.getRoot().requestRedraw(); // cause doPreRender to be called to initiate new link retrieval
    }

    /** Attempts to find this network link resource file locally, and if that fails attempts to find it remotely. */
    protected static class RequestTask implements Runnable
    {
        /** The link associated with this request. */
        protected final KMLNetworkLink link;
        /** The resource's address. */
        protected final String address;

        /**
         * Construct a request task for a specified network link resource.
         *
         * @param link    the network link for which to construct the request task.
         * @param address the address of the resource to request.
         */
        protected RequestTask(KMLNetworkLink link, String address)
        {
            if (link == null)
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

            this.link = link;
            this.address = address;
        }

        public void run()
        {
            if (Thread.currentThread().isInterrupted())
                return; // the task was cancelled because it's a duplicate or for some other reason

            this.link.retrieveNetworkResource(this.address);
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
            if (!this.link.equals(that.link))
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = link.hashCode();
            result = 31 * result + address.hashCode();
            return result;
        }

        public String toString()
        {
            return this.address;
        }
    }
}
