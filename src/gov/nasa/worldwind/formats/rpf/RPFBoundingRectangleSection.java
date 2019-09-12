/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.formats.nitfs.NITFSUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author brownrigg
 * @version $Id: RPFBoundingRectangleSection.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class RPFBoundingRectangleSection {

    public RPFBoundingRectangleSection(ByteBuffer buffer) {
        // [ bounding rectangle section subheader ]
        this.tableOffset = NITFSUtil.getUInt(buffer);
        this.numberOfRecords = NITFSUtil.getUShort(buffer);
        this.recordLength = NITFSUtil.getUShort(buffer);

        parseBoundsRecords(buffer);
    }

    public List<RPFBoundingRectangleRecord> getBoundingRecords() {
        return bndRectRecords;
    }

    private void parseBoundsRecords(ByteBuffer buffer) {
        for (int i=0; i<this.numberOfRecords; i++)
            bndRectRecords.add(new RPFBoundingRectangleRecord(buffer));
    }

    public class RPFBoundingRectangleRecord {

        public double getMinLon() {
            return (this.ulLon < this.llLon) ? this.ulLon : this.llLon;
        }

        public double getMinLat() {
            return (this.llLat < this.lrLat) ? this.llLat : this.lrLat;
        }

        public double getMaxLon() {
            return (this.urLon > this.lrLon) ? this.urLon : this.lrLon;
        }

        public double getMaxLat() {
            return (this.ulLat > this.urLat) ? this.ulLat : this.urLat;
        }

        public RPFBoundingRectangleRecord(ByteBuffer buffer) {
            this.dataType = NITFSUtil.getString(buffer, 5);
            this.compressionRatio = NITFSUtil.getString(buffer, 5);
            this.scale = NITFSUtil.getString(buffer, 12);
            this.zone = NITFSUtil.getString(buffer, 1);
            this.producer = NITFSUtil.getString(buffer, 5);
            this.ulLat = buffer.getDouble();
            this.ulLon = buffer.getDouble();
            this.llLat = buffer.getDouble();
            this.llLon = buffer.getDouble();
            this.urLat = buffer.getDouble();
            this.urLon = buffer.getDouble();
            this.lrLat = buffer.getDouble();
            this.lrLon = buffer.getDouble();
            this.nsRes = buffer.getDouble();
            this.ewRes = buffer.getDouble();
            this.latInterval = buffer.getDouble();
            this.lonInterval = buffer.getDouble();
            this.numFramesNS = NITFSUtil.getUInt(buffer);
            this.numFramesEW = NITFSUtil.getUInt(buffer);
        }

        private String dataType;
        private String compressionRatio;
        private String scale;
        private String zone;
        private String producer;
        private double ulLat;
        private double ulLon;
        private double llLat;
        private double llLon;
        private double urLat;
        private double urLon;
        private double lrLat;
        private double lrLon;
        private double nsRes;
        private double ewRes;
        private double latInterval;
        private double lonInterval;
        private long   numFramesNS;
        private long   numFramesEW;
    }

    private long tableOffset;
    private int numberOfRecords;
    private int recordLength;
    private ArrayList<RPFBoundingRectangleRecord> bndRectRecords =
            new ArrayList<RPFBoundingRectangleRecord>();
}
