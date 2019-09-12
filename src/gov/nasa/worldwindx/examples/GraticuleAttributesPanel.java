/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author dcollins
 * @version $Id: GraticuleAttributesPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings("unchecked")
public class GraticuleAttributesPanel extends JPanel
{
    // Line attribute components.
    private JPanel linePanel;
    private ColorPanel lineColorPanel;
    private JSlider lineWidthSlider;
    private JSpinner lineWidthSpinner;
    private SpinnerNumberModel lineWidthSpinnerModel;
    private JComboBox lineStyle;
    // Label attribute components.
    private JPanel labelPanel;
    private ColorPanel labelColorPanel;
    private JCheckBox labelEnabled;
    private JComboBox labelFontName;
    private JComboBox labelFontStyle;
    private JComboBox labelFontSize;

    private static final int MIN_LINE_WIDTH = 1;
    private static final int MAX_LINE_WIDTH = 8;
    private static final int LINE_WIDTH_SCALE = 16;

    public static final String LINE_COLOR_PROPERTY = "LineColor";
    public static final String LINE_WIDTH_PROPERTY = "LineWidth";
    public static final String LINE_STYLE_PROPERTY = "LineStyle";
    public static final String LABEL_ENABLED_PROPERTY = "LabelEnabled";
    public static final String LABEL_COLOR_PROPERTY = "LabelColor";    
    public static final String LABEL_FONT_PROPERTY = "LabelFont";

    public GraticuleAttributesPanel()
    {
        makeComponents();
        layoutComponents();
    }

    public Color getSelectedLineColor()
    {
        return this.lineColorPanel.getColor();
    }

    public void setSelectedLineColor(Color value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.lineColorPanel.setColor(value);
    }

    public double getSelectedLineWidth()
    {
        return this.lineWidthSpinnerModel.getNumber().doubleValue();
    }

    public void setSelectedLineWidth(double value)
    {
        setLineWidthControls(value);
    }

    public String getSelectedLineStyle()
    {
        return this.lineStyle.getSelectedItem().toString();
    }

    public void setSelectedLineStyle(String value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.lineStyle.setSelectedItem(value);
    }

    public boolean isLabelEnableSelected()
    {
        return this.labelEnabled.isSelected();
    }

    public void setLabelEnableSelected(boolean b)
    {
        this.labelEnabled.setSelected(b);
    }

    public Color getSelectedLabelColor()
    {
        return this.labelColorPanel.getColor();
    }

    public void setSelectedLabelColor(Color value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.labelColorPanel.setColor(value);
    }

    public Font getSelectedLabelFont()
    {
        return makeFontFromControls();
    }

    public void setSelectedLabelFont(Font value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setFontControls(value);
    }

    private void onLineColorChanged(PropertyChangeEvent event)
    {
        if (event != null)
        {
            firePropertyChange(LINE_COLOR_PROPERTY, null, event.getNewValue());
        }
    }

    private void onLineWidthSliderChanged(ChangeEvent event)
    {
        if (event != null)
        {
            double width = this.lineWidthSlider.getValue() / (double) LINE_WIDTH_SCALE;
            this.lineWidthSpinner.setValue(width);
            firePropertyChange(LINE_WIDTH_PROPERTY, null, width);
        }
    }

    private void onLineWidthSpinnerChanged(ChangeEvent event)
    {
        if (event != null)
        {
            double width = this.lineWidthSpinnerModel.getNumber().doubleValue();
            this.lineWidthSlider.setValue((int) (width * LINE_WIDTH_SCALE));
            firePropertyChange(LINE_WIDTH_PROPERTY, null, width);
        }
    }

    private void onLineStyleChanged(ActionEvent event)
    {
        if (event != null)
        {
            String style = this.lineStyle.getSelectedItem().toString();
            firePropertyChange(LINE_STYLE_PROPERTY, null, style);
        }
    }

    private void onLabelEnableChanged(ItemEvent event)
    {
        if (event != null)
        {
            firePropertyChange(LABEL_ENABLED_PROPERTY, null, event.getStateChange() == ItemEvent.SELECTED);
        }
    }

    private void onLabelColorChanged(PropertyChangeEvent event)
    {
        if (event != null)
        {
            firePropertyChange(LABEL_COLOR_PROPERTY, null, event.getNewValue());
        }
    }

