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
package gov.nasa.worldwindx.examples.dataimport;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Illustrates how to import elevation data into WorldWind. This imports a GeoTIFF file containing elevation data and
 * creates an <code>{@link gov.nasa.worldwind.globes.ElevationModel}</code> for it.
 *
 * @author tag
 * @version $Id: ImportElevations.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ImportElevations extends ApplicationTemplate
{
    // The data to import.
    protected static final String ELEVATIONS_PATH = "gov/nasa/worldwindx/examples/data/craterlake-elev-16bit-30m.tif";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Show the WAIT cursor because the import may take a while.
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Import the elevations on a thread other than the event-dispatch thread to avoid freezing the UI.
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    importElevations();

                    // Restore the cursor.
                    setCursor(Cursor.getDefaultCursor());
                }
            });

            t.start();
        }

        protected void importElevations()
        {
            try
            {
                // Download the data and save it in a temp file.
                File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH, ".tif");

                // Create a local elevation model from the data.
                final LocalElevationModel elevationModel = new LocalElevationModel();
                elevationModel.addElevations(sourceFile);

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        // Get the WorldWindow's current elevation model.
                        Globe globe = AppFrame.this.getWwd().getModel().getGlobe();
                        ElevationModel currentElevationModel = globe.getElevationModel();

                        // Add the new elevation model to the globe.
                        if (currentElevationModel instanceof CompoundElevationModel)
                            ((CompoundElevationModel) currentElevationModel).addElevationModel(elevationModel);
                        else
                            globe.setElevationModel(elevationModel);

                        // Set the view to look at the imported elevations, although they might be hard to detect. To
                        // make them easier to detect, replace the globe's CompoundElevationModel with the new elevation
                        // model rather than adding it.
                        Sector modelSector = elevationModel.getSector();
                        ExampleUtil.goTo(getWwd(), modelSector);
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Elevation Import", ImportElevations.AppFrame.class);
    }
}
