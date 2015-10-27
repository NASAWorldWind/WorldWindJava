/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.rpf.wizard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author dcollins
 * @version $Id: DataChooserPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DataChooserPanel extends JPanel
{
    private JLabel title;
    private JLabel description;
    // Logical data components.
    private Collection<FileSet> fileSetList;
    private PropertyEvents propertyEvents;
    private Map<FileSet, JToggleButton> selectButtons;
    // Data UI components.
    private JComponent dataPanel;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private JScrollPane dataScrollPane;
    private JLabel dataDescription;

    public DataChooserPanel()
    {
        this.propertyEvents = new PropertyEvents();
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

    public String getDataDescription()
    {
        return this.dataDescription.getText();
    }

    public void setDataDescription(String dataDescription)
    {
        this.dataDescription.setText(dataDescription);
    }

    public void setFileSetList(Collection<FileSet> fileSetList)
    {
        removeListeners(this.fileSetList);
        this.fileSetList = fileSetList;
        addListeners(this.fileSetList);

        this.selectButtons = new HashMap<FileSet, JToggleButton>();

        if (fileSetList != null && fileSetList.size() > 0)
        {
            Box box = Box.createVerticalBox();
            for (FileSet set : fileSetList)
            {
                JCheckBox checkBox = new JCheckBox();
                checkBox.putClientProperty("fileSet", set);
                checkBox.setSelected(set.isSelected());
                checkBox.setText(makeTitle(set));
                checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                checkBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        fileSetClicked(e);
                    }
                });
                box.add(checkBox);

                this.selectButtons.put(set, checkBox);
            }
            this.dataScrollPane.setViewportView(box);
            this.dataPanel.setVisible(true);
        }
        else
        {
            this.dataScrollPane.setViewportView(null);
            this.dataPanel.setVisible(false);
        }
        this.dataPanel.validate();

        fileSetSelectionChanged(null);
    }

    private String makeTitle(FileSet set)
    {
        String title = null;
        if (set != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            if (set.getTitle() != null)
                sb.append(set.getTitle());
            else if (set.getIdentifier() != null)
                sb.append(set.getIdentifier());
            else
                sb.append("Various");
            int fileCount = set.getFileCount();
            if (fileCount > 0)
            {
                sb.append("<font size=\"-2\">");
                if (sb.length() > 0)
                    sb.append(" - ");
                sb.append(String.format("%,d", fileCount)).append(" file").append(fileCount > 1 ? "s" : "");
                sb.append("</font>");
            }
            sb.append("</html>");
            title = sb.toString();
        }
        return title;
    }

    private void fileSetClicked(ItemEvent e)
    {
        if (e != null)
        {
            FileSet set = null;
            if (e.getItem() != null && e.getItem() instanceof JComponent)
            {
                Object property = ((JComponent) e.getItem()).getClientProperty("fileSet");
                if (property != null && property instanceof FileSet)
                    set = (FileSet) property;
            }

            if (set != null)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    set.setSelected(true);
                else if (e.getStateChange() == ItemEvent.DESELECTED)
                    set.setSelected(false);
            }
        }
    }

    private void setAllSelected(boolean b)
    {
        if (this.fileSetList != null)
        {
            for (FileSet set : this.fileSetList)
            {
                set.setSelected(b);
            }
        }
    }

    private void selectAllPressed()
    {
        setAllSelected(true);
    }

    private void deselectAllPressed()
    {
        setAllSelected(false);
    }

    private void fileSetSelectionChanged(Object source)
    {
        // Make sure the CheckBox selection reflects the FileSet selection state.
        if (source != null && source instanceof FileSet)
        {
            FileSet set = (FileSet) source;
            JToggleButton button = this.selectButtons.get(set);
            if (button != null)
                button.setSelected(set.isSelected());
        }

        // Enable "Select All" and "Select None" only when necessary.
        boolean allSelected = true;
        boolean anySelected = false;
        if (this.fileSetList != null)
        {
            for (FileSet set : this.fileSetList)
            {
                allSelected &= set.isSelected();
                anySelected |= set.isSelected();
            }
        }
        this.selectAllButton.setEnabled(!allSelected);
        this.deselectAllButton.setEnabled(anySelected);
    }

    private void addListeners(Collection<FileSet> fileSetList)
    {
        if (fileSetList != null)
        {
            for (FileSet set : fileSetList)
            {
                set.addPropertyChangeListener(this.propertyEvents);
            }
        }
    }

    private void removeListeners(Collection<FileSet> fileSetList)
    {
        if (fileSetList != null)
        {
            for (FileSet set : fileSetList)
            {
                set.removePropertyChangeListener(this.propertyEvents);
            }
        }
    }

    private class PropertyEvents implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt != null && evt.getPropertyName() != null) {
                String propertyName = evt.getPropertyName();
                if (propertyName.equals(FileSet.SELECTED)) {
                    fileSetSelectionChanged(evt.getSource());
                    firePropertyChange(propertyName, null, evt.getSource());
                }
            }
        }
    }

    private void makeComponents()
    {
        this.title = new JLabel(" ");
        this.title.setBackground(Color.gray);
        this.title.setOpaque(true);
        this.description = new JLabel();

        this.dataPanel = Box.createVerticalBox();
        this.selectAllButton = new JButton("Select All");
        this.deselectAllButton = new JButton("Select None");
        Font font = this.selectAllButton.getFont();
        font = new Font(font.getName(), font.getStyle(), 9);
        Dimension size = new Dimension(35, 20);
        this.selectAllButton.setFont(font);
        this.selectAllButton.setPreferredSize(size);
        this.selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllPressed();
            }
        });
        this.deselectAllButton.setFont(font);
        this.deselectAllButton.setPreferredSize(size);
        this.deselectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deselectAllPressed();
            }
        });
        this.dataScrollPane = new JScrollPane();
        this.dataScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        size = this.dataScrollPane.getPreferredSize();
        this.dataScrollPane.setPreferredSize(new Dimension(size.width, Short.MAX_VALUE));
        this.dataScrollPane.setOpaque(false);
        this.dataScrollPane.getViewport().setOpaque(false);

        this.dataDescription = new JLabel(" ");
    }

    private void layoutComponents()
    {
        setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBackground(this.title.getBackground());
        this.title.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(this.title, BorderLayout.WEST);
        p.add(new JSeparator(), BorderLayout.SOUTH);
        add(p, BorderLayout.NORTH);

        Box b = Box.createVerticalBox();
        b.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.description.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(this.description);
        b.add(Box.createVerticalStrut(10));

        this.dataPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Box b2 = Box.createHorizontalBox();
        b2.setAlignmentX(Component.LEFT_ALIGNMENT);
        b2.add(this.selectAllButton);
        b2.add(Box.createHorizontalStrut(2));
        b2.add(new JSeparator(SwingConstants.VERTICAL));
        b2.add(Box.createHorizontalStrut(2));
        b2.add(this.deselectAllButton);
        b2.add(Box.createHorizontalStrut(Short.MAX_VALUE));
        this.dataPanel.add(b2);
        this.dataPanel.add(Box.createVerticalStrut(5));
        this.dataScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.dataPanel.add(this.dataScrollPane);
        b.add(this.dataPanel);
        b.add(Box.createVerticalStrut(5));
        this.dataPanel.setVisible(false);

        this.dataDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.add(this.dataDescription);

        add(b, BorderLayout.CENTER);
    }
}
