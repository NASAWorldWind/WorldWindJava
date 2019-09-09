/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.exception.WWTimeoutException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.kml.KMLViewController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Controller to display a {@link Balloon} and handle balloon events. The controller does the following: <ul>
 * <li>Display a balloon when an object is selected</li> <li>Handle URL selection events in balloons</li> <li>Resize
 * BrowserBalloons</li> <li>Handle close, back, and forward events in BrowserBalloon</li> </ul>
 * <h2>Displaying a balloon for a selected object</h2>
 * <p>
 * When a object is clicked, the controller looks for a Balloon attached to the object. The controller includes special
 * logic for handling balloons attached to KML features.
 * <h3>KML Features</h3>
 * <p>
 * The KMLAbstractFeature is attached to the top PickedObject under AVKey.CONTEXT. The controller looks for the balloon
 * in the KMLAbstractFeature under key AVKey.BALLOON.
 * <h3>Other objects</h3>
 * <p>
 * If the top object is an instance of AVList, the controller looks for a Balloon under AVKey.BALLOON.
 * <h2>URL events</h2>
 * <p>
 * The controller looks for a value under AVKey.URL attached to either the top PickedObject. If the URL refers to a KML
 * or KMZ document, the document is loaded into a new layer. If the link includes a reference to a KML feature,
 * controller will animate the view to that feature and/or open the feature balloon.
 * <p>
 * If the link should open in a new window (determined by an AVKey.TARGET of "_blank"), the controller will launch the
 * system web browser and navigate to the link. Otherwise it will allow the BrowserBalloon to navigate to the link.
 * <p>
 * Consuming a SelectEvent in the BalloonController will prevent the balloon from taking action on that event. For
 * example, a BrowserBalloon will navigate in place when a link is clicked, but it will not if the balloon controller
 * consumes the left press and left click select events. This allows the balloon controller to override the default
 * action for certain URLs.
 * <h2>BrowserBalloon control events</h2>
 * <p>
 * {@link gov.nasa.worldwind.render.AbstractBrowserBalloon} identifies its controls by attaching a value to the
 * PickedObject's AVList under AVKey.ACTION. The controller reads this value and performs the appropriate action. The
 * possible actions are AVKey.RESIZE, AVKey.BACK, AVKey.FORWARD, and AVKey.CLOSE.
 *
 * @author pabercrombie
 * @version $Id: BalloonController.java 1531 2013-08-04 16:19:13Z pabercrombie $
 */
public class BalloonController extends MouseAdapter implements SelectListener
{
    /* Default vertical offset, in pixels, between the balloon and the point that the leader shape points to. */
    public static final int DEFAULT_BALLOON_OFFSET = 60;

    public static final String FLY_TO = "flyto";
    public static final String BALLOON = "balloon";
    public static final String BALLOON_FLY_TO = "balloonFlyto";

    protected WorldWindow wwd;

    protected Object lastSelectedObject;
    protected Balloon balloon;

    /** Vertical offset, in pixels, between the balloon and the point that the leader shape points to. */
    protected int balloonOffset = DEFAULT_BALLOON_OFFSET;

    /**
     * Timeout to use when requesting remote documents. If the document does not load within this many milliseconds the
     * controller will stop trying and report an error.
     */
    protected long retrievalTimeout = 30 * 1000; // 30 seconds
    /** Interval between periodic checks for completion of asynchronous document retrieval (in milliseconds). */
    protected long retrievalPollInterval = 1000; // 1 second

    /**
     * A resize controller is created when the mouse enters a resize control on the balloon. The controller is destroyed
     * when the mouse exits the resize control.
     */
    protected BalloonResizeController resizeController;

    /**
     * Create a new balloon controller.
     *
     * @param wwd WorldWindow to attach to.
     */
    public BalloonController(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.wwd = wwd;
        this.wwd.addSelectListener(this);
        this.wwd.getInputHandler().addMouseListener(this);
        this.wwd.getInputHandler().addMouseMotionListener(this);
    }

    /**
     * Indicates the vertical distance, in pixels, between the balloon and the point that the leader points to.
     *
     * @return Vertical offset, in pixels.
     */
    public int getBalloonOffset()
    {
        return this.balloonOffset;
    }

    /**
     * Sets the vertical distance, in pixels, between the balloon and the point that the leader points to.
     *
     * @param balloonOffset Vertical offset, in pixels.
     */
    public void setBalloonOffset(int balloonOffset)
    {
        this.balloonOffset = balloonOffset;
    }

    //********************************************************************//
    //*********************** Event handling *****************************//
    //********************************************************************//

