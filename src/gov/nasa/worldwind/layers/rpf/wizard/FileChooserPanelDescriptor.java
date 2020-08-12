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
package gov.nasa.worldwind.layers.rpf.wizard;

import gov.nasa.worldwind.util.wizard.DefaultPanelDescriptor;
import gov.nasa.worldwind.util.wizard.Wizard;
import gov.nasa.worldwind.util.wizard.WizardModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * @author dcollins
 * @version $Id: FileChooserPanelDescriptor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FileChooserPanelDescriptor extends DefaultPanelDescriptor
{
    private FileChooserPanel panelComponent;
    public static final String IDENTIFIER = "gov.nasa.worldwind.rpf.wizard.FileChooserPanel";

    public FileChooserPanelDescriptor()
    {
        this.panelComponent = new FileChooserPanel();
        this.panelComponent.addPropertyChangeListener(new PropertyEvents());
        setPanelIdentifier(IDENTIFIER);
        setPanelComponent(this.panelComponent);
    }

    public Object getBackPanelDescriptor()
    {
        return null;
    }

    public Object getNextPanelDescriptor()
    {
        Object nextDescriptor;
        if (!RPFWizardUtil.isFileListCurrent(getWizardModel()))
            nextDescriptor = FileSearchPanelDescriptor.IDENTIFIER;
        else
            nextDescriptor = DataChooserPanelDescriptor.IDENTIFIER;
        return nextDescriptor;
    }

    public void aboutToDisplayPanel()
    {
        this.panelComponent.setTitle(RPFWizardUtil.makeLarger("Choose Folder to Search"));
        this.panelComponent.setDescription("<html><br>Folder to search...</html>");
        setNextButtonAccordingToSelectedFile();
    }

    private void setNextButtonAccordingToSelectedFile()
    {
        Wizard wizard = getWizard();
        if (wizard != null)
        {
            File file = RPFWizardUtil.getSelectedFile(wizard.getModel());
            wizard.setNextButtonEnabled(file != null && file.exists());
            wizard.giveFocusToNextButton();
        }
    }

    private void selectedFileChanged(Object newValue)
    {
        WizardModel model = getWizardModel();
        if (model != null && newValue != null && newValue instanceof File)
        {
            RPFWizardUtil.setSelectedFile(model, (File) newValue);
        }
        setNextButtonAccordingToSelectedFile();
    }

    private class PropertyEvents implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt != null && evt.getPropertyName() != null) {
                String propertyName = evt.getPropertyName();
                if (propertyName.equals("selectedFile")) {
                    selectedFileChanged(evt.getNewValue());           
                }
            }
        }
    }
}
