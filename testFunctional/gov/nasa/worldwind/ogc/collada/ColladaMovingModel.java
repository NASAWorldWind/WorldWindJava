/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

/**
 * Test loading a COLLADA model and moving the model along a path. World Wind does not support animations defined in
 * COLLADA files, but models may be animated by application logic.
 *
 * @author pabercrombie
 * @version $Id: ColladaMovingModel.java 664 2012-06-26 20:36:50Z pabercrombie $
 */
public class ColladaMovingModel extends ColladaViewer
{
    public static class AppFrame extends ColladaViewer.AppFrame
    {
        @Override
        protected void addColladaLayer(final ColladaRoot colladaRoot)
        {
            super.addColladaLayer(colladaRoot);

            // Rotate the duck to an upright position
            colladaRoot.setPitch(Angle.POS90);

            int delay = 1000; //milliseconds
            ActionListener taskPerformer = new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    double deltaDegrees = 0.001;
                    Position position = colladaRoot.getPosition();
                    Position newPosition = position.add(Position.fromDegrees(deltaDegrees, deltaDegrees));

                    // Move the model
                    colladaRoot.setPosition(newPosition);

                    // Move the view to follow the model
                    WorldWindow wwd = getWwd();
                    wwd.getView().goTo(newPosition, 2000);
                    wwd.redraw();
                }
            };
            new Timer(delay, taskPerformer).start();
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40.028);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -105.27284091410579);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 4000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 50);

        final AppFrame af = (AppFrame) start("World Wind COLLADA Viewer", AppFrame.class);

        new WorkerThread(new File("testData/collada/duck_triangulate.dae"),
            Position.fromDegrees(40.00779229910037, -105.27494931422459, 100), af).start();
    }
}