    /**
     * Handle a mouse click. If the top picked object has a balloon attached to it the balloon will be made visible. A
     * balloon may be attached to a KML feature, or to any picked object though {@link AVKey#BALLOON}.
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e == null || e.isConsumed())
            return;

        // Implementation note: handle the balloon with a mouse listener instead of a select listener so that the balloon
        // can be turned off if the user clicks on the terrain.
        try
        {
            if (this.isBalloonTrigger(e))
            {
                PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
                if (pickedObjects == null || pickedObjects.getTopPickedObject() == null)
                {
                    this.hideBalloon();
                    return;
                }

                Object topObject = pickedObjects.getTopObject();
                PickedObject topPickedObject = pickedObjects.getTopPickedObject();

                boolean sameObjectSelected = this.lastSelectedObject == topObject || this.balloon == topObject;
                boolean balloonVisible = this.balloon != null && this.balloon.isVisible();

                // Do nothing if the same thing is selected and the balloon is already visible.
                if (sameObjectSelected && balloonVisible)
                    return;

                // Hide the active balloon if the selection has changed, or if terrain was selected.
                if (this.balloon != null && !(topObject instanceof Balloon))
                {
                    this.hideBalloon(); // Something else selected
                }

                Balloon balloon = this.getBalloon(topPickedObject);

                // Don't change balloons that are already visible
                if (balloon != null && !balloon.isVisible())
                {
                    this.lastSelectedObject = topObject;
                    this.showBalloon(balloon, topObject, e.getPoint());
                }
            }
        }
        catch (Exception ex)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Logging.logger().warning(ex.getMessage() != null ? ex.getMessage() : ex.toString());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        if (e == null || e.isConsumed())
            return;

        PickedObjectList list = this.wwd.getObjectsAtCurrentPosition();
        PickedObject pickedObject = list != null ? list.getTopPickedObject() : null;

        // Handle balloon resize events. Create a resize controller when the mouse enters the resize area.
        // While the mouse is in the resize area, the resize controller will handle select events to resize the
        // balloon. The controller will be destroyed when the mouse exists the resize area.
        if (pickedObject != null && this.isResizeControl(pickedObject))
        {
            this.createResizeController((Balloon) pickedObject.getObject());
        }
        else if (this.resizeController != null && !this.resizeController.isResizing())
        {
            // Destroy the resize controller if the mouse is out of the resize area and the controller
            // is not resizing the balloon. The mouse is allowed to move out of the resize area during the resize
            // operation. If this event is a drag end, check the top object at the current position to determine if
            // the cursor is still over the resize area.

            this.destroyResizeController(null);
        }
    }

    public void selected(SelectEvent event)
    {
        if (event == null || event.isConsumed()
            || (event.getMouseEvent() != null && event.getMouseEvent().isConsumed()))
        {
            return;
        }

        try
        {
            PickedObject pickedObject = event.getTopPickedObject();
            if (pickedObject == null)
                return;
            Object topObject = event.getTopObject();

            // Destroy the resize controller the event is a drag end and the mouse is out of the resize area, and the
            // controller is not resizing the balloon. The mouse is allowed to move out of the resize area during the
            // resize operation.
            if (event.isDragEnd() && this.resizeController != null && !this.resizeController.isResizing())
            {
                PickedObject po;
                PickedObjectList list = this.wwd.getObjectsAtCurrentPosition();
                po = list != null ? list.getTopPickedObject() : null;

                if (!this.isResizeControl(po))
                {
                    this.destroyResizeController(event);
                }
            }

            // Check to see if the event is a link activation or other balloon event
            if (event.isLeftClick())
            {
                String url = this.getUrl(pickedObject);
                if (url != null)
                {
                    this.onLinkActivated(event, url);
                }
                else if (pickedObject.hasKey(AVKey.ACTION) && topObject instanceof AbstractBrowserBalloon)
                {
                    this.onBalloonAction((AbstractBrowserBalloon) topObject, pickedObject.getStringValue(AVKey.ACTION));
                }
            }
            else if (event.isLeftDoubleClick())
            {
                // Call onLinkActivated for left double click even though we don't want to follow links when these
                // events occur. onLinkActivated determines if the URL is something that the controller should handle,
                // and consume the event if so. onLinkActivated does not perform the associated link action unless the
                // event is a left click. If we don't consume the event, the balloon may take action when a left press
                // event occurs on a link that the balloon controller will handle (for example, a link to a KML file.)
                // We avoid consuming left press events, since doing so prevents the WorldWindow from gaining focus.
                String url = this.getUrl(pickedObject);
                if (url != null)
                {
                    this.onLinkActivated(event, url);
                }
            }
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Logging.logger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected boolean isResizeControl(PickedObject po)
    {
        return po != null
            && AVKey.RESIZE.equals(po.getStringValue(AVKey.ACTION))
            && po.getObject() instanceof Balloon;
    }

    /**
     * Get the URL attached to a PickedObject. This method looks for a URL attached to the PickedObject under {@link
     * AVKey#URL}.
     *
     * @param pickedObject PickedObject to inspect. May not be null.
     *
     * @return The URL attached to the PickedObject, or null if there is no URL.
     */
    protected String getUrl(PickedObject pickedObject)
    {
        return pickedObject.getStringValue(AVKey.URL);
    }

    /**
     * Get the KML feature that is the context of a picked object. The context is associated with either the
     * PickedObject or the user object under the key {@link AVKey#CONTEXT}.
     *
     * @param pickedObject PickedObject to inspect for context. May not be null.
     *
     * @return The KML feature associated with the picked object, or null if no KML feature is found.
     */
    protected KMLAbstractFeature getContext(PickedObject pickedObject)
    {
        Object topObject = pickedObject.getObject();

        Object context = pickedObject.getValue(AVKey.CONTEXT);

        // If there was no context in the PickedObject, look for it in the top user object.
        if (context == null && topObject instanceof AVList)
        {
            context = ((AVList) topObject).getValue(AVKey.CONTEXT);
        }

        if (context instanceof KMLAbstractFeature)
            return (KMLAbstractFeature) context;
        else
            return null;
    }

    /**
     * Called when a {@link gov.nasa.worldwind.render.AbstractBrowserBalloon} control is activated (Close, Back, or
     * Forward).
     *
     * @param browserBalloon Balloon involved in action.
     * @param action         Identifier for the action that occurred.
     */
    protected void onBalloonAction(AbstractBrowserBalloon browserBalloon, String action)
    {
        if (AVKey.CLOSE.equals(action))
        {
            // If the balloon closing is the balloon we manage, call hideBalloon to clean up state.
            // Otherwise just make the balloon invisible.
            if (browserBalloon == this.balloon)
                this.hideBalloon();
            else
                browserBalloon.setVisible(false);
        }
        else if (AVKey.BACK.equals(action))
            browserBalloon.goBack();

        else if (AVKey.FORWARD.equals(action))
            browserBalloon.goForward();
    }

    //********************************************************************//
    //***********************  Resize events *****************************//
    //********************************************************************//

    /**
     * Create a resize controller and attach it to the WorldWindow. Has no effect if there is already an active resize
     * controller.
     *
     * @param balloon Balloon to resize.
     */
    protected void createResizeController(Balloon balloon)
    {
        // If a resize controller is already active, don't start another one.
        if (this.resizeController != null)
            return;

        this.resizeController = new BalloonResizeController(this.wwd, balloon);
    }

    /**
     * Destroy the active resize controller.
     *
     * @param event Event that triggered the controller to be destroyed.
     */
    protected void destroyResizeController(SelectEvent event)
    {
        if (this.resizeController != null)
        {
            try
            {
                // Pass the last event to the controller so that it can clean up internal state if it needs to.
                if (event != null)
                    this.resizeController.selected(event);

                this.resizeController.detach();
                this.resizeController = null;
            }
            finally
            {
                // Reset the cursor to default. The resize controller may have changed it.
                if (this.wwd instanceof Component)
                {
                    ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
                }
            }
        }
    }

    //**********************************************************************//
    //***********************  Hyperlink events  ***************************//
    //**********************************************************************//

