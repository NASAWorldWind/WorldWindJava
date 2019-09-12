/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Earth.UTMBaseGraticuleLayer;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;

/**
 * @author dcollins
 * @version $Id: MGRSAttributesPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings("unchecked")
public class MGRSAttributesPanel extends JPanel
{
    // Logical components.
    private final MGRSGraticuleLayer layer;
    // UI components.
    private JList itemList;
    private JComboBox maxResolutionComboBox;
    private JButton saveStateButton;
    private JButton loadStateButton;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private Map<String, Component> graticuleAttribPanelMap = new HashMap<String, Component>();
    // Helper properties.
    private boolean ignoreLayerEvents = false;
    private boolean ignoreUIEvents = false;

    private String[] ALL_GRATICULE_TYPES = new String[] {
        UTMBaseGraticuleLayer.GRATICULE_UTM,
        MGRSGraticuleLayer.GRATICULE_UTM_GRID,
        MGRSGraticuleLayer.GRATICULE_100000M,
        MGRSGraticuleLayer.GRATICULE_10000M,
        MGRSGraticuleLayer.GRATICULE_1000M,
        MGRSGraticuleLayer.GRATICULE_100M,
        MGRSGraticuleLayer.GRATICULE_10M,
        MGRSGraticuleLayer.GRATICULE_1M
    };
    private String[] MGRS_GRATICULE_TYPES = new String[] {
        MGRSGraticuleLayer.GRATICULE_UTM_GRID,
        MGRSGraticuleLayer.GRATICULE_100000M,
        MGRSGraticuleLayer.GRATICULE_10000M,
        MGRSGraticuleLayer.GRATICULE_1000M,
        MGRSGraticuleLayer.GRATICULE_100M,
        MGRSGraticuleLayer.GRATICULE_10M,
        MGRSGraticuleLayer.GRATICULE_1M
    };

    public MGRSAttributesPanel(MGRSGraticuleLayer mgrsGraticuleLayer)
    {
        if (mgrsGraticuleLayer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.layer = mgrsGraticuleLayer;
        this.layer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                onLayerPropertyChanged(event);
            }
        });
        init();
    }

    private void init()
    {
        // Initialize UI components.
        makeComponents();
        // Initialize UI layout.
        layoutComponents();
        // Update UI state to reflect the current layer state.
        updateComponents();

        String selectedType = MGRSGraticuleLayer.GRATICULE_UTM_GRID;
        this.itemList.setSelectedValue(selectedType, true);
        this.cardLayout.show(this.cardPanel, selectedType);
    }

    public final MGRSGraticuleLayer getLayer()
    {
        return this.layer;
    }

    public static JDialog showDialog(Component component, String title, MGRSGraticuleLayer mgrsGraticuleLayer)
    {
        if (mgrsGraticuleLayer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        JDialog dialog;
        if (component != null && component instanceof Dialog)
        {
            dialog = new JDialog((Dialog) component);
        }
        else if (component != null && component instanceof Frame)
        {
            dialog = new JDialog((Frame) component);
        }
        else
        {
            dialog = new JDialog();
        }

        if (title != null)
        {
            dialog.setTitle(title);
        }

        MGRSAttributesPanel panel = new MGRSAttributesPanel(mgrsGraticuleLayer);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setVisible(true);
        return dialog;
    }

    public String getSelectedGraticule()
    {
        Object selectedValue = this.itemList.getSelectedValue();
        return selectedValue != null ? selectedValue.toString() : null;
    }

    public void setSelectedGraticule(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.itemList.setSelectedValue(graticuleType, true);
        this.cardLayout.show(this.cardPanel, graticuleType);
    }

    private void onListSelectionChanged(ListSelectionEvent event)
    {
        if (event != null)
        {
            Object selectedValue = this.itemList.getSelectedValue();
            this.cardLayout.show(this.cardPanel, selectedValue.toString());
        }
    }

    private void onMaxResolutionChanged(ActionEvent event)
    {
        if (event != null)
        {
            if (!this.ignoreUIEvents)
            {
                updateLayer();
            }
        }
    }

    private void onSaveStatePressed(ActionEvent event)
    {
        if (event == null)
            return;

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int resultVal = fc.showSaveDialog(this);
        if (resultVal != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        if (file == null)
            return;

        try
        {
            String stateInXml = this.layer.getRestorableState();
            saveString(stateInXml, file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void onLoadStatePressed(ActionEvent event)
    {
        if (event == null)
            return;

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int resultVal = fc.showOpenDialog(this);
        if (resultVal != JFileChooser.APPROVE_OPTION)
            return;

        File file = fc.getSelectedFile();
        if (file == null)
            return;

        try
        {
            String stateInXml = loadString(file);
            this.layer.restoreState(stateInXml);
            this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void onPanelStateChanged(PropertyChangeEvent event, String graticuleType)
    {
        if (event != null && graticuleType != null)
        {
            if (!this.ignoreUIEvents)
            {
                if (//event.getPropertyName().equals(GraticuleAttributesPanel.LINE_ENABLED_PROPERTY)
                    event.getPropertyName().equals(GraticuleAttributesPanel.LINE_COLOR_PROPERTY)
                    || event.getPropertyName().equals(GraticuleAttributesPanel.LINE_WIDTH_PROPERTY)
                    || event.getPropertyName().equals(GraticuleAttributesPanel.LINE_STYLE_PROPERTY)
                    || event.getPropertyName().equals(GraticuleAttributesPanel.LABEL_ENABLED_PROPERTY)
                    || event.getPropertyName().equals(GraticuleAttributesPanel.LABEL_COLOR_PROPERTY)
                    || event.getPropertyName().equals(GraticuleAttributesPanel.LABEL_FONT_PROPERTY))
                {
                    updateLayer();
                }
            }
        }
    }

    private void updateLayer()
    {
        this.ignoreLayerEvents = true;
        try
        {
            if (this.layer != null)
            {
                this.layer.setMaximumGraticuleResolution(this.maxResolutionComboBox.getSelectedItem().toString());

                for (Map.Entry<String, Component> entry : this.graticuleAttribPanelMap.entrySet())
                {
                    if (entry.getKey() != null && entry.getValue() != null)
                    {
                        if (entry.getValue() instanceof GraticuleAttributesPanel)
                        {
                            updateLayerState((GraticuleAttributesPanel) entry.getValue(), entry.getKey());
                        }
                    }
                }
            }
        }
        finally
        {
            this.ignoreLayerEvents = false;
        }
    }

    private void updateLayerState(GraticuleAttributesPanel attributesPanel, String graticuleType)
    {
        if (this.layer != null && attributesPanel != null && graticuleType != null)
        {
            //this.layer.setDrawGraticule(attributesPanel.isLineEnableSelected(), graticuleType);
            this.layer.setGraticuleLineColor(attributesPanel.getSelectedLineColor(), graticuleType);
            this.layer.setGraticuleLineWidth(attributesPanel.getSelectedLineWidth(), graticuleType);
            this.layer.setGraticuleLineStyle(attributesPanel.getSelectedLineStyle(), graticuleType);
            this.layer.setDrawLabels(attributesPanel.isLabelEnableSelected(), graticuleType);
            this.layer.setLabelColor(attributesPanel.getSelectedLabelColor(), graticuleType);
            this.layer.setLabelFont(attributesPanel.getSelectedLabelFont(), graticuleType);
            this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
        }
    }

    private void onLayerPropertyChanged(PropertyChangeEvent event)
    {
        if (event != null)
        {
            if (!this.ignoreLayerEvents)
            {
                updateComponents();
            }
        }
    }

    private void updateComponents()
    {
        this.ignoreUIEvents = true;
        try
        {
            if (this.layer != null)
            {
                this.maxResolutionComboBox.setSelectedItem(layer.getMaximumGraticuleResolution());
            }

            for (Map.Entry<String, Component> entry : this.graticuleAttribPanelMap.entrySet())
            {
                if (entry.getKey() != null && entry.getValue() != null)
                {
                    if (entry.getValue() instanceof GraticuleAttributesPanel)
                    {
                        updatePanelState((GraticuleAttributesPanel) entry.getValue(), entry.getKey());
                    }
                }
            }
        }
        finally
        {
            this.ignoreUIEvents = false;
        }
    }

    private void updatePanelState(GraticuleAttributesPanel panel, String graticuleType)
    {
        if (this.layer != null && panel != null && graticuleType != null)
        {
            panel.setSelectedLineColor(this.layer.getGraticuleLineColor(graticuleType));
            panel.setSelectedLineWidth(this.layer.getGraticuleLineWidth(graticuleType));
            panel.setSelectedLineStyle(this.layer.getGraticuleLineStyle(graticuleType));
            panel.setLabelEnableSelected(this.layer.isDrawLabels(graticuleType));
            panel.setSelectedLabelColor(this.layer.getLabelColor(graticuleType));
            panel.setSelectedLabelFont(this.layer.getLabelFont(graticuleType));
        }
    }

    private static String loadString(File file) throws IOException
    {
        String s = null;
        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            StringBuilder sb = new StringBuilder();
            int numRead;
            char[] buffer = new char[2048];
            while ((numRead = reader.read(buffer, 0, buffer.length)) != -1)
            {
                sb.append(buffer, 0, numRead);
            }
            s = sb.toString();
        }
        finally
        {
            try
            {
                if (reader != null)
                    reader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return s;
    }

    private static void saveString(String s, File file) throws IOException
    {
        FileWriter writer = null;
        try
        {
            if (s != null && file != null)
            {
                writer = new FileWriter(file);
                writer.write(s);
            }
        }
        finally
        {
            try
            {
                if (writer != null)
                    writer.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void makeComponents()
    {
        this.itemList = new JList(ALL_GRATICULE_TYPES);
        this.itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListCellRenderer originalRenderer = this.itemList.getCellRenderer();
        this.itemList.setCellRenderer(new GraticuleTypeListRenderer(originalRenderer, null));
        this.itemList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                onListSelectionChanged(event);
            }
        });
        this.saveStateButton = new JButton("Save State");
        this.saveStateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSaveStatePressed(event);
            }
        });
        this.loadStateButton = new JButton("Load State");
        this.loadStateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onLoadStatePressed(event);
            }
        });

        this.maxResolutionComboBox = new JComboBox(MGRS_GRATICULE_TYPES);
        originalRenderer = this.maxResolutionComboBox.getRenderer();
        this.maxResolutionComboBox.setRenderer(new GraticuleTypeListRenderer(originalRenderer, null));
        this.maxResolutionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onMaxResolutionChanged(event);
            }
        });

        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel();
        this.cardPanel.setLayout(this.cardLayout);
        for (String type : ALL_GRATICULE_TYPES)
        {
            Component panel = makeGraticulePanel(type);
            this.graticuleAttribPanelMap.put(type, panel);
        }
    }

    private Component makeGraticulePanel(final String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GraticuleAttributesPanel panel = new GraticuleAttributesPanel();
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                onPanelStateChanged(event, graticuleType);
            }
        });

        return panel;
    }

    private void layoutComponents()
    {
        setLayout(new BorderLayout());

        //---------- Graticule Item List ----------//
        {
            Box box = Box.createVerticalBox();
            box.setBorder(new EmptyBorder(30, 20, 20, 5));

            JScrollPane itemScrollPane = new JScrollPane(this.itemList);
            itemScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(itemScrollPane);
            box.add(Box.createVerticalStrut(20));

            JLabel label = new JLabel("Maximum Resolution");
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(label);
            this.maxResolutionComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(this.maxResolutionComboBox);
            box.add(Box.createVerticalStrut(20));

            this.saveStateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(this.saveStateButton);
            this.loadStateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(this.loadStateButton);

            box.add(Box.createVerticalGlue());
            add(box, BorderLayout.WEST);
        }

        //---------- Graticule Card Panel ----------//
        {
            this.cardPanel.setBorder(new EmptyBorder(30, 5, 20, 20));
            for (Map.Entry<String, Component> entry : this.graticuleAttribPanelMap.entrySet())
            {
                if (entry.getKey() != null && entry.getValue() != null)
                {
                    this.cardPanel.add(entry.getValue(), entry.getKey());
                }
            }
            add(this.cardPanel, BorderLayout.CENTER);
        }
    }

    private static String getGraticuleLabel(String graticuleType)
    {
        String labelText = null;
        if (UTMBaseGraticuleLayer.GRATICULE_UTM.equals(graticuleType))
            labelText = "Global UTM";
        else if (MGRSGraticuleLayer.GRATICULE_UTM_GRID.equals(graticuleType))
            labelText = "UTM Grid";
        else if (MGRSGraticuleLayer.GRATICULE_100000M.equals(graticuleType))
            labelText = "100km";
        else if (MGRSGraticuleLayer.GRATICULE_10000M.equals(graticuleType))
            labelText = "10km";
        else if (MGRSGraticuleLayer.GRATICULE_1000M.equals(graticuleType))
            labelText = "1km";
        else if (MGRSGraticuleLayer.GRATICULE_100M.equals(graticuleType))
            labelText = "100m";
        else if (MGRSGraticuleLayer.GRATICULE_10M.equals(graticuleType))
            labelText = "10m";
        else if (MGRSGraticuleLayer.GRATICULE_1M.equals(graticuleType))
            labelText = "1m";
        return labelText;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private Icon getIcon(String path)
    {
        Icon icon = null;
        try
        {
            URL url = getClass().getResource(path);
            if (url != null)
            {
                icon = new ImageIcon(url);
            }
        }
        catch (Exception e)
        {
            String message = "Exception while loading icon";
            Logging.logger().log(java.util.logging.Level.WARNING, message, e);
        }
        return icon;
    }

    @SuppressWarnings("unchecked")
    private static class GraticuleTypeListRenderer implements ListCellRenderer
    {
        private ListCellRenderer delegate;
        private Icon icon;

        public GraticuleTypeListRenderer(ListCellRenderer delegate, Icon icon)
        {
            this.delegate = delegate;
            this.icon = icon;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            Component c = this.delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c != null && c instanceof JLabel)
            {
                JLabel label = (JLabel) c;
                Font font = label.getFont();
                if (font != null)
                {
                    label.setFont(font.deriveFont(Font.BOLD));
                }
                if (this.icon != null)
                {
                    label.setIcon(this.icon);
                }
                if (value != null && value instanceof String)
                {
                    String graticuleType = (String) value;
                    String labelText = getGraticuleLabel(graticuleType);
                    label.setText(labelText);
                }
            }
            return c;
        }
    }
}