    private void onLabelFontChanged(ActionEvent event)
    {
        if (event != null)
        {
            Font font = makeFontFromControls();
            firePropertyChange(LABEL_FONT_PROPERTY, null, font);
        }
    }

    private void setLineWidthControls(double width)
    {
        this.lineWidthSlider.setValue((int) (width * LINE_WIDTH_SCALE));
        this.lineWidthSpinner.setValue(width);
    }

    private Font makeFontFromControls()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.labelFontName.getSelectedItem());
        sb.append("-");
        sb.append(this.labelFontStyle.getSelectedItem());
        sb.append("-");
        sb.append(this.labelFontSize.getSelectedItem());
        return Font.decode(sb.toString());
    }

    private void setFontControls(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Apply the font name.
        this.labelFontName.setSelectedItem(font.getName());
        // Apply the font style.
        if ((font.getStyle() & Font.BOLD) != 0)
            this.labelFontStyle.setSelectedItem("Bold");
        else if ((font.getStyle() & Font.ITALIC) != 0)
            this.labelFontStyle.setSelectedItem("Italic");
        else if ((font.getStyle() & (Font.BOLD|Font.ITALIC)) != 0)
            this.labelFontStyle.setSelectedItem("BoldItalic");
        else
            this.labelFontStyle.setSelectedItem("Plain");
        // Apply the font size.
        this.labelFontSize.setSelectedItem(String.format("%d", font.getSize()));
    }

    private void makeComponents()
    {
        //---------- Line Properties ----------//
        {
            String[] lineStyles = new String[] {
                MGRSGraticuleLayer.LINE_STYLE_SOLID,
                MGRSGraticuleLayer.LINE_STYLE_DASHED,
                MGRSGraticuleLayer.LINE_STYLE_DOTTED
            };

            this.linePanel = new JPanel();
            //this.lineEnabled = new JCheckBox("Show Graticule");
            this.lineColorPanel = new ColorPanel();
            //noinspection PointlessArithmeticExpression
            this.lineWidthSlider = new JSlider(
                MIN_LINE_WIDTH * LINE_WIDTH_SCALE,  // min
                MAX_LINE_WIDTH * LINE_WIDTH_SCALE); // max
            this.lineWidthSlider.setMajorTickSpacing(LINE_WIDTH_SCALE);
            this.lineWidthSlider.setMinorTickSpacing(LINE_WIDTH_SCALE / 4);
            this.lineWidthSlider.setPaintTicks(true);
            this.lineWidthSlider.setSnapToTicks(true);
            this.lineWidthSpinnerModel = new SpinnerNumberModel(
                (double) MIN_LINE_WIDTH, // value
                (double) MIN_LINE_WIDTH, // min
                (double) MAX_LINE_WIDTH, // max
                4.0 / (double) LINE_WIDTH_SCALE); // stepsize
            this.lineWidthSpinner = new JSpinner(this.lineWidthSpinnerModel);
            this.lineStyle = new JComboBox(lineStyles);
            ListCellRenderer originalRenderer = this.lineStyle.getRenderer();
            this.lineStyle.setRenderer(new LineStyleRenderer(originalRenderer));

            //this.lineEnabled.addItemListener(new ItemListener() {
            //    public void itemStateChanged(ItemEvent event) {
            //        onLineEnableChanged(event);
            //    }
            //});
            this.lineColorPanel.addColorChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    onLineColorChanged(event);
                }
            });
            this.lineWidthSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    onLineWidthSliderChanged(event);
                }
            });
            this.lineWidthSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    onLineWidthSpinnerChanged(event);
                }
            });
            this.lineStyle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onLineStyleChanged(event);
                }
            });
        }

        //---------- Label Properties ----------//
        {
            this.labelPanel = new JPanel();
            this.labelEnabled = new JCheckBox("Show Labels");
            this.labelColorPanel = new ColorPanel();
            this.labelFontName = new JComboBox(new String[] {"Arial", "SansSerif", "Serif", "Courier", "Times", "Helvetica", "Trebuchet", "Tahoma"});
            this.labelFontStyle = new JComboBox(new String[] {"Plain", "Bold", "Italic", "BoldItalic"});
            this.labelFontSize = new JComboBox(new String[] {"8", "10", "12", "14", "16", "18", "20", "24", "28", "34", "48", "64"});

            this.labelEnabled.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent event) {
                    onLabelEnableChanged(event);
                }
            });
            this.labelColorPanel.addColorChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    onLabelColorChanged(event);
                }
            });
            this.labelFontName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onLabelFontChanged(event);
                }
            });
            this.labelFontStyle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onLabelFontChanged(event);
                }
            });
            this.labelFontSize.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onLabelFontChanged(event);
                }
            });
        }
    }

    private void layoutComponents()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //---------- Line Properties ----------//
        {
            this.linePanel.setLayout(new BoxLayout(this.linePanel, BoxLayout.PAGE_AXIS));
            this.linePanel.setBorder(new CompoundBorder(new TitledBorder("Graticule"), new EmptyBorder(10, 10, 10, 10)));

            //this.lineEnabled.setAlignmentX(Component.LEFT_ALIGNMENT);
            //this.linePanel.add(this.lineEnabled);
            //this.linePanel.add(Box.createVerticalStrut(10));

            this.lineColorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.linePanel.add(this.lineColorPanel);
            this.linePanel.add(Box.createVerticalStrut(10));

            Box hbox = Box.createHorizontalBox();
            hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            hbox.add(this.lineWidthSlider);
            hbox.add(this.lineWidthSpinner);
            hbox.add(Box.createHorizontalGlue());
            this.linePanel.add(hbox);
            this.linePanel.add(Box.createVerticalStrut(10));

            this.lineStyle.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.linePanel.add(this.lineStyle);
        }
        this.linePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(this.linePanel);
        add(Box.createVerticalStrut(20));

        //---------- Label Properties ----------//
        {
            this.labelPanel.setLayout(new BoxLayout(this.labelPanel, BoxLayout.PAGE_AXIS));
            this.labelPanel.setBorder(new CompoundBorder(new TitledBorder("Labels"), new EmptyBorder(10, 10, 10, 10)));

            this.labelEnabled.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.labelPanel.add(this.labelEnabled);
            this.labelPanel.add(Box.createVerticalStrut(10));

            this.labelColorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.labelPanel.add(this.labelColorPanel);
            this.labelPanel.add(Box.createVerticalStrut(10));

            Box hbox = Box.createHorizontalBox();
            hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            hbox.add(this.labelFontName);
            hbox.add(this.labelFontStyle);
            hbox.add(this.labelFontSize);
            this.labelPanel.add(hbox);
        }
        this.labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(this.labelPanel);

        add(Box.createVerticalGlue());
    }

    private static String getLineStyleLabel(String lineStyle)
    {
        String labelText = null;
        if (MGRSGraticuleLayer.LINE_STYLE_SOLID.equals(lineStyle))
            labelText = "Solid";
        else if (MGRSGraticuleLayer.LINE_STYLE_DASHED.equals(lineStyle))
            labelText = "Dashed";
        else if (MGRSGraticuleLayer.LINE_STYLE_DOTTED.equals(lineStyle))
            labelText = "Dotted";
        return labelText;
    }

    private static class LineStyleRenderer implements ListCellRenderer
    {
        private ListCellRenderer delegate;

        public LineStyleRenderer(ListCellRenderer delegate)
        {
            this.delegate = delegate;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = this.delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c != null && c instanceof JLabel)
            {
                JLabel label = (JLabel) c;
                if (value != null && value instanceof String)
                {
                    String lineStyle = (String) value;
                    String labelText = getLineStyleLabel(lineStyle);
                    label.setText(labelText);
                }
            }
            return c;
        }
    }

    private static class ColorPanel extends JPanel
    {
        private JLabel colorLabel;
        private JButton colorButton;
        private JColorChooser colorChooser;
        private JDialog colorChooserDialog;
        private JSlider opacitySlider;
        private Color lastSelectedColor = null;
        private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

        public ColorPanel()
        {
            makeComponents();
            layoutComponents();
        }

        public void addColorChangeListener(PropertyChangeListener propertyChangeListener)
        {
            this.changeSupport.addPropertyChangeListener(propertyChangeListener);
        }

        public void removeColorChangeListener(PropertyChangeListener propertyChangeListener)
        {
            this.changeSupport.removePropertyChangeListener(propertyChangeListener);
        }

        public void fireColorChanged()
        {
            this.changeSupport.firePropertyChange("color", null, makeColorFromControls());
        }

        public Color getColor()
        {
            return makeColorFromControls();
        }

        public void setColor(Color color)
        {
            if (color == null)
            {
                String message = Logging.getMessage("nullValue.ColorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            setColorControls(color);
        }

        private Color makeColorFromControls()
        {
            Color rgb = this.colorChooser.getColor();
            int a = this.opacitySlider.getValue();
            return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), a);
        }

        private void setColorControls(Color color)
        {
            if (color == null)
            {
                String message = Logging.getMessage("nullValue.ColorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.colorChooser.setColor(color);
            this.opacitySlider.setValue(color.getAlpha());
            setColorLabel(color);
        }

        private void onColorPressed()
        {
            this.lastSelectedColor = makeColorFromControls();
            this.colorChooserDialog.setVisible(true);
        }

        private void onColorChooserOk(ActionEvent event)
        {
            if (event != null)
            {
                this.lastSelectedColor = null;
                Color color = makeColorFromControls();
                setColorLabel(color);
                if (color != null)
                {
                    fireColorChanged();
                }
            }
        }

        private void onColorChooserCancel(ActionEvent event)
        {
            if (event != null)
            {
                Color color = this.lastSelectedColor;
                if (color != null)
                {
                    setColorControls(color);
                    fireColorChanged();
                }
            }
        }

        private void onColorChooserChanged(ChangeEvent event)
        {
            if (event != null)
            {
                Color color = makeColorFromControls();
                setColorLabel(color);
                if (color != null)
                {
                    fireColorChanged();
                }
            }
        }

        private void onOpacityChanged(ChangeEvent event)
        {
            if (event != null)
            {
                Color color = makeColorFromControls();
                setColorLabel(color);
                if (color != null)
                {
                    fireColorChanged();
                }
            }
        }

        private void makeComponents()
        {
            this.colorLabel = new JLabel(makeImageIcon(60, 16));
            this.colorButton = new JButton("Choose...");
            this.colorChooser = new JColorChooser();
            // Replace the color "preview panel" with an empty panel.
            // We will be previewing color changes in the WorldWindow.
            this.colorChooser.setPreviewPanel(new JPanel());
            this.opacitySlider = new JSlider(
                1,    // min
                255); // max
            this.colorChooserDialog = JColorChooser.createDialog(this, "Choose Graticule Color", true, this.colorChooser,
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onColorChooserOk(event);
                    }
                },
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onColorChooserCancel(event);
                    }
                });

            this.colorLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    onColorPressed();
                }
            });
            this.colorButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onColorPressed();
                }
            });
            this.colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    onColorChooserChanged(event);
                }
            });
            this.opacitySlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    onOpacityChanged(event);
                }
            });
        }

        private void setColorLabel(Color color)
        {
            if (color != null
                && this.colorLabel != null
                && this.colorLabel.getIcon() != null
                && this.colorLabel.getIcon() instanceof ImageIcon)
            {
                ImageIcon icon = (ImageIcon) this.colorLabel.getIcon();
                if (icon.getImage() != null)
                {
                    // We only want to represent the RGB color components
                    // on this label.
                    Color rgb = new Color(color.getRGB());
                    fillImage(icon.getImage(), rgb);
                    this.colorLabel.repaint();
                }
            }
        }

        private void layoutComponents()
        {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            Box hbox = Box.createHorizontalBox();
            hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.colorLabel.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
            hbox.add(this.colorLabel);
            hbox.add(Box.createHorizontalStrut(5));
            hbox.add(this.colorButton);
            add(hbox);
            add(Box.createVerticalStrut(10));

            this.opacitySlider.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(this.opacitySlider);
        }
    }

    private static ImageIcon makeImageIcon(int width, int height)
    {
        ImageIcon icon = null;
        try
        {
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            icon = new ImageIcon(bi);
        }
        catch (Exception e)
        {
            String message = "Exception while creating icon";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return icon;
    }

    private static void fillImage(Image image, Color color)
    {
        try
        {
            Graphics g = image.getGraphics();
            g.setColor(color);
            g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
        }
        catch (Exception e)
        {
            String message = "Exception while drawing to image";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }
}
