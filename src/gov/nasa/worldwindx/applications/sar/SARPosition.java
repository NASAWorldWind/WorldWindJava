/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.geom.*;

/**
 * @author tag
 * @version $Id: SARPosition.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARPosition extends Position
{
    public static class Info
    {
        private final String author;
        private final long editTime;
        private final String comment;

        public Info(String author, long editTime, String comment)
        {
            this.author = author;
            this.editTime = editTime;
            this.comment = comment;
        }
    }

    private Info info;

    public SARPosition()
    {
        super(Angle.ZERO, Angle.ZERO, 0d);
    }

    public SARPosition(Angle latitude, Angle longitude, double elevation)
    {
        super(latitude, longitude, elevation);
    }

    public SARPosition(Position pos)
    {
        super(pos.getLatitude(), pos.getLongitude(), pos.getElevation());
    }
}
