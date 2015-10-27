/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.util.Logging;

/**
 * <code>
 * <pre>
 * [ MIL-STD-2411, section 4.5.4.4]
 * a. All RPF [frame file] names shall be logically coded as follows:
 * [file name]
 *     {1}
 *     <reference designator>, ascii: 8
 *     <period>, ascii: 1
 *     <data series and zone>, ascii: 3
 *
 * d. [file name]s shall contain the following logical elements, listed in alphabetical order:
 *     (1) <data series and zone> := a 3-byte ASCII character field...
 *     encoded as specified in MIL-STD-2411-1, section 5.1.5
 *
 *     (2) <period> := a 1-byte ASCII character field := "."
 *
 *     (3) <reference designator> := an 8-byte ASCII character field...
 *     Each byte of <reference designator> shall have one of the following possible values:
 *     any digit from 0 through 9; or any capital letter, A through Z, excluding I and O
 * </pre>
 * </code>
 *
 * @author dcollins
 * @version $Id: RPFFrameFilename.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFFrameFilename
{
    // TODO: use nitfs-rpf naming convention

    private final String dataSeriesCode;
    private final int frameNumber;
    private final char producerId;
    private final int version;
    private final char zoneCode;
    // Cached values.
    private int hashCode = -1;
    private String filename = null;

    private static final int FILENAME_LENGTH = 12;

    private RPFFrameFilename(String dataSeriesCode, int frameNumber, char producerId, int version, char zoneCode)
    {
        this.dataSeriesCode = dataSeriesCode;
        this.frameNumber = frameNumber;
        this.producerId = producerId;
        this.version = version;
        this.zoneCode = zoneCode;
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    public static RPFFrameFilename parseFilename(String filename)
    {
        if (filename == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (filename.length() != FILENAME_LENGTH)
        {
            String message = Logging.getMessage("RPFFrameFilename.BadFilenameLength", filename);
            Logging.logger().fine(message);
            throw new RPFFrameFilenameFormatException(message);
        }

        char[] buffer = new char[FILENAME_LENGTH];        
        filename.getChars(0, FILENAME_LENGTH, buffer, 0);

        char producerId = buffer[7];
        String dataSeriesCode = filename.substring(9, 11);
        char zoneCode = buffer[11];

        // Default to CADRG filename structure.
        int frameChars = 5;
        int versionChars = 2;
        if (RPFDataSeries.isCIBDataSeries(dataSeriesCode))
        {
            frameChars = 6;
            versionChars = 1;
        }

        int frameNumber;
        int version;
        try
        {
            frameNumber = Base34Converter.parseChars(buffer, 0, frameChars);
            version = Base34Converter.parseChars(buffer, frameChars, versionChars);
        }
        catch (IllegalArgumentException e)
        {
            String message = Logging.getMessage("RPFFrameFilename.IntegerNotParsed");
            Logging.logger().fine(message);
            throw new RPFFrameFilenameFormatException(message, e);
        }

        return new RPFFrameFilename(dataSeriesCode, frameNumber, producerId, version, zoneCode);
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    private static void toCharArray(RPFFrameFilename frameFilename, char[] dest)
    {
        // Default to CARDG filename strucutre.
        int frameChars = 5;
        int versionChars = 2;
        if (RPFDataSeries.isCIBDataSeries(frameFilename.dataSeriesCode))
        {
            frameChars = 6;
            versionChars = 1;
        }

        Base34Converter.valueOf(frameFilename.frameNumber, dest, 0, frameChars);
        Base34Converter.valueOf(frameFilename.version, dest, frameChars, versionChars);
        dest[7] = frameFilename.producerId;
        dest[8] = '.';
        frameFilename.dataSeriesCode.getChars(0, 2, dest, 9);
        dest[11] = frameFilename.zoneCode;
    }

    public final boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || o.getClass() != this.getClass())
            return false;

        final RPFFrameFilename that = (RPFFrameFilename) o;
        if (Character.toUpperCase(this.zoneCode) != Character.toUpperCase(that.zoneCode))
            return false;
        if (this.frameNumber != that.frameNumber)
            return false;
        if (this.dataSeriesCode != null ? !this.dataSeriesCode.equalsIgnoreCase(that.dataSeriesCode) : that.dataSeriesCode != null)
            return false;
        if (Character.toUpperCase(this.producerId) != Character.toUpperCase(that.producerId))
            return false;
        //noinspection RedundantIfStatement
        if (this.version != that.version)
            return false;

        return true;
    }

    public final String getDataSeriesCode()
    {
        return this.dataSeriesCode;
    }

    public final String getFilename()
    {
        if (this.filename == null)
        {
            char[] buffer = new char[FILENAME_LENGTH];
            toCharArray(this, buffer);
            this.filename = new String(buffer);
        }
        return this.filename;
    }

    public final int getFrameNumber()
    {
        return this.frameNumber;
    }

    public final char getProducerId()
    {
        return this.producerId;
    }

    public final int getVersion()
    {
        return this.version;
    }

    public final char getZoneCode()
    {
        return this.zoneCode;
    }

    public int hashCode()
    {
        if (this.hashCode < 0)
            this.hashCode = this.computeHash();
        return this.hashCode;
    }

    private int computeHash()
    {
        int hash = 0;
        if (this.dataSeriesCode != null)
            hash = this.dataSeriesCode.hashCode();
        hash = 29 * hash + frameNumber;
        hash = 29 * hash + (int) producerId;
        hash = 29 * hash + version;
        hash = 29 * hash + this.zoneCode;
        return hash;
    }

    public static boolean isFilename(String str)
    {
        if (str == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        if (str.length() != FILENAME_LENGTH)
            return false;

        char[] buffer = new char[FILENAME_LENGTH];
        str.getChars(0, 12, buffer, 0);

        if (!Base34Converter.isBase34(buffer, 0, 7))
            return false;
        if (!RPFProducer.isProducerId(buffer[7]))
            return false;
        if ('.' != buffer[8])
            return false;
        String seriesCode = str.substring(9, 11);
        if (!RPFDataSeries.isDataSeriesCode(seriesCode))
            return false;
        //noinspection RedundantIfStatement
        if (!RPFZone.isZoneCode(buffer[11]))
            return false;

        return true;
    }

    public final String toString()
    {
        return this.getFilename();
    }
}
