/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf.wizard;

import gov.nasa.worldwind.util.wizard.WizardProperties;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.layers.Layer;

import java.io.File;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: RPFWizardUtil.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFWizardUtil
{
    public static final String SELECTED_FILE = "selectedFile";
    public static final String FILE_LIST = "fileList";
    public static final String IS_FILE_LIST_CURRENT = "isFileListCurrent";
    public static final String FILE_SET_LIST = "fileSetList";
    public static final String LAYER_LIST = "layerList";

    public static File getSelectedFile(WizardProperties properties)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = null;
        Object value = properties.getProperty(SELECTED_FILE);
        if (value != null && value instanceof File)
            file = (File) value;
        return file;
    }

    public static void setSelectedFile(WizardProperties properties, File file)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        properties.setProperty(SELECTED_FILE, file);
    }

    @SuppressWarnings({"unchecked"})
    public static List<File> getFileList(WizardProperties properties)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<File> fileList = null;
        Object value = properties.getProperty(FILE_LIST);
        if (value != null && value instanceof List)
            fileList = (List<File>) value;
        return fileList;
    }

    public static void setFileList(WizardProperties properties, List<File> fileList)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        properties.setProperty(FILE_LIST, fileList);
    }

    public static boolean isFileListCurrent(WizardProperties properties)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean isFileListCurrent = false;
        Boolean value = properties.getBooleanProperty(IS_FILE_LIST_CURRENT);
        if (value != null)
            isFileListCurrent = value;
        return isFileListCurrent;
    }

    public static void setFileListCurrent(WizardProperties properties, boolean current)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        properties.setProperty(IS_FILE_LIST_CURRENT, current);
    }

    @SuppressWarnings({"unchecked"})
    public static List<FileSet> getFileSetList(WizardProperties properties)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<FileSet> fileSets = null;
        Object value = properties.getProperty(FILE_SET_LIST);
        if (value != null && value instanceof List)
            fileSets = (List<FileSet>) value;
        return fileSets;
    }

    public static void setFileSetList(WizardProperties properties, List<FileSet> fileSetList)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        properties.setProperty(FILE_SET_LIST, fileSetList);
    }

    @SuppressWarnings({"unchecked"})
    public static List<Layer> getLayerList(WizardProperties properties)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<Layer> fileSets = null;
        Object value = properties.getProperty(LAYER_LIST);
        if (value != null && value instanceof List)
            fileSets = (List<Layer>) value;
        return fileSets;
    }

    public static void setLayerList(WizardProperties properties, List<Layer> layerList)
    {
        if (properties == null)
        {
            String message = "WizardProperties is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        properties.setProperty(LAYER_LIST, layerList);
    }

    public static String makeLarger(String text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<font size=\"+1\">");
        sb.append(text);
        sb.append("</font>");
        sb.append("</html>");
        return sb.toString();
    }

    public static String makeSmaller(String text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<font size=\"-2\">");
        sb.append(text);
        sb.append("</font>");
        sb.append("</html>");
        return sb.toString();
    }

    public static String makeBold(String text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<br>");
        sb.append("<b>");
        sb.append(text);
        sb.append("</b>");
        sb.append("<br>");
        sb.append("</html>");
        return sb.toString();
    }
}
