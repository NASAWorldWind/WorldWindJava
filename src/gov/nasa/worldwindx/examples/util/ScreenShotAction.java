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

package gov.nasa.worldwindx.examples.util;

import com.jogamp.opengl.util.awt.*;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.util.WWIO;

import javax.imageio.ImageIO;
import com.jogamp.opengl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

/**
 * @author tag
 * @version $Id: ScreenShotAction.java 1689 2013-10-23 18:18:11Z dcollins $
 */
public class ScreenShotAction extends AbstractAction implements RenderingListener
{
    WorldWindow wwd;
    private File snapFile;
    JFileChooser fileChooser;

    public ScreenShotAction(WorldWindow wwd)
    {
        super("Screen Shot");

        this.wwd = wwd;
        this.fileChooser = new JFileChooser();
    }

    public void actionPerformed(ActionEvent event)
    {
        Component frame = wwd instanceof Component ? ((Component) wwd).getParent() : null;
        this.snapFile = this.chooseFile(frame);
    }

    private File chooseFile(Component parentFrame)
    {
        File outFile = null;

        try
        {
            while (true)
            {
                fileChooser.setDialogTitle("Save Screen Shot");
                fileChooser.setSelectedFile(new File(composeSuggestedName()));

                int status = fileChooser.showSaveDialog(parentFrame);
                if (status != JFileChooser.APPROVE_OPTION)
                    return null;

                outFile = fileChooser.getSelectedFile();
                if (outFile == null) // Shouldn't happen, but include a reaction just in case
                {
                    JOptionPane.showMessageDialog(parentFrame, "Please select a location for the image file.",
                        "No Location Selected", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!outFile.getPath().endsWith(".png"))
                    outFile = new File(outFile.getPath() + ".png");

                if (outFile.exists())
                {
                    status = JOptionPane.showConfirmDialog(parentFrame,
                        "Replace existing file\n" + outFile.getName() + "?",
                        "Overwrite Existing File?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (status == JOptionPane.NO_OPTION)
                        continue;
                    if (status != JOptionPane.YES_OPTION)
                        return null;
                }
                break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.wwd.removeRenderingListener(this); // ensure not to add a duplicate
        this.wwd.addRenderingListener(this);

        return outFile;
    }

    public void stageChanged(RenderingEvent event)
    {
        if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP) && this.snapFile != null)
        {
            try
            {
                GLAutoDrawable glad = (GLAutoDrawable) event.getSource();
                AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(glad.getGLProfile(), false);
                BufferedImage image = glReadBufferUtil.readPixelsToBufferedImage(glad.getGL(), true);
                String suffix = WWIO.getSuffix(this.snapFile.getPath());
                ImageIO.write(image, suffix, this.snapFile);
                System.out.printf("Image saved to file %s\n", this.snapFile.getPath());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                this.snapFile = null;
                this.wwd.removeRenderingListener(this);
            }
        }
    }

    private String composeSuggestedName()
    {
        String baseName = "WWJSnapShot";
        String suffix = ".png";

        File currentDirectory = this.fileChooser.getCurrentDirectory();

        File candidate = new File(currentDirectory.getPath() + File.separatorChar + baseName + suffix);
        for (int i = 1; candidate.exists(); i++)
        {
            String sequence = String.format("%03d", i);
            candidate = new File(currentDirectory.getPath() + File.separatorChar + baseName + sequence + suffix);
        }

        return candidate.getPath();
    }
}
