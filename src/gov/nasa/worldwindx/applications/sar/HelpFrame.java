/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.sar;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.html.*;

/**
 * @author tag
 * @version $Id: HelpFrame.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class HelpFrame extends JFrame
{
    private JEditorPane helpPane;

    public HelpFrame() throws IOException
    {
        initComponents();
        this.loadHelpText();
    }

    private void loadHelpText() throws IOException
    {
        InputStream is = this.getClass().getResourceAsStream("SARHelp.html");
        this.helpPane.read(is, new HTMLEditorKit());
    }

    private void initComponents()
    {
        //======== this ========
        setTitle(SARApp.APP_NAME + " Help");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== HelpPane ========
        {
            this.helpPane = new JEditorPane();
            this.helpPane.setEditable(false);
            this.helpPane.setPreferredSize(new Dimension(500, 600));
            this.helpPane.setContentType("text/html");
            JScrollPane scrollPane = new JScrollPane(this.helpPane);
            contentPane.add(scrollPane, BorderLayout.CENTER);
        }

        pack();
        SAR2.centerWindowInDesktop(this);
    }
}
