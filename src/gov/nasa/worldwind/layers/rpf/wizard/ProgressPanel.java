/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf.wizard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: ProgressPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ProgressPanel extends JPanel
{
    // Panel title and optional description.
    private JLabel title;
    private JLabel description;
    // Progress UI components.
    private JProgressBar progressBar;
    private JLabel progressDescription1;
    private JLabel progressDescription2;

    public ProgressPanel()
    {
        makeComponents();
        layoutComponents();
    }

    public String getTitle()
    {
        return this.title.getText();
    }

    public void setTitle(String title)
    {
        this.title.setText(title);
    }

    public String getDescription()
    {
        return this.description.getText();
    }

    public void setDescription(String description)
    {
        this.description.setText(description);
    }

    public JProgressBar getProgressBar()
    {
        return this.progressBar;
    }
    
    public String getProgressDescription1()
    {
        return this.progressDescription1.getText();
    }

    public void setProgressDescription1(String description)
    {
        this.progressDescription1.setText(description);
    }

    public String getProgressDescription2()
    {
        return this.progressDescription2.getText();
    }

    public void setProgressDescription2(String description)
    {
        this.progressDescription2.setText(description);
    }

    private void makeComponents()
    {
        this.title = new JLabel(" ");
        this.title.setBackground(Color.gray);
        this.title.setOpaque(true);
        this.description = new JLabel();

        this.progressBar = new JProgressBar();
        this.progressDescription1 = new JLabel();
        this.progressDescription2 = new JLabel();
    }

    private void layoutComponents()
    {
        removeAll();
        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBackground(this.title.getBackground());
        // Title, may be empty.
        this.title.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(this.title, BorderLayout.WEST);
        JSeparator separator = new JSeparator();
        p.add(separator, BorderLayout.SOUTH);
        add(p, BorderLayout.NORTH);

        Box b = Box.createVerticalBox();
        b.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Description, may be empty.
        this.description.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(this.description);
        b.add(Box.createVerticalStrut(10));
        // Progress description 1, may be empty.
        this.progressDescription1.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(this.progressDescription1);
        b.add(Box.createVerticalStrut(5));
        // Progress bar.
        this.progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(this.progressBar);
        b.add(Box.createVerticalStrut(5));
        // Progress description 2, may be empty.
        this.progressDescription2.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(this.progressDescription2);

        JScrollPane scrollPane = new JScrollPane(b);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(scrollPane, BorderLayout.CENTER);
    }
}
