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
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: AboutDialog.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AboutDialog
{
    private Object content;
    private String contentType;
    private Dimension preferredSize;

    public AboutDialog()
    {
    }

    public Object getContent()
    {
        return this.content;
    }

    public void setContent(Object content)
    {
        this.content = content;
    }

    public String getContentType()
    {
        return this.contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public Dimension getPreferredSize()
    {
        return this.preferredSize;
    }

    public void setPreferredSize(Dimension preferredSize)
    {
        this.preferredSize = preferredSize;
    }

    public void showDialog(Component parentComponent)
    {
        Component component = makeContentComponent();
        showContentDialog(parentComponent, component);
    }

    private static void showContentDialog(Component parentComponent, Component component)
    {
        try
        {
            final JDialog dialog;
            if (parentComponent instanceof Dialog)
                dialog = new JDialog((Dialog) parentComponent);
            else if (parentComponent instanceof Frame)
                dialog = new JDialog((Frame) parentComponent);
            else
                dialog = new JDialog();

            component.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });

            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.getContentPane().add(component, BorderLayout.CENTER);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setModal(true);
            dialog.setResizable(false);
            dialog.pack();
            SAR2.centerWindowInDesktop(dialog);
            dialog.setVisible(true);
        }
        catch (Exception e)
        {
            String message = "Exception while displaying content dialog";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    private Component makeContentComponent()
    {
        JEditorPane editor = null;
        try
        {
            if (this.content != null)
            {
                URL url = getClass().getResource(this.content.toString());
                editor = new JEditorPane();
                if (this.contentType != null)
                    editor.setContentType(this.contentType);
                editor.setPage(url);
            }

            if (editor != null)
            {
                editor.setEditable(false);
                if (this.preferredSize != null)
                    editor.setPreferredSize(this.preferredSize);
            }
        }
        catch (Exception e)
        {
            String message = "Exception while fetching content";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            editor = null;
        }
        return editor;
    }
}
