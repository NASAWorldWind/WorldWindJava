/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.nitfs;

/**
 * @author Lado Garakanidze
 * @version $Id: NITFSFileHeader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NITFSFileHeader
{
    private String  headerID;
    private String  version;
    private String  specialType;
    private int     headerLength;
    private int     fileLength;
    private boolean isVersion0210;
    private short   complexityLevel ;
    private String  originationStationId;
    private String  dateTime;
    private String  title   ;

    private String  FSCLAS  ;
    private String  FSCLSY  ;
    private String  FSCODE  ;
    private String  FSCTLH  ;
    private String  FSREL   ;
    private String  FSDCTP  ;
    private String  FSDCDT  ;
    private String  FSDCXM  ;
    private String  FSDG    ;
    private String  FSDGDT  ;
    private String  FSCLTX  ;
    private String  FSCATP  ;
    private String  FSCAUT  ;
    private String  FSCRSN  ;
    private String  FSSRDT  ;
    private String  FSCTLN  ;
    private String  FSDWNG  ;
    private String  FSDEVT  ;
    private String  FSCOP   ;
    private String  FSCPYS  ;
    private String  ENCRYP  ;
    private String  FBKGC   ;
    private String  ONAME   ;
    private String  OPHONE  ;

    public NITFSFileHeader(java.nio.ByteBuffer buffer)
    {
        parseFileHeaderInfo(buffer);
    }

    private void parseFileHeaderInfo(java.nio.ByteBuffer buffer)
    {
        this.headerID = NITFSUtil.getString(buffer, 0, 4);
        this.version =  NITFSUtil.getString(buffer, 5);
        this.isVersion0210 = "02.10".equals(version);
        this.complexityLevel = NITFSUtil.getShortNumeric(buffer, 2);
        this.specialType = NITFSUtil.getString(buffer, 4);                                   // offset  11, size 4
        this.originationStationId = NITFSUtil.getString(buffer, 10);                         // offset  15, size 10
        this.dateTime = NITFSUtil.getString(buffer, 14);                                     // offset  25, size 14
        this.title   = NITFSUtil.getString(buffer, 80);                                      // offset  39, size 80

        this.FSCLAS  = NITFSUtil.getString(buffer, 1);                                       // offset 119, size 1
        this.FSCLSY  = (isVersion0210 ? NITFSUtil.getString(buffer, 2) : "");  // offset 120, size 2
        this.FSCODE  = NITFSUtil.getString(buffer, isVersion0210 ? 11 : 40);
        this.FSCTLH  = NITFSUtil.getString(buffer, isVersion0210 ? 2 : 40);
        this.FSREL   = NITFSUtil.getString(buffer, isVersion0210 ? 20 : 40);

        this.FSDCTP  = (isVersion0210 ? NITFSUtil.getString(buffer, 2) : "");
        this.FSDCDT  = (isVersion0210 ? NITFSUtil.getString(buffer,  8) : "");     // offset 157/
        this.FSDCXM  = (isVersion0210 ? NITFSUtil.getString(buffer,  4) : "");     // offset 165/
        this.FSDG    = (isVersion0210 ? NITFSUtil.getString(buffer,  1) : "");     // offset 169/
        this.FSDGDT  = (isVersion0210 ? NITFSUtil.getString(buffer,  8) : "");     // oofset 170/
        this.FSCLTX  = (isVersion0210 ? NITFSUtil.getString(buffer, 43) : "");     // offset 178/
        this.FSCATP  = (isVersion0210 ? NITFSUtil.getString(buffer,  1) : "");     // offset 221/

        this.FSCAUT  = NITFSUtil.getString(buffer, isVersion0210 ? 40 : 20);                     // offset 222/240

        this.FSCRSN  = (isVersion0210 ? NITFSUtil.getString(buffer,  1) : "");     // offset 262/
        this.FSSRDT  = (isVersion0210 ? NITFSUtil.getString(buffer, 8) : "");      // offset 263/
        this.FSCTLN  = NITFSUtil.getString(buffer, isVersion0210 ? 15 : 20);                     // offset 271/260
        this.FSDWNG  = (isVersion0210) ? "" : NITFSUtil.getString(buffer, 6);      // offset    /280

        this.FSDEVT  = (!isVersion0210 && "999998".equals(FSDWNG))                   // offset    /286
                            ? NITFSUtil.getString(buffer, 40) : "";

        this.FSCOP  = NITFSUtil.getString(buffer, 5);                                           // offset 286/+40
        this.FSCPYS = NITFSUtil.getString(buffer, 5);                                           // offset 291/+40
        this.ENCRYP = NITFSUtil.getString(buffer, 1);                                           // offset 296/+40

        this.FBKGC  = (isVersion0210 ? NITFSUtil.getString(buffer, 297, 3) : ""); // offset 297/
        this.ONAME  = NITFSUtil.getString(buffer, isVersion0210 ? 24 : 27);                     // offset 300/297(+40)
        this.OPHONE = NITFSUtil.getString(buffer, 18);                                          // offset 324(+40)

        this.fileLength = NITFSUtil.getNumeric(buffer, 12);                                   // offset 342(+40)
        this.headerLength = NITFSUtil.getNumeric(buffer, 6);                                  // offset 352(+40)
   }
    
    public String getHeaderID()
    {
        return this.headerID;
    }

    public String getVersion()
    {
        return this.version;
    }

    public boolean isVersion0210()
    {
        return this.isVersion0210;
    }

    public short getComplexityLevel()
    {
        return this.complexityLevel;
    }

    public String getSpecialType()
    {
        return this.specialType;
    }

    public String getOriginationStationId()
    {
        return this.originationStationId;
    }

    public String getDateTime()
    {
        return this.dateTime;
    }

    public String getTitle()
    {
        return this.title;
    }

    public int getHeaderLength()
    {
        return this.headerLength;
    }

    public String getFSCLAS()
    {
        return this.FSCLAS;
    }

    public String getFSCLSY()
    {
        return this.FSCLSY;
    }

    public String getFSCODE()
    {
        return this.FSCODE;
    }

    public String getFSCTLH()
    {
        return this.FSCTLH;
    }

    public String getFSREL()
    {
        return this.FSREL;
    }

    public String getFSDCTP()
    {
        return this.FSDCTP;
    }

    public String getFSDCDT()
    {
        return this.FSDCDT;
    }

    public String getFSDCXM()
    {
        return this.FSDCXM;
    }

    public String getFSDG()
    {
        return this.FSDG;
    }

    public String getFSDGDT()
    {
        return this.FSDGDT;
    }

    public String getFSCLTX()
    {
        return this.FSCLTX;
    }

    public String getFSCATP()
    {
        return this.FSCATP;
    }

    public String getFSCAUT()
    {
        return this.FSCAUT;
    }

    public String getFSCRSN()
    {
        return this.FSCRSN;
    }

    public String getFSSRDT()
    {
        return this.FSSRDT;
    }

    public String getFSCTLN()
    {
        return this.FSCTLN;
    }

    public String getFSDWNG()
    {
        return this.FSDWNG;
    }

    public String getFSDEVT()
    {
        return this.FSDEVT;
    }

    public String getFSCOP()
    {
        return this.FSCOP;
    }

    public String getFSCPYS()
    {
        return this.FSCPYS;
    }

    public String getENCRYP()
    {
        return this.ENCRYP;
    }

    public String getFBKGC()
    {
        return this.FBKGC;
    }

    public String getONAME()
    {
        return this.ONAME;
    }

    public String getOPHONE()
    {
        return this.OPHONE;
    }

    public int getFileLength()
    {
        return this.fileLength;
    }
}