    /**
     * Called when a URL in a balloon is activated. This method handles links to KML documents, features in KML
     * documents, and links that target a new browser window.
     * <p>
     * The possible cases are:
     * <p>
     * <b>KML/KMZ document</b> - Load the document in a new layer.<br> <b>Feature in KML/KMZ document</b> - Load the
     * document, navigate to the feature and/or open feature balloon.<br> <b>Feature in currently open KML/KMZ
     * document</b> - Navigate to the feature and/or open feature balloon. <br> <b>HTML document, target current
     * window</b> - No action, let the BrowserBalloon navigate to the URL. <br> <b>HTML document, target new window</b>
     * - Launch the system web browser and navigate to the URL.
     * <p>
     * If the URL matches one of the cases defined above, the SelectEvent will be marked as consumed. Marking the event
     * as consumed prevents BrowserBalloon from handling the event. However, the controller will only take action on the
     * event if the event is a link activation trigger.
     * <p>
     * For example, if a left click event (a link activation event) occurs on a link to a KML document, the event will
     * be marked as consumed and the document will be opened. If a left press event (not a link activation event) occurs
     * with the same URL, the event will be consumed but the document will not be opened (if the press is followed by a
     * click, the click will cause the document to be opened). Consuming the left press prevents the balloon from
     * processing the event.
     *
     * @param event SelectEvent for the URL activation. If the event is a link activation trigger the controller will
     *              take action on the event (by opening a KML document, etc). If the event is not a link activation
     *              trigger, but the URL is a URL that the balloon controller would normally handle, the event is
     *              consumed to prevent the balloon itself from trying to handle the event, but no further action is
     *              taken.
     * @param url   URL that was activated.
     *
     * @see #isLinkActivationTrigger(gov.nasa.worldwind.event.SelectEvent)
     */
    protected void onLinkActivated(SelectEvent event, String url)
    {
        PickedObject pickedObject = event.getTopPickedObject();
        String type = pickedObject.getStringValue(AVKey.MIME_TYPE);

        // Break URL into base and reference
        String linkBase;
        String linkRef;

        int hashSign = url.indexOf("#");
        if (hashSign != -1)
        {
            linkBase = url.substring(0, hashSign);
            linkRef = url.substring(hashSign);
        }
        else
        {
            linkBase = url;
            linkRef = null;
        }

        KMLRoot targetDoc; // The document to load and/or fly to
        KMLRoot contextDoc = null; // The local KML document that initiated the link
        KMLAbstractFeature kmlFeature;

        boolean isKmlUrl = this.isKmlUrl(linkBase, type);
        boolean foundLocalFeature = false;

        // Look for a KML feature attached to the picked object. If present, the link will be interpreted relative
        // to this feature.
        kmlFeature = this.getContext(pickedObject);
        if (kmlFeature != null)
            contextDoc = kmlFeature.getRoot();

        // If this link is to a KML or KMZ document we will load the document into a new layer.
        if (isKmlUrl)
        {
            targetDoc = this.findOpenKmlDocument(linkBase);
            if (targetDoc == null)
            {
                // Asynchronously request the document if the event is a link activation trigger.
                if (this.isLinkActivationTrigger(event))
                    this.requestDocument(linkBase, contextDoc, linkRef);

                // We are opening a document, consume the event to prevent balloon from trying to load the document.
                event.consume();
                return;
            }
        }
        else
        {
            // URL does not refer to a remote KML document, assume that it refers to a feature in the current doc
            targetDoc = contextDoc;
        }

        // If the link also has a feature reference, we will move to the feature
        if (linkRef != null)
        {
            if (this.onFeatureLinkActivated(targetDoc, linkRef, event))
            {
                foundLocalFeature = true;
                event.consume(); // Consume event if the target feature was found
            }
        }

        // If the link is not to a KML file or feature, and the link targets a new browser window, launch the system web
        // browser. BrowserBalloon ignores link events that target new windows, so we need to handle them here.
        if (!isKmlUrl && !foundLocalFeature)
        {
            String target = pickedObject.getStringValue(AVKey.TARGET);
            if ("_blank".equalsIgnoreCase(target))
            {
                // Invoke the system browser to open the link if the event is link activation trigger.
                if (this.isLinkActivationTrigger(event))
                    this.openInNewBrowser(event, url);
                event.consume();
            }
        }
    }

    /**
     * Determines if a SelectEvent is an event that activates a hyperlink.
     *
     * @param event Event to test. May not be null.
     *
     * @return {@code true} if the event actives hyperlinks. This implementation returns {@code true} for left click
     *         events.
     */
    protected boolean isLinkActivationTrigger(SelectEvent event)
    {
        return event.isLeftClick();
    }

    /**
     * Open a URL in a new web browser. Launch the system web browser and navigate to the URL.
     *
     * @param event SelectEvent that triggered navigation. The event is consumed if URL can be parsed.
     * @param url   URL to open.
     */
    protected void openInNewBrowser(SelectEvent event, String url)
    {
        try
        {
            BrowserOpener.browse(new URL(url));
            event.consume();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToInvokeWebBrower", url);
            Logging.logger().warning(message);
        }
    }

    /**
     * Called when a link to a KML feature is activated.
     *
     * @param doc          Document to search for the feature.
     * @param linkFragment Reference to the feature. The fragment may contain a display directive. For example
     *                     "#myPlacemark", or "#myPlacemark;balloon".
     * @param event        The select event that activated the link. This event will be consumed if a KML feature is
     *                     found that matches the link fragment. However, the controller only moves to the feature or
     *                     opens a balloon if the event is a link activation event, or null. Other events are consumed
     *                     to prevent the balloon from handling events for a link that the controller wants handle. This
     *                     parameter may be null.
     *
     * @return True if a feature matching the reference was found and some action was taken.
     */
    protected boolean onFeatureLinkActivated(KMLRoot doc, String linkFragment, SelectEvent event)
    {
        // Split the reference into the feature id and the display directive (flyto, balloon, etc)
        String[] parts = linkFragment.split(";");
        String featureId = parts[0];
        String directive = parts.length > 1 ? parts[1] : FLY_TO;

        if (!WWUtil.isEmpty(featureId) && doc != null)
        {
            Object o = doc.resolveReference(featureId);
            if (o instanceof KMLAbstractFeature)
            {
                // Perform the link action if the event is a link activation event.
                if (event == null || this.isLinkActivationTrigger(event))
                    this.doFeatureLinkActivated((KMLAbstractFeature) o, directive);
                return true;
            }
        }
        return false;
    }

    /**
     * Handle activation of a KML feature link. Depending on the display directive, this method will either move the
     * view to the feature, open the balloon for the feature, or both. See the KML specification for details on links to
     * features in the KML description balloon.
     *
     * @param feature   Feature to navigate to.
     * @param directive Display directive, one of {@link #FLY_TO}, {@link #BALLOON}, or {@link #BALLOON_FLY_TO}.
     */
    protected void doFeatureLinkActivated(KMLAbstractFeature feature, String directive)
    {
        if (FLY_TO.equals(directive) || BALLOON_FLY_TO.equals(directive))
        {
            this.moveToFeature(feature);
        }

        if (BALLOON.equals(directive) || BALLOON_FLY_TO.equals(directive))
        {
            this.showBalloon(feature);
        }
    }

