/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.gdal.GDALUtils;
import org.gdal.gdal.Dataset;
import org.gdal.osr.*;

import java.awt.geom.*;

/**
 * @author Lado Garakanidze
 * @version $Id: GDAL.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class GDAL
{
    public static final int GT_SIZE = 6;

    public static final int GT_0_ORIGIN_LON = 0;
    public static final int GT_1_PIXEL_WIDTH = 1;
    public static final int GT_2_ROTATION_X = 2;
    public static final int GT_3_ORIGIN_LAT = 3;
    public static final int GT_4_ROTATION_Y = 4;
    public static final int GT_5_PIXEL_HEIGHT = 5;

    private GDAL()
    {
    }

    public static java.awt.geom.Point2D[] computeCornersFromGeotransform(double[] gt, int width, int height)
    {
        if (null == gt || gt.length != GDAL.GT_SIZE)
            return null;

        if (gt[GDAL.GT_5_PIXEL_HEIGHT] > 0)
            gt[GDAL.GT_5_PIXEL_HEIGHT] = -gt[GDAL.GT_5_PIXEL_HEIGHT];

        java.awt.geom.Point2D[] corners = new java.awt.geom.Point2D[]
            {
                getGeoPointForRasterPoint(gt, 0, height),
                getGeoPointForRasterPoint(gt, width, height),
                getGeoPointForRasterPoint(gt, width, 0),
                getGeoPointForRasterPoint(gt, 0, 0)
            };

        return corners;
    }

    public static java.awt.geom.Point2D getGeoPointForRasterPoint(double[] gt, int x, int y)
    {
        java.awt.geom.Point2D geoPoint = null;

        if (null != gt && gt.length == 6)
        {
            double easting = gt[GDAL.GT_0_ORIGIN_LON] + gt[GDAL.GT_1_PIXEL_WIDTH] * (double) x
                + gt[GDAL.GT_2_ROTATION_X] * (double) y;

            double northing = gt[GDAL.GT_3_ORIGIN_LAT] + gt[GDAL.GT_4_ROTATION_Y] * (double) x
                + gt[GDAL.GT_5_PIXEL_HEIGHT] * (double) y;

            geoPoint = new java.awt.geom.Point2D.Double(easting, northing);
        }

        return geoPoint;
    }

    public static class Area
    {
        protected SpatialReference srs;
        protected java.awt.geom.Point2D[] corners = null;
        protected Sector bbox = null; // its a sector for Geodetic rasters, and a BoundingBox for projected rasters

        public Area(SpatialReference srs, Dataset ds) throws IllegalArgumentException
        {
            if (null == ds)
            {
                String message = Logging.getMessage("nullValue.DataSetIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (null == srs)
            {
                String wkt = ds.GetProjectionRef();
                if (null != wkt && wkt.length() > 0)
                    srs = new SpatialReference(wkt);

                if (null == srs)
                {
                    String message = Logging.getMessage("nullValue.SpatialReferenceIsNull");
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }
            }
            if (srs.IsGeographic() == 0 && srs.IsProjected() == 0)
            {
                String message = Logging.getMessage("generic.UnexpectedCoordinateSystem", srs.ExportToWkt());
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.srs = srs.Clone();

            // retrieve GeoTransform matrix
            double[] gt = new double[6];
            ds.GetGeoTransform(gt);

            this.corners = GDAL.computeCornersFromGeotransform(gt, ds.getRasterXSize(), ds.getRasterYSize());
            this.bbox = calcBoundingSector(srs, this.corners);
        }

        /**
         * calculates a Geodetic bounding box
         *
         * @param srs     A Spatial Reference, must not be null and not LOCAL (aka SCREEN) Coordinate System
         * @param corners An array of 2D geographic points (java.awt.geom.Point2D)
         *
         * @return Sector
         *
         * @throws IllegalArgumentException if any of the parameters are null
         * @throws WWRuntimeException       in case of geo-transformation errors
         */
        public static Sector calcBoundingSector(SpatialReference srs, java.awt.geom.Point2D[] corners)
            throws IllegalArgumentException, WWRuntimeException
        {
            if (null == srs)
            {
                String message = Logging.getMessage("nullValue.SpatialReferenceIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (null == corners)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            Sector bbox = null;
            try
            {
                double minx = Double.MAX_VALUE, maxx = -Double.MAX_VALUE;
                double miny = Double.MAX_VALUE, maxy = -Double.MAX_VALUE;

                CoordinateTransformation ct = new CoordinateTransformation(srs, GDALUtils.createGeographicSRS());

                for (java.awt.geom.Point2D corner : corners)
                {
                    double[] point = ct.TransformPoint(corner.getX(), corner.getY());

                    if (null != point)
                    {
                        minx = (point[0] < minx) ? point[0] : minx;
                        maxx = (point[0] > maxx) ? point[0] : maxx;
                        miny = (point[1] < miny) ? point[1] : miny;
                        maxy = (point[1] > maxy) ? point[1] : maxy;
                    }
                }
                bbox = Sector.fromDegrees(miny, maxy, minx, maxx);
            }
            catch (Throwable t)
            {
                String error = GDALUtils.getErrorMessage();
                String reason = (null != error && error.length() > 0) ? error : t.getMessage();
                String message = Logging.getMessage("generic.ExceptionWhileTransformation", reason);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }
            return bbox;
        }

        protected Area(SpatialReference srs, double minY, double maxY, double minX, double maxX)
            throws IllegalArgumentException
        {
            if (null == srs)
            {
                String message = Logging.getMessage("nullValue.SpatialReferenceIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (srs.IsGeographic() == 0 && srs.IsProjected() == 0)
            {
                String message = Logging.getMessage("generic.UnexpectedCoordinateSystem", srs.ExportToWkt());
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.srs = srs.Clone();
            this.makeCorners(minY, maxY, minX, maxX);

            if (this.srs.IsGeographic() > 0)
            {
                this.bbox = Sector.fromDegrees(minY, maxY, minX, maxX);
            }
            else
                this.bbox = calcBoundingSector(this.srs, this.corners);
        }

        protected void makeCorners(double minY, double maxY, double minX, double maxX)
        {
            double xWest, yNorth, xEast, ySouth;

            xWest = Math.min(minX, maxX);
            xEast = Math.max(minX, maxX);
            ySouth = Math.min(minY, maxY);
            yNorth = Math.max(minY, maxY);

            this.corners = new java.awt.geom.Point2D[] {
                new java.awt.geom.Point2D.Double(xWest, ySouth), // SW corner
                new java.awt.geom.Point2D.Double(xEast, ySouth), // SE corner
                new java.awt.geom.Point2D.Double(xEast, yNorth), // NE corner
                new java.awt.geom.Point2D.Double(xWest, yNorth)  // NW corner
            };
        }

        public Area(SpatialReference srs, Sector sector) throws IllegalArgumentException
        {
            if (null == sector)
            {
                String message = Logging.getMessage("nullValue.SectorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (null == srs)
            {
                String message = Logging.getMessage("nullValue.SpatialReferenceIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (srs.IsGeographic() == 0 && srs.IsProjected() == 0)
            {
                String message = Logging.getMessage("generic.UnexpectedCoordinateSystem", srs.ExportToWkt());
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.srs = srs;
            this.bbox = sector;

            SpatialReference geodetic = GDALUtils.createGeographicSRS();
            CoordinateTransformation ct = new CoordinateTransformation(geodetic, this.srs);

            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (LatLon ll : sector.getCorners())
            {
                double[] point = ct.TransformPoint(ll.getLongitude().degrees, ll.getLatitude().degrees);
                if (null != point)
                {
                    minX = (point[0] < minX) ? point[0] : minX;
                    maxX = (point[0] > maxX) ? point[0] : maxX;
                    minY = (point[1] < minY) ? point[1] : minY;
                    maxY = (point[1] > maxY) ? point[1] : maxY;
                }
            }
            this.makeCorners(minY, maxY, minX, maxX);
        }

        public boolean isGeographic()
        {
            return (null != this.srs && this.srs.IsGeographic() > 0);
        }

        public boolean isProjected()
        {
            return (null != this.srs && this.srs.IsProjected() > 0);
        }

        public SpatialReference getSpatialReference()
        {
            return this.srs.Clone();
        }

        public Sector getSector()
        {
            return this.bbox;
        }

        public Area getBoundingArea()
        {
            return new Area(this.srs.Clone(), this.getMinY(), this.getMaxY(), this.getMinX(), this.getMaxX());
        }

        public java.awt.geom.Point2D[] getCorners()
        {
            return this.corners.clone();
        }

        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer("Area { ");
            for (java.awt.geom.Point2D corner : this.corners)
            {
                sb.append('(').append(corner.getX()).append(',').append(corner.getY()).append(") ");
            }
            sb.append('}');
            return sb.toString();
        }

        public double getMinX()
        {
            return GDAL.getMinX(this.corners);
        }

        public double getMaxX()
        {
            return GDAL.getMaxX(this.corners);
        }

        public double getMinY()
        {
            return GDAL.getMinY(this.corners);
        }

        public double getMaxY()
        {
            return GDAL.getMaxY(this.corners);
        }

        public Area intersection(Sector sector) throws WWRuntimeException
        {
            return this.intersection(new Area(this.srs, sector));
        }

        public Area intersection(Area that) throws WWRuntimeException
        {
            if (null == that)
                return null;

            if (this.srs.IsSame(that.getSpatialReference()) == 0)
            {
                String message = Logging.getMessage("generic.SectorMismatch", this, that);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            double minY = Math.max(this.getMinY(), that.getMinY());
            double maxY = Math.min(this.getMaxY(), that.getMaxY());
            if (minY > maxY)
                return null;

            double minX = Math.max(this.getMinX(), that.getMinX());
            double maxX = Math.min(this.getMaxX(), that.getMaxX());
            if (minX > maxX)
                return null;

            return new Area(this.srs.Clone(), minY, maxY, minX, maxX);
        }

        public boolean contains(Area that) throws WWRuntimeException
        {
            if (null == that)
                return false;

            if (this.srs.IsSame(that.getSpatialReference()) == 0)
            {
                String message = Logging.getMessage("generic.SectorMismatch", this, that);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            if (that.getMinX() < this.getMinX())
                return false;
            if (that.getMaxX() > this.getMaxX())
                return false;
            if (that.getMinY() < this.getMinY())
                return false;
            if (that.getMaxY() > this.getMaxY())
                return false;

            return true;
        }

        public java.awt.geom.AffineTransform computeGeoToRasterTransform(int width, int height)
        {
            double ty = -this.getMaxY();
            double tx = -this.getMinX();

            double deltaX = this.getMaxX() - this.getMinX();
            double deltaY = this.getMaxY() - this.getMinY();

            if (deltaX == 0d || deltaY == 0d)
                return null;

            double sy = -(height / deltaY);
            double sx = (width / deltaX);

            java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
            transform.scale(sx, sy);
            transform.translate(tx, ty);
            return transform;
        }

        public java.awt.Rectangle computeClipRect(int rasterWidth, int rasterHeight, Area clipArea)
            throws IllegalArgumentException
        {
            if (null == clipArea)
            {
                String message = Logging.getMessage("nullValue.AreaIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            java.awt.geom.AffineTransform geoToRaster =
                this.computeGeoToRasterTransform(rasterWidth, rasterHeight);

            java.awt.geom.Point2D geoPoint = new java.awt.geom.Point2D.Double();
            java.awt.geom.Point2D ul = new java.awt.geom.Point2D.Double();
            java.awt.geom.Point2D lr = new java.awt.geom.Point2D.Double();

            geoPoint.setLocation(clipArea.getMinX(), clipArea.getMaxY());
            geoToRaster.transform(geoPoint, ul);

            geoPoint.setLocation(clipArea.getMaxX(), clipArea.getMinY());
            geoToRaster.transform(geoPoint, lr);

            int x = (int) Math.floor(ul.getX());
            int y = (int) Math.floor(ul.getY());
            int width = (int) Math.ceil(lr.getX() - ul.getX());
            int height = (int) Math.ceil(lr.getY() - ul.getY());

            return new java.awt.Rectangle(x, y, width, height);
        }
    }

    public static AffineTransform getAffineTransform(Dataset ds) throws IllegalArgumentException
    {
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] gt = new double[6];
        ds.GetGeoTransform(gt);

        return new AffineTransform(
            gt[GDAL.GT_1_PIXEL_WIDTH],
            gt[GDAL.GT_4_ROTATION_Y], gt[GDAL.GT_2_ROTATION_X],
            ((gt[GDAL.GT_5_PIXEL_HEIGHT] > 0) ? -gt[GDAL.GT_5_PIXEL_HEIGHT] : gt[GDAL.GT_5_PIXEL_HEIGHT]),
            gt[GDAL.GT_0_ORIGIN_LON], gt[GDAL.GT_3_ORIGIN_LAT]);
    }

    public static AffineTransform getAffineTransform(Dataset ds, int newWidth, int newHeight)
        throws IllegalArgumentException
    {
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (newWidth <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", newWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (newHeight <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", newHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        AffineTransform atx = getAffineTransform(ds);
        if (null != atx)
        {
            double sy = ((double) ds.getRasterYSize() / (double) newHeight);
            double sx = ((double) ds.getRasterXSize() / (double) newWidth);
            atx.scale(sx, sy);
        }

        return atx;

//        double[] gt = new double[6];
//        ds.GetGeoTransform( gt );
//
//        double dx = gt[GDAL.GT_1_PIXEL_WIDTH] * ((double)ds.getRasterXSize()) / ((double)newWidth);
//        double dy = gt[GDAL.GT_5_PIXEL_HEIGHT] * ((double)ds.getRasterYSize()) / ((double)newHeight);
//        dy = ( dy > 0d ) ? -dy : dy; // make sure DY is always negative
//
//        return new AffineTransform(
//            dx,  gt[GDAL.GT_4_ROTATION_Y], gt[GDAL.GT_2_ROTATION_X],
//            dy, gt[GDAL.GT_0_ORIGIN_LON], gt[GDAL.GT_3_ORIGIN_LAT] );
    }

    public static double getMinX(java.awt.geom.Point2D[] points) throws IllegalArgumentException
    {
        if (null == points)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double min = Double.MAX_VALUE;
        for (java.awt.geom.Point2D point : points)
        {
            min = (point.getX() < min) ? point.getX() : min;
        }

        return min;
    }

    public static double getMaxX(java.awt.geom.Point2D[] points) throws IllegalArgumentException
    {
        if (null == points)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double max = -Double.MAX_VALUE;
        for (java.awt.geom.Point2D point : points)
        {
            max = (point.getX() > max) ? point.getX() : max;
        }

        return max;
    }

    public static double getMinY(java.awt.geom.Point2D[] points) throws IllegalArgumentException
    {
        if (null == points)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double min = Double.MAX_VALUE;
        for (java.awt.geom.Point2D point : points)
        {
            min = (point.getY() < min) ? point.getY() : min;
        }

        return min;
    }

    public static double getMaxY(java.awt.geom.Point2D[] points) throws IllegalArgumentException
    {
        if (null == points)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double max = -Double.MAX_VALUE;
        for (java.awt.geom.Point2D point : points)
        {
            max = (point.getY() > max) ? point.getY() : max;
        }

        return max;
    }
}
