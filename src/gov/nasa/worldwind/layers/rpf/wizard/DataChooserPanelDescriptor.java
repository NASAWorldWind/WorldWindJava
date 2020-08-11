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
import java.util.Collection;
import java.io.File;

/**
 * @author dcollins
 * @version $Id: DataChooserPanelDescriptor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DataChooserPanelDescriptor extends DefaultPanelDescriptor
{
    private DataChooserPanel panelComponent;
    private PropertyEvents propertyEvents;
    public static final String IDENTIFIER = "gov.nasa.worldwind.rpf.wizard.DataChooserPanel";

    public DataChooserPanelDescriptor()
    {
        this.panelComponent = new DataChooserPanel();
        this.propertyEvents = new PropertyEvents();
        this.panelComponent.addPropertyChangeListener(this.propertyEvents);
        setPanelIdentifier(IDENTIFIER);
        setPanelComponent(this.panelComponent);
    }

    public Object getBackPanelDescriptor()
    {
        return FileChooserPanelDescriptor.IDENTIFIER;
    }

    public Object getNextPanelDescriptor()
    {
        return PreprocessPanelDescriptor.IDENTIFIER;
    }

    public void registerPanel(Wizard wizard)
    {
        WizardModel oldWizardModel = getWizardModel();
        if (oldWizardModel != null)
            oldWizardModel.removePropertyChangeListener(this.propertyEvents);

        super.registerPanel(wizard);

        WizardModel newWizardModel = getWizardModel();
        if (newWizardModel != null)
            newWizardModel.addPropertyChangeListener(this.propertyEvents);
    }

    public void aboutToDisplayPanel()
    {
        setNextButtonAccordingToSelection();
    }

    private void setNextButtonAccordingToSelection()
    {
        Wizard wizard = getWizard();
        if (wizard != null)
        {
            boolean anySelected = false;
            Collection<FileSet> fileSetList = RPFWizardUtil.getFileSetList(wizard.getModel());
            if (fileSetList != null && fileSetList.size() > 0)
            {
                for (FileSet set : fileSetList)
                    anySelected |= set.isSelected();
            }
            wizard.setNextButtonEnabled(anySelected);
            wizard.giveFocusToNextButton();
        }
    }

    private void fileSetListChanged()
    {
        WizardModel model = getWizardModel();
        if (model != null)
        {
            Collection<FileSet> fileSetList = RPFWizardUtil.getFileSetList(model);
            updatePanelTitle(fileSetList);
            updatePanelData(fileSetList);
            updatePanelDataDescription(fileSetList);
        }
    }

    private void fileSetSelectionChanged()
    {
        setNextButtonAccordingToSelection();

        WizardModel model = getWizardModel();
        if (model != null)
        {
            Collection<FileSet> fileSetList = RPFWizardUtil.getFileSetList(model);
            updatePanelDataDescription(fileSetList);
        }
    }

    private class PropertyEvents implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt != null && evt.getPropertyName() != null) {
                String propertyName = evt.getPropertyName();
                if (propertyName.equals(RPFWizardUtil.FILE_SET_LIST)) {
                    fileSetListChanged();
                } else if (propertyName.equals(FileSet.SELECTED)) {
                    fileSetSelectionChanged();
                }
            }
        }
    }

    private void updatePanelTitle(Collection<FileSet> fileSetList)
    {
        if (fileSetList != null && fileSetList.size() > 0)
        {
            this.panelComponent.setTitle(RPFWizardUtil.makeLarger("Select Imagery to Import"));
        }
        else
        {
            this.panelComponent.setTitle(RPFWizardUtil.makeLarger("No Imagery Found"));
        }
    }

    private void updatePanelData(Collection<FileSet> fileSetList)
    {
        this.panelComponent.setFileSetList(fileSetList);
    }

    private void updatePanelDataDescription(Collection<FileSet> fileSetList)
    {
        int totalFiles = 0;
        int selectedFiles = 0;
        if (fileSetList != null && fileSetList.size() > 0)
        {
            for (FileSet set : fileSetList)
            {
                if (set != null)
                {
                    int count = set.getFileCount();
                    totalFiles += count;
                    if (set.isSelected())
                        selectedFiles += count;
                }
            }
        }

        if (totalFiles > 0)
        {
            StringBuilder sb = new StringBuilder();
            if (selectedFiles > 0)
            {
                long WAVELET_SIZE_EST = 262160; // TODO: compute this value
                long WAVELET_TIME_EST = 200;    // TODO: compute this value
                long estimatedBytes = selectedFiles * WAVELET_SIZE_EST;
                long estimatedMillis = selectedFiles * WAVELET_TIME_EST;
                sb.append("Selected files: ");
                sb.append(String.format("%,d", selectedFiles));
                if (estimatedBytes > 0)
                {
                    SizeFormatter sf = new SizeFormatter();
                    if (sb.length() > 0)
                        sb.append(" - ");
                    sb.append("Disk space required: ~");
                    sb.append(sf.formatEstimate(estimatedBytes));
                }
                if (estimatedMillis > 0)
                {
                    TimeFormatter tf = new TimeFormatter();
                    if (sb.length() > 0)
                        sb.append(" - ");
                    sb.append("Processing time: ");
                    sb.append(tf.formatEstimate(estimatedMillis));
                }
            }
            else
            {
                sb.append("No files selected");
            }
            this.panelComponent.setDataDescription(RPFWizardUtil.makeSmaller(sb.toString()));
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append("No Imagery");
            WizardModel model = getWizardModel();
            if (model != null)
            {
                File selectedFile = RPFWizardUtil.getSelectedFile(model);
                if (selectedFile != null)
                    sb.append(" in \'").append(selectedFile.getAbsolutePath()).append(File.separator).append("\'");
            }
            this.panelComponent.setDataDescription(RPFWizardUtil.makeBold(sb.toString()));
        }
    }
}
