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

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.applications.worldwindow.core.Registry;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.LayerPath;

/**
 * @author tag
 * @version $Id: AbstractOnDemandLayerFeature.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractOnDemandLayerFeature extends AbstractFeature
{
    protected String group;
    protected Layer layer;
    protected boolean on = false;

    protected abstract Layer createLayer();

    public AbstractOnDemandLayerFeature(String s, String featureID, String iconPath, String group, Registry registry)
    {
        super(s, featureID, iconPath, registry);

        this.group = group;
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    @Override
    public boolean isOn()
    {
        return this.on;
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (tf == this.on)
            return;

        if (tf && this.layer == null)
            this.layer = this.createLayer();

        if (this.layer == null)
            return;

        if (tf)
        {
            LayerPath path = new LayerPath(this.group);
            this.addLayer(path);
            this.controller.getLayerManager().selectLayer(this.layer, true);
        }
        else
        {
            this.removeLayer();
        }

        this.on = tf;
    }

    protected void addLayer(LayerPath path)
    {
        this.controller.getLayerManager().addLayer(this.layer, path);
    }

    protected void removeLayer()
    {
        this.controller.getLayerManager().removeLayer(this.layer);
    }
}
