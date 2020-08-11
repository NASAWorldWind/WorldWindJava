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
