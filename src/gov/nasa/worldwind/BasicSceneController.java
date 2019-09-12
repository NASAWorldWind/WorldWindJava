/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.SectorGeometryList;

/**
 * @author Tom Gaskins
 * @version $Id: BasicSceneController.java 2249 2014-08-21 20:13:30Z dcollins $
 */
public class BasicSceneController extends AbstractSceneController
{
    SectorGeometryList sglC, sglL, sglR;
    Sector visibleSectorC, visibleSectorL, visibleSectorR;

    public void doRepaint(DrawContext dc)
    {
        this.initializeFrame(dc);
        try
        {
            if (dc.getGlobe() instanceof Globe2D && ((Globe2D)dc.getGlobe()).isContinuous())
                this.do2DContiguousRepaint(dc);
            else
                this.doNormalRepaint(dc);
        }
        finally
        {
            this.finalizeFrame(dc);
        }
    }

    protected void doNormalRepaint(DrawContext dc)
    {
        this.applyView(dc);
        this.createPickFrustum(dc);
        this.createTerrain(dc);
        this.preRender(dc);
        this.clearFrame(dc);
        this.pick(dc);
        this.clearFrame(dc);
        this.draw(dc);
    }

    protected void do2DContiguousRepaint(DrawContext dc)
    {
        ((Globe2D) dc.getGlobe()).setOffset(0);

        this.applyView(dc);
        this.createPickFrustum(dc);
        this.createTerrain2DContinuous(dc);
        this.preRender2DContiguous(dc);
        this.clearFrame(dc);
        this.pick2DContiguous(dc);
        this.clearFrame(dc);
        this.draw2DContiguous(dc);
    }

    protected void makeCurrent(DrawContext dc, int offset)
    {
        ((Globe2D) dc.getGlobe()).setOffset(offset);

        switch (offset)
        {
            case -1:
                dc.setSurfaceGeometry(this.sglL);
                dc.setVisibleSector(this.visibleSectorL);
                break;
            case 0:
                dc.setSurfaceGeometry(this.sglC);
                dc.setVisibleSector(this.visibleSectorC);
                break;
            case 1:
                dc.setSurfaceGeometry(this.sglR);
                dc.setVisibleSector(this.visibleSectorR);
                break;
        }
    }

    protected void createTerrain2DContinuous(DrawContext dc)
    {
        this.sglC = null;
        this.visibleSectorC = null;
        ((Globe2D) dc.getGlobe()).setOffset(0);
        if (dc.getGlobe().intersects(dc.getView().getFrustumInModelCoordinates()))
        {
            this.sglC = dc.getModel().getGlobe().tessellate(dc);
            this.visibleSectorC = this.sglC.getSector();
        }

        this.sglR = null;
        this.visibleSectorR = null;
        ((Globe2D) dc.getGlobe()).setOffset(1);
        if (dc.getGlobe().intersects(dc.getView().getFrustumInModelCoordinates()))
        {
            this.sglR = dc.getModel().getGlobe().tessellate(dc);
            this.visibleSectorR = this.sglR.getSector();
        }

        this.sglL = null;
        this.visibleSectorL = null;
        ((Globe2D) dc.getGlobe()).setOffset(-1);
        if (dc.getGlobe().intersects(dc.getView().getFrustumInModelCoordinates()))
        {
            this.sglL = dc.getModel().getGlobe().tessellate(dc);
            this.visibleSectorL = this.sglL.getSector();
        }
    }

    protected void draw2DContiguous(DrawContext dc)
    {
        String drawing = "";
        if (this.sglC != null)
        {
            drawing += " 0 ";
            this.makeCurrent(dc, 0);
            this.setDeferOrderedRendering(this.sglL != null || this.sglR != null);
            this.draw(dc);
        }

        if (this.sglR != null)
        {
            drawing += " 1 ";
            this.makeCurrent(dc, 1);
            this.setDeferOrderedRendering(this.sglL != null);
            this.draw(dc);
        }

        this.setDeferOrderedRendering(false);

        if (this.sglL != null)
        {
            drawing += " -1 ";
            this.makeCurrent(dc, -1);
            this.draw(dc);
        }
//        System.out.println("DRAWING " + drawing);
    }

    protected void preRender2DContiguous(DrawContext dc)
    {
        if (this.sglC != null)
        {
            this.makeCurrent(dc, 0);
            this.preRender(dc);
        }

        if (this.sglR != null)
        {
            this.makeCurrent(dc, 1);
            this.preRender(dc);
        }

        if (this.sglL != null)
        {
            this.makeCurrent(dc, -1);
            this.preRender(dc);
        }
    }

    protected void pick2DContiguous(DrawContext dc)
    {
        if (this.sglC != null)
        {
            this.makeCurrent(dc, 0);
            this.setDeferOrderedRendering(this.sglL != null || this.sglR != null);
            this.pick(dc);
        }

        if (this.sglR != null)
        {
            this.makeCurrent(dc, 1);
            this.setDeferOrderedRendering(this.sglL != null);
            this.pick(dc);
        }

        this.setDeferOrderedRendering(false);

        if (this.sglL != null)
        {
            this.makeCurrent(dc, -1);
            this.pick(dc);
        }
    }
}
