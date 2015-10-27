/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.event.BulkRetrievalListener;
import gov.nasa.worldwind.geom.Sector;

/**
 * Interface for classes whose data may be retrieved in bulk from its remote source. When used, will copy the requested
 * data to either the local World Wind cache or a specified filestore. Data already contained in the specified location
 * is not recopied.
 *
 * @author Patrick Murris
 * @version $Id: BulkRetrievable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface BulkRetrievable
{
    /**
     * Initiates data retrieval to the current World Wind data cache. The method starts a new thread to perform the
     * retrieval. The thread terminates when either all the requested data has been retrieved or when any data not
     * retrieved is determined to be unretrievable.
     *
     * @param sector     the sector for which to retrieve the data.
     * @param resolution the resolution desired. All data within the specified sector up to and including this
     *                   resolution is downloaded.
     * @param listener   an optional bulk-download listener that can be used to monitor the success or failure of
     *                   individual retrievals. Note: The listener is called on the thread performing the download,
     *                   which is not the event dispatch thread. Therefore any interaction with AWT or Swing within the
     *                   call must be done within a call to SwingUtilities.invokeLater().
     *
     * @return returns the running thread created to perform the retrieval.
     */
    BulkRetrievalThread makeLocal(Sector sector, double resolution, BulkRetrievalListener listener);

    /**
     * Estimates the amount of data, in bytes, that must be retrieved to the World Wind data cache for a specified
     * sector and resolution.
     *
     * @param sector     the sector for which to retrieve the data.
     * @param resolution the resolution desired. All data within the specified sector up to and including this
     *                   resolution is downloaded.
     *
     * @return the estimated data size, in bytes.
     */
    long getEstimatedMissingDataSize(Sector sector, double resolution);

    /**
     * Estimates the amount of data, in bytes, that must be retrieved to a specified filestore for a specified sector
     * and resolution.
     *
     * @param sector     the sector for which to retrieve the data.
     * @param resolution the resolution desired. All data within the specified sector up to and including this
     *                   resolution is downloaded.
     * @param fileStore  the location to place the data. If null, the current World Wind cache is used.
     *
     * @return the estimated data size, in bytes.
     */
    long getEstimatedMissingDataSize(Sector sector, double resolution, FileStore fileStore);

    /**
     * Initiates data retrieval to a specified filestore. The method starts a new thread to perform the
     * retrieval. The thread terminates when either all the requested data has been retrieved or when any data not
     * retrieved is determined to be unretrievable.
     *
     * @param sector     the sector for which to retrieve the data.
     * @param resolution the resolution desired. All data within the specified sector up to and including this
     *                   resolution is downloaded.
     * @param listener   an optional bulk-download listener that can be used to monitor the success or failure of
     *                   individual retrievals. Note: The listener is called on the thread performing the download,
     *                   which is not the event dispatch thread. Therefore any interaction with AWT or Swing within the
     *                   call must be done within a call to SwingUtilities.invokeLater().
     * @param fileStore  the location to place the data. If null, the current World Wind cache is used.
     *
     * @return returns the running thread created to perform the retrieval.
     */
    BulkRetrievalThread makeLocal(Sector sector, double resolution, FileStore fileStore,
        BulkRetrievalListener listener);
    
    String getName();
}
