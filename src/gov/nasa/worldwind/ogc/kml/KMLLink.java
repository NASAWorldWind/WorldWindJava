/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.awt.*;
import java.net.*;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents the KML <i>Link</i> element and provides access to its contents. The Link maintains a timestamp that
 * indicates when the resource was last updated. The timestamp is updated based on the link's refresh mode. Code that
 * uses a resource loaded from a Link should keep track of when the resource was retrieved, and periodically check that
 * timestamp against the timestamp returned by {@link #getUpdateTime()}.
 *
 * @author tag
 * @version $Id: KMLLink.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLLink extends KMLAbstractObject
{
    protected static final String DEFAULT_VIEW_FORMAT = "BBOX=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]";

    /** The time, in milliseconds since the Epoch, at which the linked content was most recently updated. */
    protected AtomicLong updateTime = new AtomicLong();

    /** If this link's href does not need to be modified with a query string, this value is the resource address. */
    protected String finalHref;
    /** The {@link URL} for the raw href. Will be null if the href is not a remote URL or this link has a query string */
    protected URL hrefURL;

    /** Scheduled task that will update the when it runs. Used to implement {@code onInterval} refresh mode. */
    protected ScheduledFuture refreshTask;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLLink(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getHref()
    {
        return (String) this.getField("href");
    }

    public String getRefreshMode()
    {
        return (String) this.getField("refreshMode");
    }

    public Double getRefreshInterval()
    {
        return (Double) this.getField("refreshInterval");
    }

    public String getViewRefreshMode()
    {
        return (String) this.getField("viewRefreshMode");
    }

    public Double getViewRefreshTime()
    {
        return (Double) this.getField("viewRefreshTime");
    }

    public Double getViewBoundScale()
    {
        return (Double) this.getField("viewBoundScale");
    }

    public String getViewFormat()
    {
        return (String) this.getField("viewFormat");
    }

    public String getHttpQuery()
    {
        return (String) this.getField("httpQuery");
    }

    /** {@inheritDoc} Overridden to mark {@code onChange} links as updated when a field set. */
    @Override
    public void setField(String keyName, Object value)
    {
        super.setField(keyName, value);

        // If the link refreshes "onChange", mark the link as updated because it has changed.
        if (KMLConstants.ON_CHANGE.equals(this.getRefreshMode()))
        {
            this.setUpdateTime(System.currentTimeMillis());
        }
    }

    /**
     * Returns the time at which the linked resource was last updated. This method is safe to call from any thread.
     *
     * @return The time at which the linked content was most recently updated. See {@link System#currentTimeMillis()}
     *         for its numerical meaning of this timestamp.
     */
    public long getUpdateTime()
    {
        // Schedule a task to refresh the link if the refresh mode requires it. If the client code never calls
        // getUpdateTime, the link may never refresh. But in this case, the link is never rendered, so it doesn't matter.
        // Scheduling a refresh task only when the link is actually used avoids creating a long running update task
        // that may keep the link and it's document in memory even if the application is no longer using the link.
        this.scheduleRefreshIfNeeded();

        return this.updateTime.get();
    }

    /**
     * Specifies the time at which the linked resource was last updated.  This method is safe to call from any thread.
     *
     * @param updateTime The time at which the linked content was most recently updated. See {@link
     *                   System#currentTimeMillis()} for its numerical meaning of this timestamp.
     */
    public void setUpdateTime(long updateTime)
    {
        this.updateTime.set(updateTime);
    }

    /**
     * Specifies the time at which the linked resource expires. If the link's update mode is onExpire, the link will
     * mark itself updated at this time.
     *
     * @param time Time, in milliseconds since the Epoch, at which the link expires. Zero indicates no expiration.
     */
    public void setExpirationTime(long time)
    {
        // If the refresh mode is onExpire, schedule a task to update the link at the expiration time. Otherwise
        // we don't care about the expiration.
        if (KMLConstants.ON_EXPIRE.equals(this.getRefreshMode()))
        {
            // If there is already a task running, cancel it
            if (this.refreshTask != null)
                this.refreshTask.cancel(false);

            if (time != 0)
            {
                long refreshDelay = time - System.currentTimeMillis();
                this.refreshTask = this.scheduleDelayedTask(new RefreshTask(), refreshDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Schedule a task to refresh the link if the refresh mode requires it. In the case of an {@code onInterval} and
     * {@code onExpire} refresh modes, this method schedules a task to update the link after the refresh interval
     * elapses, but only if such a task has not already been scheduled (only one refresh task is active at a time).
     */
    protected void scheduleRefreshIfNeeded()
    {
        Long refreshTime = this.computeRefreshTime();
        if (refreshTime == null)
            return;

        // Determine if the refresh interval has elapsed since the last refresh task was scheduled.
        boolean intervalElapsed = System.currentTimeMillis() > refreshTime;

        // If the refresh interval has already elapsed then the link needs to refresh immediately.
        if (intervalElapsed)
            this.updateTime.set(System.currentTimeMillis());

        // Schedule a refresh if the refresh interval has elapsed, or if no refresh task is already active.
        // Checking the refresh interval ensures that even if the task fails to run for some reason, a new
        // task will be scheduled after the interval expires.
        if (intervalElapsed || this.refreshTask == null || this.refreshTask.isDone())
        {
            long refreshDelay = refreshTime - System.currentTimeMillis();
            this.refreshTask = this.scheduleDelayedTask(new RefreshTask(), refreshDelay, TimeUnit.MILLISECONDS);
        }
    }

    protected Long computeRefreshTime()
    {
        Long refreshTime = null;

        // Only handle onInterval here. onExpire is handled by KMLNetworkLink when the network resource is retrieved.
        if (KMLConstants.ON_INTERVAL.equals(this.getRefreshMode()))
        {
            Double ri = this.getRefreshInterval();
            refreshTime = ri != null ? this.updateTime.get() + (long) (ri * 1000d) : null;
        }

        if (refreshTime == null)
            return null;

        KMLNetworkLinkControl linkControl = this.getRoot().getNetworkLinkControl();
        if (linkControl != null)
        {
            Long minRefresh = (long) (linkControl.getMinRefreshPeriod() * 1000d);
            if (minRefresh != null && minRefresh > refreshTime)
                refreshTime = minRefresh;
        }

        return refreshTime;
    }

    /**
     * Schedule the link to refresh when an {@link View#VIEW_STOPPED} message is received, and the link's view refresh
     * mode is {@code onStop}.
     *
     * @param msg The message that was received.
     */
    @Override
    public void onMessage(Message msg)
    {
        String viewRefreshMode = this.getViewRefreshMode();
        if (View.VIEW_STOPPED.equals(msg.getName()) && KMLConstants.ON_STOP.equals(viewRefreshMode))
        {
            Double refreshTime = this.getViewRefreshTime();
            if (refreshTime != null)
            {
                this.scheduleDelayedTask(new RefreshTask(), refreshTime.longValue(), TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Schedule a task to mark a link as updated after a delay. The task only executes once.
     *
     * @param task     Task to schedule.
     * @param delay    Delay to wait before executing the task. The time unit is determined by {code timeUnit}.
     * @param timeUnit The time unit of {@code delay}.
     *
     * @return Future that represents the scheduled task.
     */
    protected ScheduledFuture scheduleDelayedTask(Runnable task, long delay, TimeUnit timeUnit)
    {
        return WorldWind.getScheduledTaskService().addScheduledTask(task, delay, timeUnit);
    }

    /** {@inheritDoc} Overridden to set a default refresh mode of {@code onChange} if the refresh mode is not specified. */
    @Override
    public Object parse(XMLEventParserContext ctx, XMLEvent inputEvent, Object... args) throws XMLStreamException
    {
        Object o = super.parse(ctx, inputEvent, args);

        // If the link does not have a refresh mode, set the default refresh mode of "onChange". We set an explicit
        // default after parsing is complete so that we can distinguish links that do not specify a refresh mode from
        // those in which the refresh mode has not yet been parsed.
        if (WWUtil.isEmpty(this.getRefreshMode()))
        {
            this.setField("refreshMode", KMLConstants.ON_CHANGE);
        }

        return o;
    }

    /**
     * Returns the address of the resource specified by this KML link. If the resource specified in this link's
     * <code>href</code> is a local resource, this returns only the <code>href</code>, and ignores the
     * <code>viewFormat</code> and <code>httpQuery</code>. Otherwise, this returns the concatenation of the
     * <code>href</code>, the <code>viewFormat</code> and the <code>httpQuery</code> for form an absolute URL string. If
     * the the <code>href</code> contains a query string, the <code>viewFormat</code> and <code>httpQuery</code> are
     * appended to that string. If necessary, this inserts the <code>&amp;</code> character between the <code>href</code>'s
     * query string, the <code>viewFormat</code>, and the <code>httpQuery</code>.
     * <p>
     * This substitutes the following parameters in <code>viewFormat</code> and <code>httpQuery</code>: <ul>
     * <li><code>[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]</code> - visible bounds of the globe, or 0 if the globe
     * is not visible. The visible bounds are scaled from their centroid by this link's
     * <code>viewBoundScale</code>.</li> <li><code>[lookatLon], [lookatLat]</code> - longitude and latitude of the
     * position on the globe the view is looking at, or 0 if the view is not looking at the globe.</li>
     * <li><code>[lookatRange]</code> - distance between view's eye position and the point on the globe the view is
     * looking at.</li> <li><code>[lookatTilt], [lookatHeading]</code> - view's tilt and heading.</li>
     * <li><code>[lookatTerrainLon], [lookatTerrainLat], [lookatTerrainAlt]</code> - terrain position the view is
     * looking at, or 0 if the view is not looking at the terrain.</li> <li><code>[cameraLon], [cameraLat],
     * [cameraAlt]</code> - view's eye position.</li> <li><code>[horizFov], [vertFov]</code> - view's horizontal and
     * vertical field of view.</li> <li><code>[horizPixels], [vertPixels]</code> - width and height of the
     * viewport.</li> <li><code>[terrainEnabled]</code> - always <code>true</code></li> <li><code>[clientVersion]</code>
     * - WorldWind client version.</li> <li><code>[clientName]</code> - WorldWind client name.</li>
     * <li><code>[kmlVersion]</code> - KML version supported by WorldWind.</li> <li><code>[language]</code> - current
     * locale's language.</li> </ul> If the <code>viewFormat</code> is unspecified, and the <code>viewRefreshMode</code>
     * is one of <code>onRequest</code>, <code>onStop</code> or <code>onRegion</code>, this automatically appends the
     * following information to the query string: <code>BBOX=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]</code>. The
     * <code>[clientName]</code> and <code>[clientVersion]</code> parameters of the <code>httpQuery</code> may be
     * specified in the configuration file using the keys <code>{@link gov.nasa.worldwind.avlist.AVKey#NAME}</code> and
     * <code>{@link gov.nasa.worldwind.avlist.AVKey#VERSION}</code>. If not specified, this uses default values of
     * <code>{@link gov.nasa.worldwind.Version#getVersionName()}</code> and <code>{@link
     * gov.nasa.worldwind.Version#getVersion()}</code> for <code>[clientName]</code> and <code>[clientVersion]</code>,
     * respectively.
     *
     * @param dc the <code>DrawContext</code> used to determine the current view parameters.
     *
     * @return the address of the resource specified by this KML link.
     *
     * @throws IllegalArgumentException if <code>dc</code> is <code>null</code>.
     * @see #getHref()
     * @see #getViewFormat()
     * @see #getHttpQuery()
     * @see gov.nasa.worldwind.Configuration
     */
    public String getAddress(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // See if we've already determined the href is a local reference
        if (this.finalHref != null)
            return this.finalHref;

        String href = this.getHref();
        if (href != null)
            href = href.trim();

        if (WWUtil.isEmpty(href))
            return href;

        // If the href is a local resource, the viewFormat and httpQuery parameters are ignored, and we return the href.
        // We treat the href as a local resource reference if it fails to parse as a URL, or if the URL's protocol is
        // "file" or "jar".
        // See OGC KML specification 2.2.0, section 13.1.2.
        URL url = this.hrefURL != null ? this.hrefURL : WWIO.makeURL(href);
        if (url == null || this.isLocalReference(url))
        {
            this.finalHref = href; // don't need to parse the href anymore
            return href;
        }

        String queryString = this.buildQueryString(dc);
        if (WWUtil.isEmpty(queryString))
        {
            this.finalHref = href; // don't need to parse the href anymore
            return href;
        }

        this.hrefURL = url; // retain it so that we don't regenerate it every time

        try
        {
            // Create a new URL, with the full query string.
            URL newUrl = new URL(this.hrefURL.getProtocol(), this.hrefURL.getHost(), this.hrefURL.getPort(),
                this.hrefURL.getPath() + queryString);
            return newUrl.toString();
        }
        catch (MalformedURLException e)
        {
            return href; // If constructing a URL from the href and query string fails, assume this is a local file.
        }
    }

    /**
     * Returns whether the resource specified by the <code>url</code> is a local resource.
     *
     * @param url the URL to test.
     *
     * @return <code>true</code> if the <code>url</code> specifies a local resource, otherwise <code>false</code>.
     */
    protected boolean isLocalReference(URL url)
    {
        return url.getProtocol() == null || "file".equals(url.getProtocol()) || "jar".equals(url.getProtocol());
    }

    /**
     * This returns the concatenation of the query part of <code>href</code> (if any), the <code>viewFormat</code> and
     * the <code>httpQuery</code> to form the link URL's query part. This returns <code>null</code> if this link's
     * <code>href</code> does not specify a URL. This substitutes parameters in <code>viewFormat</code> according to the
     * specified <code>DrawContext</code>'s current viewing parameters, and substitutes parameters in
     * <code>httpQuery</code> according to the current <code>{@link gov.nasa.worldwind.Configuration}</code>
     * parameters.
     *
     * @param dc the <code>DrawContext</code> used to determine the current view parameters.
     *
     * @return the query part of this KML link's address, or <code>null</code> if this link does not specify a URL.
     */
    protected String buildQueryString(DrawContext dc)
    {
        URL url = WWIO.makeURL(this.getHref());
        if (url == null)
            return null;

        StringBuilder queryString = new StringBuilder(url.getQuery() != null ? url.getQuery() : "");

        String viewRefreshMode = this.getViewRefreshMode();
        if (viewRefreshMode != null)
            viewRefreshMode = viewRefreshMode.trim();

        // Ignore the viewFormat if the viewRefreshMode is unspecified or if the viewRefreshMode is "never".
        // See OGC KML specification 2.2.0, section 16.22.1.
        if (!WWUtil.isEmpty(viewRefreshMode) && !KMLConstants.NEVER.equals(viewRefreshMode))
        {
            String s = this.getViewFormat();
            if (s != null)
                s = s.trim();

            // Use a default viewFormat that includes the view bounding box parameters if no viewFormat is specified
            // and the viewRefreshMode is "onStop".
            // See Google KML Reference: http://code.google.com/apis/kml/documentation/kmlreference.html#link
            if (s == null && KMLConstants.ON_STOP.equals(viewRefreshMode))
                s = DEFAULT_VIEW_FORMAT;

            // Ignore the viewFormat if it's specified but empty.
            if (!WWUtil.isEmpty(s))
            {
                Sector viewBounds = this.computeVisibleBounds(dc);
                //noinspection ConstantConditions
                s = s.replaceAll("\\[bboxWest\\]", Double.toString(viewBounds.getMinLongitude().degrees));
                s = s.replaceAll("\\[bboxSouth\\]", Double.toString(viewBounds.getMinLatitude().degrees));
                s = s.replaceAll("\\[bboxEast\\]", Double.toString(viewBounds.getMaxLongitude().degrees));
                s = s.replaceAll("\\[bboxNorth\\]", Double.toString(viewBounds.getMaxLatitude().degrees));

                View view = dc.getView();

                Vec4 centerPoint = view.getCenterPoint();
                if (centerPoint != null)
                {
                    // Use the view's center position as the "look at" position.
                    Position centerPosition = view.getGlobe().computePositionFromPoint(centerPoint);
                    s = s.replaceAll("\\[lookatLat\\]", Double.toString(centerPosition.getLatitude().degrees));
                    s = s.replaceAll("\\[lookatLon\\]", Double.toString(centerPosition.getLongitude().degrees));
                    s = s.replaceAll("\\[lookatAlt\\]", Double.toString(centerPosition.getAltitude()));

                    double range = centerPoint.distanceTo3(view.getEyePoint());
                    s = s.replaceAll("\\[lookatRange\\]", Double.toString(range));

                    s = s.replaceAll("\\[lookatHeading\\]", Double.toString(view.getHeading().degrees));
                    s = s.replaceAll("\\[lookatTilt\\]", Double.toString(view.getPitch().degrees));

                    // TODO make sure that these terrain fields really should be treated the same as the fields above
                    s = s.replaceAll("\\[lookatTerrainLat\\]", Double.toString(centerPosition.getLatitude().degrees));
                    s = s.replaceAll("\\[lookatTerrainLon\\]", Double.toString(centerPosition.getLongitude().degrees));
                    s = s.replaceAll("\\[lookatTerrainAlt\\]", Double.toString(centerPosition.getAltitude()));
                }

                Position eyePosition = view.getCurrentEyePosition();
                s = s.replaceAll("\\[cameraLat\\]", Double.toString(eyePosition.getLatitude().degrees));
                s = s.replaceAll("\\[cameraLon\\]", Double.toString(eyePosition.getLongitude().degrees));
                s = s.replaceAll("\\[cameraAlt\\]", Double.toString(eyePosition.getAltitude()));

                s = s.replaceAll("\\[horizFOV\\]", Double.toString(view.getFieldOfView().degrees));

                Rectangle viewport = view.getViewport();
                s = s.replaceAll("\\[horizPixels\\]", Integer.toString(viewport.width));
                s = s.replaceAll("\\[vertPixels\\]", Integer.toString(viewport.height));

                // TODO: Implement support for the remaining viewFormat parameters: [vertFov] [terrainEnabled].

                if (queryString.length() > 0 && queryString.charAt(queryString.length() - 1) != '&')
                    queryString.append('&');
                queryString.append(s, s.startsWith("&") ? 1 : 0, s.length());
            }
        }

        // Ignore the httpQuery if it's unspecified, or if an empty httpQuery is specified.
        String s = this.getHttpQuery();
        if (s != null)
            s = s.trim();

        if (!WWUtil.isEmpty(s))
        {
            String clientName = Configuration.getStringValue(AVKey.NAME, Version.getVersionName());
            String clientVersion = Configuration.getStringValue(AVKey.VERSION, Version.getVersionNumber());

            //noinspection ConstantConditions
            s = s.replaceAll("\\[clientVersion\\]", clientVersion);
            s = s.replaceAll("\\[kmlVersion\\]", KMLConstants.KML_VERSION);
            s = s.replaceAll("\\[clientName\\]", clientName);
            s = s.replaceAll("\\[language\\]", Locale.getDefault().getLanguage());

            if (queryString.length() > 0 && queryString.charAt(queryString.length() - 1) != '&')
                queryString.append('&');
            queryString.append(s, s.startsWith("&") ? 1 : 0, s.length());
        }

        if (queryString.length() > 0 && queryString.charAt(0) != '?')
            queryString.insert(0, '?');

        return queryString.length() > 0 ? queryString.toString() : null;
    }

    /**
     * Returns a <code>Sector</code> that specifies the current visible bounds on the globe. If this link specifies a
     * <code>viewBoundScale</code>, this scales the visible bounds from its centroid by that factor, but limits the
     * bounds to [-90,90] latitude and [-180,180] longitude. This returns <code>{@link
     * gov.nasa.worldwind.geom.Sector#EMPTY_SECTOR}</code> if the globe is not visible.
     *
     * @param dc the <code>DrawContext</code> for which to compute the visible bounds.
     *
     * @return the current visible bounds on the specified <code>DrawContext</code>.
     */
    protected Sector computeVisibleBounds(DrawContext dc)
    {
        if (dc.getVisibleSector() != null && this.getViewBoundScale() != null)
        {
            // If the DrawContext has a visible sector and a viewBoundScale is specified, compute the view bounding box
            // by scaling the DrawContext's visible sector from its centroid, based on the scale factor specified by
            // viewBoundScale.
            double centerLat = dc.getVisibleSector().getCentroid().getLatitude().degrees;
            double centerLon = dc.getVisibleSector().getCentroid().getLongitude().degrees;
            double latDelta = dc.getVisibleSector().getDeltaLatDegrees();
            double lonDelta = dc.getVisibleSector().getDeltaLonDegrees();

            // Limit the view bounding box to the standard LatLon range. This prevents a viewBoundScale greater than one
            // from creating a bounding box that extends beyond [-90,90] latitude or [-180,180] longitude. The factory
            // methods Angle.fromDegreesLatitude and Angle.fromDegreesLongitude automatically limit latitude and
            // longitude to these ranges.
            return new Sector(
                Angle.fromDegreesLatitude(centerLat - this.getViewBoundScale() * (latDelta / 2d)),
                Angle.fromDegreesLatitude(centerLat + this.getViewBoundScale() * (latDelta / 2d)),
                Angle.fromDegreesLongitude(centerLon - this.getViewBoundScale() * (lonDelta / 2d)),
                Angle.fromDegreesLongitude(centerLon + this.getViewBoundScale() * (lonDelta / 2d)));
        }
        else if (dc.getVisibleSector() != null)
        {
            // If the DrawContext has a visible sector but no viewBoundScale is specified, use the DrawContext's visible
            // sector as the view bounding box.
            return dc.getVisibleSector();
        }
        else
        {
            // If the DrawContext does not have a visible sector, use the standard EMPTY_SECTOR as the view bounding
            // box. If the viewFormat contains bounding box parameters, we must substitute them with a valid value. In
            // this case we substitute them with 0.
            return Sector.EMPTY_SECTOR;
        }
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLLink))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        KMLLink link = (KMLLink) sourceValues;

        link.finalHref = null;
        link.hrefURL = null;
        link.refreshTask = null;
        link.updateTime.set(System.currentTimeMillis());

        super.applyChange(sourceValues);

        this.onChange(new Message(KMLAbstractObject.MSG_LINK_CHANGED, this));
    }

    /** A Runnable task that marks a KMLLink as updated when the task executes. */
    class RefreshTask implements Runnable
    {
        /** Mark the link as updated. */
        public void run()
        {
            // Mark the link as updated.
            KMLLink.this.setUpdateTime(System.currentTimeMillis());

            // Trigger a repaint to cause the link to be refreshed.
            KMLLink.this.getRoot().requestRedraw();
        }
    }
}
