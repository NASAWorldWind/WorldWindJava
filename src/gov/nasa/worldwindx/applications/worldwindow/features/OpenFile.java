/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: OpenFile.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OpenFile extends AbstractOpenResourceFeature
{
    public OpenFile(Registry registry)
    {
        super("Open File...", Constants.FEATURE_OPEN_FILE, null, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        WWMenu fileMenu = (WWMenu) this.getController().getRegisteredObject(Constants.FILE_MENU);
        if (fileMenu != null)
            fileMenu.addMenu(this.getFeatureID());
    }

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        JFileChooser fc = this.getController().getFileChooser();
        fc.setDialogTitle("Open File");
        fc.setMultiSelectionEnabled(false);

        try
        {
            int status = fc.showOpenDialog(this.getController().getFrame());
            if (status == JFileChooser.APPROVE_OPTION)
            {
                this.runOpenThread(fc.getSelectedFile());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        fc.setDialogTitle("");
        fc.setMultiSelectionEnabled(true);

        super.doActionPerformed(actionEvent);
    }
}
