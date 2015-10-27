/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: FileMenu.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FileMenu extends AbstractMenu
{
    public FileMenu(Registry registry)
    {
        super("File", Constants.FILE_MENU, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToMenuBar();
    }
}