    /**
     * Does a URL refer to a KML or KMZ document?
     *
     * @param url         URL to test.
     * @param contentType Mime type of the URL content. May be null.
     *
     * @return Return true if the URL refers to a file with a ".kml" or ".kmz" extension, or if the {@code contentType}
     *         is the KML or KMZ mime type.
     */
    protected boolean isKmlUrl(String url, String contentType)
    {
        if (WWUtil.isEmpty(url))
            return false;

        String suffix = WWIO.getSuffix(url);

        return "kml".equalsIgnoreCase(suffix)
            || "kmz".equalsIgnoreCase(suffix)
            || KMLConstants.KML_MIME_TYPE.equals(contentType)
            || KMLConstants.KMZ_MIME_TYPE.equals(contentType);
    }

    /**
     * Move the view to look at a KML feature. The view will be adjusted to look at the bounding sector that contains
     * all of the feature's points.
     *
     * @param feature Feature to look at.
     */
    protected void moveToFeature(KMLAbstractFeature feature)
    {
        KMLViewController viewController = KMLViewController.create(this.wwd);
        viewController.goTo(feature);
    }

    //**********************************************************************//
    //**********************  Show/Hide Balloon  ***************************//
    //**********************************************************************//

    /**
     * Show a balloon for a KML feature. The balloon will be positioned over the feature on the globe. If the feature
     * does not have a balloon, a balloon may be created. {@link #canShowBalloon(gov.nasa.worldwind.ogc.kml.KMLAbstractFeature)
     * canShowBalloon} determines if a balloon will be created.
     *
     * @param feature KML feature for which to show a balloon.
     */
    public void showBalloon(KMLAbstractFeature feature)
    {
        Balloon balloon = feature.getBalloon();

        // Create a new balloon if the feature does not have one
        if (balloon == null && this.canShowBalloon(feature))
            balloon = this.createBalloon(feature);

        // Don't change balloons that are already visible
        if (balloon != null && !balloon.isVisible())
        {
            this.lastSelectedObject = feature;

            Position pos = this.getBalloonPosition(feature);
            if (pos != null)
            {
                this.hideBalloon(); // Hide previously displayed balloon, if any
                this.showBalloon(balloon, pos);
            }
            else
            {
                // The feature may be attached to the screen, not the globe
                Point point = this.getBalloonPoint(feature);
                if (point != null)
                {
                    this.hideBalloon(); // Hide previously displayed balloon, if any
                    this.showBalloon(balloon, null, point);
                }
                // If the feature is not attached to a particular point, just put it in the middle of the viewport
                else
                {
                    Rectangle viewport = this.wwd.getView().getViewport();

                    Point center = new Point((int) viewport.getCenterX(), (int) viewport.getCenterY());

                    this.hideBalloon();
                    this.showBalloon(balloon, null, center);
                }
            }
        }
    }

    /**
     * Determines whether or not a balloon must be created for a KML feature. A balloon is created for any feature with
     * a balloon style or a non-empty description. No balloon is created for a feature with no balloon style and no
     * description.
     *
     * @param feature KML feature to test.
     *
     * @return {@code true} if a balloon must be created for the feature. Otherwise {@code false}.
     */
    public boolean canShowBalloon(KMLAbstractFeature feature)
    {
        KMLBalloonStyle style = (KMLBalloonStyle) feature.getSubStyle(new KMLBalloonStyle(null), KMLConstants.NORMAL);

        boolean isBalloonHidden = "hide".equals(style.getDisplayMode());

        // Determine if the balloon style actually has fields.
        boolean hasBalloonStyle = style.hasStyleFields() && !style.hasField(AVKey.UNRESOLVED);

        // Do not create a balloon if there is no balloon style and the feature has no description.
        return (hasBalloonStyle || !WWUtil.isEmpty(feature.getDescription()) || feature.getExtendedData() != null)
            && !isBalloonHidden;
    }

    /**
     * Inspect a mouse event to see if it should make a balloon visible.
     *
     * @param e Event to inspect.
     *
     * @return {@code true} if the event is a balloon trigger. This implementation returns {@code true} if the event is
     *         a left click.
     */
    protected boolean isBalloonTrigger(MouseEvent e)
    {
        // Handle only left click
        return (e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() % 2 == 1);
    }

    /**
     * Get the balloon attached to a PickedObject. If the PickedObject represents a KML feature, then the balloon will
     * be retrieved from the feature.  Otherwise, the balloon will be retrieved from the user object's field
     * AVKey.BALLOON.
     * <p>
     * If a KML feature is picked, and the feature does not have a balloon, a new balloon may be created and attached to
     * the feature. {@link #canShowBalloon(gov.nasa.worldwind.ogc.kml.KMLAbstractFeature) canShowBalloon} determines if
     * a balloon will be created for the feature.
     *
     * @param pickedObject PickedObject to inspect. May not be null.
     *
     * @return The balloon attached to the picked object, or null if there is no balloon. Returns null if {@code
     *         pickedObject} is null.
     */
    protected Balloon getBalloon(PickedObject pickedObject)
    {
        Object topObject = pickedObject.getObject();
        Object balloonObj = null;

        // Look for a KMLAbstractFeature context. If the top picked object is part of a KML feature, the
        // feature will determine the balloon.
        if (pickedObject.hasKey(AVKey.CONTEXT))
        {
            Object contextObj = pickedObject.getValue(AVKey.CONTEXT);
            if (contextObj instanceof KMLAbstractFeature)
            {
                KMLAbstractFeature feature = (KMLAbstractFeature) contextObj;
                balloonObj = feature.getBalloon();

                // Create a new balloon if the feature does not have one
                if (balloonObj == null && this.canShowBalloon(feature))
                    balloonObj = this.createBalloon(feature);
            }
        }

        // If we didn't find a balloon on the KML feature, look for a balloon in the AVList
        if (balloonObj == null && topObject instanceof AVList)
        {
            AVList avList = (AVList) topObject;
            balloonObj = avList.getValue(AVKey.BALLOON);
        }

        if (balloonObj instanceof Balloon)
            return (Balloon) balloonObj;
        else
            return null;
    }

