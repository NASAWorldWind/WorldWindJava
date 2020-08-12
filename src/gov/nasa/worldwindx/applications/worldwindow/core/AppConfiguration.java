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

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: AppConfiguration.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AppConfiguration implements Initializable
{
    protected Controller controller;
    protected String configurationLocation;

    public AppConfiguration()
    {
    }

    public void initialize(Controller controller)
    {
        this.controller = controller;
    }

    public boolean isInitialized()
    {
        return this.controller != null;
    }

    public void configure(final String appConfigurationLocation)
    {
        if (WWUtil.isEmpty(appConfigurationLocation))
            throw new IllegalArgumentException("The application configuration location name is null or empty");

        this.configurationLocation = appConfigurationLocation;

        ImageLibrary.setInstance(new ImageLibrary());

        this.configureFeatures(appConfigurationLocation);
    }

    protected void configureFeatures(final String appConfigurationLocation)
    {
        // Configure the application objects on the EDT
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    registerConfiguration(appConfigurationLocation);
                }
                catch (Exception e)
                {
                    Util.getLogger().log(Level.SEVERE, "Unable to create initial configuration for {0}",
                        appConfigurationLocation);
                }
            }
        });
    }

    // Registers the objects in the configuration.
    protected void registerConfiguration(String config) throws Exception
    {
        // TODO: this call can return null
        Document doc = WWXML.openDocumentFile(config, this.getClass());
        NodeList emNodes = (NodeList) WWXML.makeXPath().evaluate("//Feature", doc, XPathConstants.NODESET);
        ArrayList<Object> objects = new ArrayList<Object>();

        for (int i = 0; i < emNodes.getLength(); i++)
        {
            String featureID = null;
            String className = null;
            String actuate = null;

            try
            {
                Element element = (Element) emNodes.item(i);

                featureID = WWXML.getText(element, "@featureID");
                className = WWXML.getText(element, "@className");
                actuate = WWXML.getText(element, "@actuate");

                if (className == null || className.length() == 0)
                {
                    Util.getLogger().log(Level.WARNING,
                        "Configuration entry in {0} missing feature ID ({1})or classname ({2})",
                        new Object[]
                            {config, featureID != null ? featureID : "null", className != null ? className : "null"});
                    continue;
                }

                if (!WWUtil.isEmpty(featureID))
                {
                    if (actuate != null && actuate.equals("onDemand"))
                        this.controller.registerObject(featureID, Class.forName(className));
                    else
                        objects.add(this.controller.createAndRegisterObject(featureID, className));
                }
                else
                {
                    objects.add(this.controller.createRegistryObject(className));
                }

                String accelerator = WWXML.getText(element, "@accelerator");
                if (accelerator != null && accelerator.length() > 0)
                    this.controller.registerObject(className + Constants.ACCELERATOR_SUFFIX, accelerator);
            }
            catch (Exception e)
            {
                String msg = String.format(
                    "Error creating configuration entry in %s for feature ID (%s), classname (%s), activate (%s)",
                    config, featureID != null ? featureID : "null",
                    className != null ? className : "null",
                    actuate != null ? actuate : "null");
                Util.getLogger().log(Level.WARNING, msg, e);
                //noinspection UnnecessaryContinue
                continue;
            }
        }

        for (Object o : objects)
        {
            try
            {
                if (o instanceof Initializable)
                    ((Initializable) o).initialize(this.controller);
            }
            catch (Exception e)
            {
                String msg = String.format("Error initializing object %s", o.getClass().getName());
                Util.getLogger().log(Level.WARNING, msg, e);
            }
        }
    }
}
