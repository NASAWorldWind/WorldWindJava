/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Example of integrating a gazetteer search function using the {@link gov.nasa.worldwind.poi.YahooGazetteer}.
 * The example includes a search dialog box, into which the user can enter a search term or a coordinate pair. When the
 * search is executed, the globe will animate to top search result.
 *
 * @author jparsons
 * @version $Id: GazetteerApp.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GazetteerApp extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame() throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException
        {
            super(true, false, false);

            this.getContentPane().add(new GazetteerPanel(this.getWwd(), null),   //use default yahoo service
                BorderLayout.NORTH);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Gazetteer Example", AppFrame.class);
    }
}
