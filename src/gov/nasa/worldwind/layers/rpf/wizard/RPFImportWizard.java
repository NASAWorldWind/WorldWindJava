/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf.wizard;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.wizard.Wizard;
import gov.nasa.worldwind.util.wizard.WizardPanelDescriptor;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: RPFImportWizard.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFImportWizard extends Wizard
{
    public RPFImportWizard()
    {
        registerPanels();
    }

    public RPFImportWizard(Dialog owner)
    {
        super(owner);
        registerPanels();
    }

    public RPFImportWizard(Frame owner)
    {
        super(owner);
        registerPanels();
    }
    
    private void registerPanels()
    {
        // Step 1: Choose where to import from.
        WizardPanelDescriptor wpd = new FileChooserPanelDescriptor();
        registerWizardPanel(FileChooserPanelDescriptor.IDENTIFIER, wpd);
        // Step 2a: Search for data to import.
        wpd = new FileSearchPanelDescriptor();
        registerWizardPanel(FileSearchPanelDescriptor.IDENTIFIER, wpd);
        // Step 2: Choose what to import.
        wpd = new DataChooserPanelDescriptor();
        registerWizardPanel(DataChooserPanelDescriptor.IDENTIFIER, wpd);
        // Step 3: Preprocessing (progress).
        wpd = new PreprocessPanelDescriptor();
        registerWizardPanel(PreprocessPanelDescriptor.IDENTIFIER, wpd);

        setCurrentPanelDescriptor(FileChooserPanelDescriptor.IDENTIFIER);
    }
}