    /**
     * Create a balloon for a KML feature and attach the balloon to the feature. The type of balloon created depends on
     * the type of feature and the result of {@link #isUseBrowserBalloon()}. If the feature is attached to a point on
     * the globe, this method creates a {@link GlobeBalloon}. If the feature is attached to the screen, a {@link
     * ScreenBalloon} is created. If isUseBrowserBalloon() returns {@code true}, the balloon will be a descendant of
     * {@link AbstractBrowserBalloon}. Otherwise it will be a descendant of {@link AbstractAnnotationBalloon}.
     *
     * @param feature Feature to create balloon for.
     *
     * @return New balloon. May return null if the feature should not have a balloon.
     */
    protected Balloon createBalloon(KMLAbstractFeature feature)
    {
        KMLBalloonStyle balloonStyle = (KMLBalloonStyle) feature.getSubStyle(new KMLBalloonStyle(null),
            KMLConstants.NORMAL);

        String text = balloonStyle.getText();
        if (text == null)
            text = "";

        // Create the balloon based on the features attachment mode and the browser balloon settings. Wrap the balloon
        // in a KMLBalloonImpl to handle balloon style resolution.  
        KMLAbstractBalloon kmlBalloon;
        if (AVKey.GLOBE.equals(this.getAttachmentMode(feature)))
        {
            GlobeBalloon balloon;
            if (this.isUseBrowserBalloon())
                balloon = new GlobeBrowserBalloon(text, Position.ZERO); // 0 is dummy position
            else
                balloon = new GlobeAnnotationBalloon(text, Position.ZERO); // 0 is dummy position

            kmlBalloon = new KMLGlobeBalloonImpl(balloon, feature);
        }
        else
        {
            ScreenBalloon balloon;
            if (this.isUseBrowserBalloon())
                balloon = new ScreenBrowserBalloon(text, new Point(0, 0)); // 0,0 is dummy position
            else
                balloon = new ScreenAnnotationBalloon(text, new Point(0, 0)); // 0,0 is dummy position

            kmlBalloon = new KMLScreenBalloonImpl(balloon, feature);
        }

        kmlBalloon.setVisible(false);
        kmlBalloon.setAlwaysOnTop(true);

        // Attach the balloon to the feature
        feature.setBalloon(kmlBalloon);

        this.configureBalloon(kmlBalloon, feature);

        return kmlBalloon;
    }

    /**
     * Configure a new balloon for a KML feature.
     *
     * @param balloon Balloon to configure.
     * @param feature Feature that owns the Balloon.
     */
    protected void configureBalloon(Balloon balloon, KMLAbstractFeature feature)
    {
        // Configure the balloon for a container to not have a leader. These balloons will display in the middle of the
        // viewport.
        if (feature instanceof KMLAbstractContainer)
        {
            BalloonAttributes attrs = new BasicBalloonAttributes();

            // Size the balloon to match the size of the content.
            Size size = new Size(Size.NATIVE_DIMENSION, 0.0, null, Size.NATIVE_DIMENSION, 0.0, null);

            // Do not allow the balloon to be auto-sized larger than 80% of the viewport. The user may resize the balloon
            // larger than this size.
            Size maxSize = new Size(Size.EXPLICIT_DIMENSION, 0.8, AVKey.FRACTION,
                Size.EXPLICIT_DIMENSION, 0.8, AVKey.FRACTION);

            attrs.setSize(size);
            attrs.setMaximumSize(maxSize);
            attrs.setOffset(new Offset(0.5, 0.5, AVKey.FRACTION, AVKey.FRACTION));
            attrs.setLeaderShape(AVKey.SHAPE_NONE);
            balloon.setAttributes(attrs);
        }
        else
        {
            BalloonAttributes attrs = new BasicBalloonAttributes();

            // Size the balloon to match the size of the content.
            Size size = new Size(Size.NATIVE_DIMENSION, 0.0, null, Size.NATIVE_DIMENSION, 0.0, null);

            // Do not allow the balloon to be auto-sized larger than 50% of the viewport width, and 40% of the height.
            // The user may resize the balloon larger than this size.
            Size maxSize = new Size(Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION,
                Size.EXPLICIT_DIMENSION, 0.4, AVKey.FRACTION);

            attrs.setSize(size);
            attrs.setMaximumSize(maxSize);
            balloon.setAttributes(attrs);
        }
    }

