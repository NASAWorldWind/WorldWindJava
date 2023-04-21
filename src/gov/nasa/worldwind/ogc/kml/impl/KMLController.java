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

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.render.*;

/**
 * Executes the mapping from KML to WorldWind. Traverses a parsed KML document and creates the appropriate WorldWind
 * object to represent the KML.
 *
 * @author tag
 * @version $Id: KMLController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLController extends WWObjectImpl implements PreRenderable, Renderable, MessageListener
{
    protected KMLRoot kmlRoot;
    protected KMLTraversalContext tc;

    public KMLController(KMLRoot root)
    {
        this.setKmlRoot(root);
        this.setTraversalContext(new KMLTraversalContext());
    }

    public KMLRoot getKmlRoot()
    {
        return this.kmlRoot;
    }

    public void setKmlRoot(KMLRoot kmlRoot)
    {
        // Stop listening for property changes in previous KMLRoot
        KMLRoot oldRoot = this.getKmlRoot();
        if (oldRoot != null)
            oldRoot.removePropertyChangeListener(this);

        this.kmlRoot = kmlRoot;

        if (kmlRoot != null)
            kmlRoot.addPropertyChangeListener(this);
    }

    public void setTraversalContext(KMLTraversalContext tc)
    {
        this.tc = tc;
    }

    public KMLTraversalContext getTraversalContext()
    {
        return this.tc;
    }

    public void preRender(DrawContext dc)
    {
        this.initializeTraversalContext(this.getTraversalContext());
        this.kmlRoot.preRender(this.getTraversalContext(), dc);
    }

    public void render(DrawContext dc)
    {
        this.initializeTraversalContext(this.getTraversalContext());
        this.kmlRoot.render(this.getTraversalContext(), dc);
    }

    /**
     * Initializes this KML controller's traversal context to its default state. A KML traversal context must be
     * initialized prior to use during preRendering or rendering, to ensure that state from the previous pass does not
     * affect the current pass.
     *
     * @param tc the KML traversal context to initialize.
     */
    protected void initializeTraversalContext(KMLTraversalContext tc)
    {
        tc.initialize();
        tc.setDetailHint(this.kmlRoot.getDetailHint());
    }

    @Override
    public void onMessage(Message msg)
    {
        if (this.kmlRoot != null)
            this.kmlRoot.onMessage(msg);
    }
}
