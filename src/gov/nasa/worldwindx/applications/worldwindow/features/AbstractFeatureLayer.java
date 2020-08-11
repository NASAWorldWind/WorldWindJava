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
import gov.nasa.worldwindx.applications.worldwindow.core.*;

import java.awt.event.*;

/**
 * @author tag
 * @version $Id: AbstractFeatureLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractFeatureLayer extends AbstractFeature
{
    protected Layer layer;
    private boolean twoState = false;

    protected abstract Layer doAddLayer();

    protected AbstractFeatureLayer(String featureTitle, String featureID, String iconFilePath, boolean twoState,
        Registry registry)
    {
        super(featureTitle, featureID, iconFilePath, registry);

        this.twoState = twoState;
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.layer = this.doAddLayer();
    }

    @Override
    public void setEnabled(boolean tf)
    {
        super.setEnabled(tf);

        if (this.layer != null)
            this.layer.setEnabled(isEnabled());
    }

    public boolean isOn()
    {
        return this.layer != null && this.isEnabled() && this.layer.isEnabled()
            && this.controller.getActiveLayers().contains(this.layer);
    }

    @Override
    public boolean isTwoState()
    {
        return this.twoState;
    }

    @Override
    public void turnOn(boolean tf)
    {
        boolean currentState = this.isOn();

        layer.setEnabled(tf);

        this.firePropertyChange(Constants.ON_STATE, currentState, layer.isEnabled());

        controller.redraw();
    }

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        this.turnOn(this.layer == null || !layer.isEnabled());
        controller.redraw();
    }
}
