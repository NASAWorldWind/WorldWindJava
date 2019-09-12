/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVList;

import java.net.URL;
import java.util.List;

/**
 * Provides tracking of per-host network availability. Host computers that fail network requests can be logged to the
 * implementing object's tracking list. When a host has been logged a specified number of times, it is marked as
 * unreachable. Users can query instances of classes implementing this interface to determine whether a host has been
 * marked as unreachable.
 * <p>
 * Users are expected to invoke the {@link #logUnavailableHost(java.net.URL)} method when an attempt to contact a host
 * fails. Each invocation increments the failure count by one. When the count exceeds the attempt limit, the host is
 * marked as unreachable. When attempts to contact the host <em>are</em> successful, users should invoke {@link
 * #logAvailableHost(java.net.URL)} method to clear its status.
 * <p>
 * A host may become reachable at a time subsequent to its being logged. To detect this, the implementation marks a host
 * as not unreachable after a specifiable interval of time. If the host is once more logged as unavailable, its entry
 * returns to the unavailable state. This cycle continues indefinitely.
 * <p>
 * Methods are provided to determine whether the public network can be reached and whether the NASA WorldWind servers
 * cab be reached. The addresses used to detect public network access can be explicitly specified.
 *
 * @author tag
 * @version $Id: NetworkStatus.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface NetworkStatus extends AVList
{
    public static final String HOST_UNAVAILABLE = "gov.nasa.worldwind.util.NetworkStatus.HostUnavailable";
    public static final String HOST_AVAILABLE = "gov.nasa.worldwind.util.NetworkStatus.HostAvailable";

    /**
     * Log a host as unavailable. Each invocation increments the host's attempt count. When the count equals or exceeds
     * the attempt limit, the host is marked as unavailable.
     *
     * @param url a url containing the host to mark as unavailable.
     */
    void logUnavailableHost(URL url);

    /**
     * Log a host as available. Each invocation causes the host to no longer be marked as unavailable. Its
     * unavailability count is effectively set to 0.
     *
     * @param url a url containing the host to mark as available.
     */
    void logAvailableHost(URL url);

    /**
     * Indicates whether the host has been marked as unavailable. To be marked unavailable a host's attempt count must
     * exceed the specified attempt limit.
     *
     * @param url a url containing the host to check for availability.
     *
     * @return true if the host is marked as unavailable, otherwise false.
     */
    boolean isHostUnavailable(URL url);

    /**
     * Indicates whether a public network can be reached or has been reached in the previous five seconds.
     *
     * @return false if the network can be reached or has been reached in the previous five seconds, otherwise true.
     */
    boolean isNetworkUnavailable();

    /**
     * Indicates whether a public network can be reached or has been reached in a specified previous amount of time.
     *
     * @param checkInterval the number of milliseconds in the past used to determine whether the server was avaialble
     *                      recently.
     *
     * @return false if the network can be reached or has been reached in a specified time, otherwise true.
     */
    boolean isNetworkUnavailable(long checkInterval);

    /**
     * Indicates whether the NASA WorldWind servers can be reached.
     *
     * @return false if the servers can be reached, otherwise true.
     */
    boolean isWorldWindServerUnavailable();

    /**
     * Returns the number of times a host must be logged as unavailable before it is marked unavailable in this class.
     *
     * @return the limit.
     */
    int getAttemptLimit();

    /**
     * Returns the length of time to wait until a host is marked as not unreachable subsequent to its being marked
     * unreachable.
     *
     * @return the interval, in milliseconds.
     */
    long getTryAgainInterval();

    /**
     * Indicates whether WorldWind will attempt to connect to the network to retrieve data or for other reasons.
     *
     * @return <code>true</code> if WorldWind is in off-line mode, <code>false</code> if not.
     */
    boolean isOfflineMode();

    /**
     * Indicates whether WorldWind should attempt to connect to the network to retrieve data or for other reasons. The
     * default value for this attribute is <code>false</code>, indicating that the network should be used.
     *
     * @param offlineMode <code>true</code> if WorldWind should use the network, <code>false</code> otherwise
     */
    void setOfflineMode(boolean offlineMode);

    /**
     * Sets the number of times a host must be logged as unavailable before it is marked unavailable in this class.
     *
     * @param limit the number of log-unavailability invocations necessary to consider the host unreachable.
     *
     * @throws IllegalArgumentException if the limit is less than 1.
     */
    void setAttemptLimit(int limit);

    /**
     * Sets the length of time to wait until a host is marked as not unreachable subsequent to its being marked
     * unreachable.
     *
     * @param interval The length of time, in milliseconds, to wait to unmark a host as unreachable.
     *
     * @throws IllegalArgumentException if the interval is less than 0.
     */
    void setTryAgainInterval(long interval);

    /**
     * Returns the server domain names of the sites used to test public network availability.
     *
     * @return the list of sites used to check network status. The list is a copy of the internal list, so changes to it
     *         do not affect instances of this class.
     */
    List<String> getNetworkTestSites();

    /**
     * Sets the domain names, e.g., worldwind.arc.nasa.gov, of sites used to determine public network availability.
     *
     * @param networkTestSites the list of desired test sites. The list is copied internally, so changes made to the
     *                         submitted list do not affect instances of this class. May be null, in which case no sites
     *                         are consulted.
     */
    void setNetworkTestSites(List<String> networkTestSites);
}
