/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
