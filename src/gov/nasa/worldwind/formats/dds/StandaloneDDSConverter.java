/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
