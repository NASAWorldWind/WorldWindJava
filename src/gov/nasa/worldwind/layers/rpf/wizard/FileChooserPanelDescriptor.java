/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