    /**
     * Get the attachment mode of a KML feature: {@link AVKey#GLOBE} or {@link AVKey#SCREEN}. Some features, such as a
     * PointPlacemark, are attached to a point on the globe. Others, such as a ScreenImage, are attached to the screen.
     *
     * @param feature KML feature to test.
     *
     * @return {@link AVKey#GLOBE} if the feature is attached to a geographic location. Otherwise {@link AVKey#SCREEN}.
     *         Container features (Document and Folder) are considered screen features.
     */
    protected String getAttachmentMode(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLPlacemark || feature instanceof KMLGroundOverlay)
            return AVKey.GLOBE;
        else
            return AVKey.SCREEN;
    }

    /**
     * Indicates if the controller will create Balloons of type {@link AbstractBrowserBalloon}. BrowserBalloons are used
     * on platforms that support them (currently Windows and Mac). {@link AbstractAnnotationBalloon} is used on other
     * platforms.
     *
     * @return {@code true} if the controller will create BrowserBalloons.
     */
    protected boolean isUseBrowserBalloon()
    {
        return Configuration.isWindowsOS() || Configuration.isMacOS();
    }

    /**
     * Show a balloon at a screen point.
     *
     * @param balloon       Balloon to make visible.
     * @param balloonObject The picked object that owns the balloon. May be {@code null}.
     * @param point         Point where mouse was clicked.
     */
    protected void showBalloon(Balloon balloon, Object balloonObject, Point point)
    {
        // If the balloon is attached to the screen rather than the globe, move it to the
        // current point. Otherwise move it to the position under the current point.
        if (balloon instanceof ScreenBalloon)
            ((ScreenBalloon) balloon).setScreenLocation(point);
        else if (balloon instanceof GlobeBalloon)
        {
            Position position = this.getBalloonPosition(balloonObject, point);
            if (position != null)
            {
                GlobeBalloon globeBalloon = (GlobeBalloon) balloon;
                globeBalloon.setPosition(position);
                globeBalloon.setAltitudeMode(this.getBalloonAltitudeMode(balloonObject));
            }
        }

        if (this.mustAdjustPosition(balloon))
            this.adjustPosition(balloon, point);

        this.balloon = balloon;
        this.balloon.setVisible(true);
    }

    /**
     * Show a balloon at a globe position.
     *
     * @param balloon  Balloon to make visible.
     * @param position Position on the globe to locate the balloon. If the balloon is attached to the screen, it will be
     *                 position at the screen point currently over this position.
     */
    protected void showBalloon(Balloon balloon, Position position)
    {
        Vec4 screenVec4 = this.wwd.getView().project(
            this.wwd.getModel().getGlobe().computePointFromPosition(position));

        Point screenPoint = new Point((int) screenVec4.x,
            (int) (this.wwd.getView().getViewport().height - screenVec4.y));

        // If the balloon is attached to the screen rather than the globe, move it to the
        // current point. Otherwise move it to the position under the current point.
        if (balloon instanceof ScreenBalloon)
        {
            ((ScreenBalloon) balloon).setScreenLocation(screenPoint);
        }
        else
        {
            ((GlobeBalloon) balloon).setPosition(position);
        }

        if (this.mustAdjustPosition(balloon))
            this.adjustPosition(balloon, screenPoint);

        this.balloon = balloon;
        this.balloon.setVisible(true);
    }

    /**
     * Determines if a balloon position must be adjusted to make the balloon visible in the viewport.
     *
     * @param balloon Balloon to inspect.
     *
     * @return {@code true} if the balloon position must be adjusted to make the balloon visible.
     */
    protected boolean mustAdjustPosition(Balloon balloon)
    {
        // Look at the balloon leader shape. If there is no leader shape, assume that the balloon itself is positioned
        // over the point of interest, and cannot be moved. Otherwise, assume that the balloon must be adjusted.
        BalloonAttributes attrs = balloon.getAttributes();
        return !(AVKey.SHAPE_NONE.equals(attrs.getLeaderShape()));
    }

    /**
     * Adjust the position of a balloon so that the entire balloon is visible on screen.
     *
     * @param balloon     Balloon to adjust the position of.
     * @param screenPoint Screen point to which the balloon leader points.
     */
    protected void adjustPosition(Balloon balloon, Point screenPoint)
    {
        // Create an offset that will ensure that the balloon is visible. This method assumes that the balloon
        // width is less than half of the viewport width, and that the balloon height is less half of the viewport
        // height, the default maximum size applied to balloons created by the controller.

        Rectangle viewport = this.wwd.getView().getViewport();

        double x, y;
        String xUnits, yUnits;

        // If the balloon point is in the right 25% of the viewport, place the balloon to the left.
        xUnits = AVKey.FRACTION;
        if (screenPoint.x > viewport.width * 0.75)
        {
            x = 1.0;
        }
        // If the point is in the left 25% of the viewport, place the balloon to the right.
        else if (screenPoint.x < viewport.width * 0.25)
        {
            x = 0;
        }
        // Otherwise, center the balloon on the point.
        else
        {
            x = 0.5;
        }

        int vertOffset = this.getBalloonOffset();
        y = -vertOffset;

        // If the point is in the top half of the viewport, place the balloon below the point.
        if (screenPoint.y < viewport.height * 0.5)
        {
            yUnits = AVKey.INSET_PIXELS;
        }
        // Otherwise, place the balloon above the point.
        else
        {
            yUnits = AVKey.PIXELS;
        }

        Offset offset = new Offset(x, y, xUnits, yUnits);

        BalloonAttributes attributes = balloon.getAttributes();
        if (attributes == null)
        {
            attributes = new BasicBalloonAttributes();
            balloon.setAttributes(attributes);
        }
        attributes.setOffset(offset);

        BalloonAttributes highlightAttributes = balloon.getHighlightAttributes();
        if (highlightAttributes != null)
            highlightAttributes.setOffset(offset);
    }

    /** Hide the active balloon. Does nothing if there is no active balloon. */
    protected void hideBalloon()
    {
        if (this.balloon != null)
        {
            this.balloon.setVisible(false);
            this.balloon = null;
        }
        this.lastSelectedObject = null;
    }

    //**********************************************************************//
    //***********  Methods to determine where to put the balloon  **********//
    //**********************************************************************//

    /**
     * Get the position of the balloon for a KML feature attached to the globe. This method applies to KML features that
     * area attached to the globe, rather than to the screen (for example, this method applies to GroundOverlay, but not
     * to ScreenOverlay). This method determines the type of feature, and calls a more specific method to handle
     * features of that type.
     *
     * @param feature Feature to find balloon position for.
     *
     * @return Position at which to place the Placemark balloon.
     *
     * @see #getBalloonPositionForPlacemark(gov.nasa.worldwind.ogc.kml.KMLPlacemark)
     * @see #getBalloonPositionForGroundOverlay(gov.nasa.worldwind.ogc.kml.KMLGroundOverlay)
     * @see #getBalloonPoint(gov.nasa.worldwind.ogc.kml.KMLAbstractFeature)
     */
    protected Position getBalloonPosition(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLPlacemark)
        {
            return this.getBalloonPositionForPlacemark((KMLPlacemark) feature);
        }
        else if (feature instanceof KMLGroundOverlay)
        {
            return this.getBalloonPositionForGroundOverlay(((KMLGroundOverlay) feature));
        }
        return null;
    }

    /**
     * Get the position of the balloon for a picked object with an attached balloon. If the top object is an instance of
     * {@link Locatable}, this method returns the position of the Locatable. If the object is an instance of {@link
     * AbstractShape}, the method performs an intersection calculation between a ray through the pick point and the
     * shape. If neither of the previous conditions are true, or if the object is {@code null}, this method returns the
     * intersection position of a ray through the pick point and the globe.
     *
     * @param topObject Object that was picked. May be {@code null}.
     * @param pickPoint The point at which the mouse event occurred.
     *
     * @return Position at which to place the balloon, or {@code null} if a position cannot be determined.
     */
    protected Position getBalloonPosition(Object topObject, Point pickPoint)
    {
        Position position = null;

        if (topObject instanceof Locatable)
        {
            position = ((Locatable) topObject).getPosition();
        }
        else if (topObject instanceof AbstractShape)
        {
            position = this.computeIntersection((AbstractShape) topObject, pickPoint);
        }

        // Fall back to a terrain intersection if we still don't have a position.
        if (position == null)
        {
            Line ray = this.wwd.getView().computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
            Intersection[] inter = this.wwd.getSceneController().getDrawContext().getSurfaceGeometry().intersect(ray);
            if (inter != null && inter.length > 0)
            {
                position = this.wwd.getModel().getGlobe().computePositionFromPoint(inter[0].getIntersectionPoint());
            }

            // We still don't have a position, fall back to intersection with the ellipsoid.
            if (position == null)
            {
                position = this.wwd.getView().computePositionFromScreenPoint(pickPoint.x, pickPoint.y);
            }
        }

        return position;
    }

    /**
     * Get the appropriate altitude mode for a GlobeBalloon, depending on the object that has been selected. If the
     * balloon object is an instance of {@link PointPlacemark}, this implementation returns the altitude mode of the
     * placemark. Otherwise it returns {@link WorldWind#ABSOLUTE}.
     *
     * @param balloonObject The object that the balloon is attached to.
     *
     * @return The altitude mode that should be applied to the balloon, one of {@link WorldWind#ABSOLUTE}, {@link
     *         WorldWind#CLAMP_TO_GROUND}, or {@link WorldWind#RELATIVE_TO_GROUND}.
     */
    protected int getBalloonAltitudeMode(Object balloonObject)
    {
        // Balloons are often attached to PointPlacemarks, so handle this case specially. The balloon altitude mode
        // needs to match the placemark altitude mode. Shapes do not have this problem because an intersection calculation
        // can place the balloon.
        if (balloonObject instanceof PointPlacemark)
        {
            return ((PointPlacemark) balloonObject).getAltitudeMode();
        }
        return WorldWind.ABSOLUTE; // Default to absolute
    }

    /**
     * Compute the intersection of a line through a screen point and a shape.
     *
     * @param shape       Shape with which to compute intersection.
     * @param screenPoint Compute the intersection of a line through this screen point and the shape.
     *
     * @return The intersection position, or {@code null} if there is no intersection, or if the computation is
     *         interrupted.
     */
    protected Position computeIntersection(AbstractShape shape, Point screenPoint)
    {
        try
        {
            // Compute the intersection using whatever terrain is available. This calculation does not need to be very
            // precise, it just needs to place the balloon close to the shape.
            Terrain terrain = this.wwd.getSceneController().getDrawContext().getTerrain();

            // Compute a line through the pick point.
            Line line = this.wwd.getView().computeRayFromScreenPoint(screenPoint.x, screenPoint.y);

            // Find the intersection of the line and the shape.
            List<Intersection> intersections = shape.intersect(line, terrain);
            if (intersections != null && !intersections.isEmpty())
                return intersections.get(0).getIntersectionPosition();
        }
        catch (InterruptedException ignored)
        {
            // Do nothing
        }

        return null;
    }

    /**
     * Get the position of the balloon for a KML placemark. For a point placemark, this method returns the placemark
     * point. For all other placemarks, this method returns the centroid of the sector that bounds all of the points in
     * the placemark. Note that the centroid of the sector may not actually fall on the visible area of the shape.
     *
     * @param placemark Placemark for which to find a balloon position.
     *
     * @return Position for the balloon, or null if a position cannot be determined.
     *
     * @see #getBalloonPosition(gov.nasa.worldwind.ogc.kml.KMLAbstractFeature)
     */
    protected Position getBalloonPositionForPlacemark(KMLPlacemark placemark)
    {
        List<Position> positions = new ArrayList<Position>();

        KMLAbstractGeometry geometry = placemark.getGeometry();
        KMLUtil.getPositions(this.wwd.getModel().getGlobe(), geometry, positions);

        return this.getBalloonPosition(positions);
    }

    /**
     * Get the position of the balloon for a KML GroundOverlay. This method returns the centroid of the sector that
     * bounds all of the points in the overlay.
     *
     * @param overlay Ground overlay for which to find a balloon position.
     *
     * @return Position for the balloon, or null if a position cannot be determined.
     *
     * @see #getBalloonPosition(gov.nasa.worldwind.ogc.kml.KMLAbstractFeature)
     */
    protected Position getBalloonPositionForGroundOverlay(KMLGroundOverlay overlay)
    {
        Position.PositionList positionsList = overlay.getPositions();
        return this.getBalloonPosition(positionsList.list);
    }

    /**
     * Get the position of the balloon for a list of positions that bound a feature. This method returns a position at
     * the centroid of the sector that bounds all of the points in the list, and at the maximum altitude of the points
     * in the list.
     *
     * @param positions List of positions to find a balloon position.
     *
     * @return Position for the balloon, or null if a position cannot be determined.
     */
    protected Position getBalloonPosition(List<? extends Position> positions)
    {
        if (positions.size() == 1) // Only one point, just return the point
        {
            return positions.get(0);
        }
        else if (positions.size() > 1)// Many points, find center point of bounding sector
        {
            Sector sector = Sector.boundingSector(positions);

            return new Position(sector.getCentroid(), this.findMaxAltitude(positions));
        }
        return null;
    }

    /**
     * Get the screen point for a balloon for a KML feature attached to the screen. This method applies only to KML
     * features that area attached to the screen, rather than to the globe (for example, ScreenOverlay, but not
     * GroundOverlay). This method determines the type of feature, and then calls a more specific method to handle
     * features of that type.
     *
     * @param feature Feature for which to find a balloon point.
     *
     * @return Point for the balloon, or null if a point cannot be determined.
     *
     * @see #getBalloonPointForScreenOverlay(gov.nasa.worldwind.ogc.kml.KMLScreenOverlay)
     * @see #getBalloonPosition(gov.nasa.worldwind.ogc.kml.KMLAbstractFeature)
     */
    protected Point getBalloonPoint(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLScreenOverlay)
        {
            return this.getBalloonPointForScreenOverlay((KMLScreenOverlay) feature);
        }
        return null;
    }

    /**
     * Get the screen point for a balloon for a ScreenOverlay.
     *
     * @param overlay ScreenOverlay for which to find a balloon position.
     *
     * @return Point for the balloon, or null if a point cannot be determined.
     *
     * @see #getBalloonPoint(gov.nasa.worldwind.ogc.kml.KMLAbstractFeature)
     */
    protected Point getBalloonPointForScreenOverlay(KMLScreenOverlay overlay)
    {
        KMLVec2 xy = overlay.getScreenXY();

        Offset offset = new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
            KMLUtil.kmlUnitsToWWUnits(xy.getYunits()));

        Rectangle viewport = this.wwd.getView().getViewport();
        Point2D point2D = offset.computeOffset(viewport.width, viewport.height, 1d, 1d);

        int y = (int) point2D.getY();
        return new Point((int) point2D.getX(), viewport.height - y);
    }

    /**
     * Get the maximum altitude in a list of positions.
     *
     * @param positions List of positions to search for max altitude.
     *
     * @return The maximum elevation in the list of positions. Returns {@code -Double.MAX_VALUE} if {@code positions} is
     *         empty.
     */
    protected double findMaxAltitude(List<? extends Position> positions)
    {
        double maxAltitude = -Double.MAX_VALUE;
        for (Position p : positions)
        {
            double altitude = p.getAltitude();
            if (altitude > maxAltitude)
                maxAltitude = altitude;
        }

        return maxAltitude;
    }

    //**********************************************************************//
    //******************  Remote document retrieval  ***********************//
    //**********************************************************************//

    /**
     * Search for a KML document that has already been opened. This method looks in the session cache for a parsed
     * KMLRoot.
     *
     * @param url URL of the KML document.
     *
     * @return KMLRoot for an already-parsed document, or null if the document was not found in the cache.
     */
    protected KMLRoot findOpenKmlDocument(String url)
    {
        Object o = WorldWind.getSessionCache().get(url);
        if (o instanceof KMLRoot)
            return (KMLRoot) o;
        else
            return null;
    }

    /**
     * Asynchronously load a KML document. When the document is available, {@link #onDocumentLoaded(String,
     * gov.nasa.worldwind.ogc.kml.KMLRoot, String) onDocumentLoaded} will be called on the Event Dispatch Thread (EDT).
     * If the document fails to load, {@link #onDocumentFailed(String, Exception) onDocumentFailed} will be called.
     * Failure will be reported if the document does not load within {@link #retrievalTimeout} milliseconds.
     *
     * @param url        URL of KML doc to open.
     * @param context    Context of the URL, used to resolve local references.
     * @param featureRef A reference to a feature in the remote file to animate the globe to once the file is
     *                   available.
     *
     * @see #onDocumentLoaded(String, gov.nasa.worldwind.ogc.kml.KMLRoot, String)
     * @see #onDocumentFailed(String, Exception)
     */
    protected void requestDocument(String url, KMLRoot context, String featureRef)
    {
        Timer docLoader = new Timer("BalloonController document retrieval");

        // Schedule a task that will request the document periodically until the document becomes available or the
        // request timeout is reached.
        docLoader.scheduleAtFixedRate(new DocumentRetrievalTask(url, context, featureRef, this.retrievalTimeout),
            0, this.retrievalPollInterval);
    }

    /**
     * Called when a KML document has been loaded. This implementation creates a new layer and adds the new document to
     * the layer.
     *
     * @param url        URL of the document that has been loaded.
     * @param document   Parsed document.
     * @param featureRef Reference to a feature that must be activated (fly to or open balloon).
     */
    protected void onDocumentLoaded(String url, KMLRoot document, String featureRef)
    {
        // Use the URL as the document's DISPLAY_NAME. This field is used by addDocumentLayer to determine the layer's
        // name.
        document.setField(AVKey.DISPLAY_NAME, url);
        this.addDocumentLayer(document);

        if (featureRef != null)
            this.onFeatureLinkActivated(document, featureRef, null);
    }

    /**
     * Called when a KML file fails to load due to a network timeout or parsing error. This implementation simply logs a
     * warning.
     *
     * @param url URL of the document that failed to load.
     * @param e   Exception that caused the failure.
     */
    protected void onDocumentFailed(String url, Exception e)
    {
        String message = Logging.getMessage("generic.ExceptionWhileReading", url + ": " + e.getMessage());
        Logging.logger().warning(message);
    }

    /**
     * Adds the specified <code>document</code> to this controller's <code>WorldWindow</code> as a new
     * <code>Layer</code>.
     * <p>
     * This expects the <code>kmlRoot</code>'s <code>AVKey.DISPLAY_NAME</code> field to contain a display name suitable
     * for use as a layer name.
     *
     * @param document the KML document to add a <code>Layer</code> for.
     */
    protected void addDocumentLayer(KMLRoot document)
    {
        KMLController controller = new KMLController(document);

        // Load the document into a new layer.
        RenderableLayer kmlLayer = new RenderableLayer();
        kmlLayer.setName((String) document.getField(AVKey.DISPLAY_NAME));
        kmlLayer.addRenderable(controller);

        this.wwd.getModel().getLayers().add(kmlLayer);
    }

    /**
     * A TimerTask that will request a resource from the {@link gov.nasa.worldwind.cache.FileStore} until it becomes
     * available, or until a timeout is exceeded. When the task finishes it will trigger a callback on the Event
     * Dispatch Thread (EDT) to either {@link BalloonController#onDocumentLoaded(String,
     * gov.nasa.worldwind.ogc.kml.KMLRoot, String) onDocumentLoaded} or {@link BalloonController#onDocumentFailed(String,
     * Exception) onDocumentFailed}.
     * <p>
     * This task is designed to be repeated periodically. The task will cancel itself when the document becomes
     * available, or the timeout is exceeded.
     */
    protected class DocumentRetrievalTask extends TimerTask
    {
        /** URL of the KML document to load. */
        protected String docUrl;
        /** The document that contained the link this document. */
        protected KMLRoot context;
        /**
         * Reference to a feature in the remote document, with an action (for example, "myFeature;flyto"). The action
         * will be carried out when the document becomes available.
         */
        protected String featureRef;
        /**
         * Task timeout. If the document has not been loaded after this many milliseconds, the task will cancel itself
         * and report an error.
         */
        protected long timeout;
        /** Time that the task started, used to evaluate the timeout. */
        protected long start;

        /**
         * Create a new retrieval task.
         *
         * @param url        URL of document to retrieve.
         * @param context    Context of the link to the document. May be null.
         * @param featureRef Reference to a feature in the remote document, with an action to perform on the feature
         *                   (for example, "myFeature;flyto"). The action will be carried out when the document becomes
         *                   available.
         * @param timeout    Timeout for this task in milliseconds. The task will fail if the document has not been
         *                   downloaded in this many milliseconds.
         */
        public DocumentRetrievalTask(String url, KMLRoot context, String featureRef, long timeout)
        {
            this.docUrl = url;
            this.context = context;
            this.featureRef = featureRef;
            this.timeout = timeout;
        }

        /**
         * Request the document from the {@link gov.nasa.worldwind.cache.FileStore}. If the document is available, parse
         * it and schedule a callback on the EDT to {@link BalloonController#onDocumentLoaded(String,
         * gov.nasa.worldwind.ogc.kml.KMLRoot, String)}. If an exception occurs, or the timeout is exceeded, schedule a
         * callback on the EDT to {@link BalloonController#onDocumentFailed(String, Exception)}
         */
        public void run()
        {
            KMLRoot root = null;

            try
            {
                // If this is the first execution, capture the start time so that we can evaluate the timeout later.
                if (this.start == 0)
                    this.start = System.currentTimeMillis();

                // Check for timeout before doing any work
                if (System.currentTimeMillis() > this.start + this.timeout)
                    throw new WWTimeoutException(Logging.getMessage("generic.CannotOpenFile", this.docUrl));

                // If we have a context document, let that doc resolve the reference. Otherwise, request it from the
                // file store.
                Object docSource;
                if (this.context != null)
                    docSource = this.context.resolveReference(this.docUrl);
                else
                    docSource = WorldWind.getDataFileStore().requestFile(this.docUrl);

                if (docSource instanceof KMLRoot)
                {
                    root = (KMLRoot) docSource;
                    // Roots returned by resolveReference are already parsed, no need to parse here
                }
                else if (docSource != null)
                {
                    root = KMLRoot.create(docSource);
                    root.parse();
                }

                // If root is non-null we have succeeded in loading the document.
                if (root != null)
                {
                    // Schedule a callback on the EDT to let the BalloonController finish loading the document.
                    final KMLRoot pinnedRoot = root; // Final ref that can be accessed by anonymous class
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            BalloonController.this.onDocumentLoaded(docUrl, pinnedRoot, featureRef);
                        }
                    });

                    this.cancel();
                }
            }
            catch (final Exception e)
            {
                // Schedule a callback on the EDT to report the error to the BalloonController
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        BalloonController.this.onDocumentFailed(docUrl, e);
                    }
                });
                this.cancel();
            }
        }
    }
}
