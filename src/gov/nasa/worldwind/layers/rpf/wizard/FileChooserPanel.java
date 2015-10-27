/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf.wizard;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * @author dcollins
 * @version $Id: FileChooserPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FileChooserPanel extends JPanel
{
    private JLabel title;
    private JLabel description;
    private JFormattedTextField fileField;
    private JButton chooseButton;
    private JFileChooser fileChooser;
    
    public FileChooserPanel()
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

    public JFileChooser getFileChooser()
    {
        return this.fileChooser;
    }

    public void setFileChooser(JFileChooser fileChooser)
    {
        if (fileChooser == null)
        {
            String message = "JFileChooser is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        this.fileChooser = fileChooser;
    }

    public File getSelectedFile()
    {
        return getValueAsFile(this.fileField.getValue());
    }

    private File getValueAsFile(Object value)
    {
        if (value != null)
        {
            if (value instanceof File)
                return (File) value;
            else
                return new File(value.toString());
        }
        return null;
    }

    public void promptForNewSelection()
    {
        onChooseClicked();
    }

    private void onChooseClicked()
    {
        int returnCode = this.fileChooser.showDialog(this, "Choose");
        if (returnCode == JFileChooser.APPROVE_OPTION && this.fileChooser.getSelectedFile() != null)
        {
            this.fileField.setValue(this.fileChooser.getSelectedFile());
        }
    }

    private void onFileFieldChanged(Object newValue)
    {
        File newFile = getValueAsFile(newValue);
        firePropertyChange("selectedFile", null, newFile);
    }

    private void makeComponents()
    {
        this.title = new JLabel(" ");
        this.title.setBackground(Color.gray);
        this.title.setOpaque(true);        
        this.description = new JLabel();
        this.fileField = new JFormattedTextField("Click 'Choose...'");
        Font font = this.fileField.getFont();
        if (!font.isBold())
            font = new Font(font.getName(), Font.BOLD | font.getStyle(), font.getSize());
        this.fileField.setFont(font);
        // Override input-path maximum size to avoid any vertical stretching by the layout manager.
        Dimension preferred = this.fileField.getPreferredSize();
        Dimension max = this.fileField.getMaximumSize();
        this.fileField.setMaximumSize(new Dimension(max.width, preferred.height));
        this.fileField.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event != null) {
                    onFileFieldChanged(event.getNewValue());
                }
            }
        });
        this.chooseButton = new JButton("Choose...");
        this.chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onChooseClicked();
            }
        });
        this.fileChooser = new JFileChooser();
        this.fileChooser.setDialogTitle("Choose Folder to Search");
        this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.fileChooser.setMultiSelectionEnabled(false);
    }

    private void layoutComponents()
    {
        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBackground(this.title.getBackground());
        this.title.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(this.title, BorderLayout.WEST);
        JSeparator separator = new JSeparator();
        p.add(separator, BorderLayout.SOUTH);
        add(p, BorderLayout.NORTH);

        Box b = Box.createVerticalBox();
        b.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.description.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(this.description);
        b.add(Box.createVerticalStrut(10));
        Box b2 = Box.createHorizontalBox();
        b2.setAlignmentX(Component.LEFT_ALIGNMENT);
        b2.add(this.fileField);
        b2.add(Box.createHorizontalStrut(10));
        b2.add(this.chooseButton);
        b.add(b2);
        b.add(Box.createVerticalStrut(Short.MAX_VALUE));
        add(b, BorderLayout.CENTER);
    }
}
