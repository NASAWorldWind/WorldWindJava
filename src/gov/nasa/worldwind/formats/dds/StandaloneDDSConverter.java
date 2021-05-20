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
package gov.nasa.worldwind.formats.dds;

import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.io.*;
import java.nio.*;

/**
 * @author Tom Gaskins
 * @version $Id: StandaloneDDSConverter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class StandaloneDDSConverter
{
    private static void convertToDDS(File file) throws IOException
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!file.exists() || !file.canRead())
        {
            String message = Logging.getMessage("DDSConverter.NoFileOrNoPermission");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (file.isDirectory())
            convertDirectory(file, new String[]{".jpg", "png"});
        else
            convertFile(file);
    }

    private static void convertDirectory(File dir, final String[] suffixes)
    {
        System.out.printf("===== Converting Directory %s\n", dir.getPath());

        File[] files = dir.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                for (String suffix : suffixes)
                {
                    if (file.getPath().endsWith(suffix))
                        return true;
                }

                return false;
            }
        });

        if (files != null)
        {
            for (File file : files)
            {
                try
                {
                    convertFile(file);
                }
                catch (Exception e)
                {
                    System.out.printf("Exception converting %s, skipping file\n", file.getPath());
                    e.printStackTrace();
                }
            }
        }

        File[] directories = dir.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });

        if (directories != null)
        {
            for (File directory : directories)
            {
                convertDirectory(directory, suffixes);
            }
        }
    }

    private static void convertFile(File file) throws IOException
    {
        System.out.printf("Converting File %s\n", file.getPath());
        ByteBuffer buffer = DDSCompressor.compressImageFile(file);
        File newFile = new File(WWIO.replaceSuffix(file.getPath(), ".dds"));
        WWIO.saveBuffer(buffer, newFile);
    }

    public static void main(String[] args)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        
        int status = fileChooser.showOpenDialog(null);
        if (status != JFileChooser.APPROVE_OPTION)
            return;
        
        File[] files = fileChooser.getSelectedFiles();
        if (files == null)
        {
            System.out.println("No files selected");
            return;
        }

        for (File file : files)
        {
            try
            {
                convertToDDS(file);
            }
            catch (IOException e)
            {
                System.out.printf("Exception converting input file %s, skipping file\n", file.getPath());
                e.printStackTrace();
            }
        }
    }
}
