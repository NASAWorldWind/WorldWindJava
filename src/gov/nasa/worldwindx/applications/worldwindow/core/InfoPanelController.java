/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.applications.worldwindow.features.AbstractFeature;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import java.awt.*;

/**
 * @author tag
 * @version $Id: InfoPanelController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class InfoPanelController extends AbstractFeature implements SelectListener
{
    protected static final String HARD_SPACE = "\u00a0";
    protected static final String INDENT = "\u00a0\u00a0\u00a0\u00a0";
    protected int maxLineLength = 100;

    protected AnnotationLayer annotationLayer;
    protected ScreenAnnotation annotationPanel;

    public InfoPanelController(Registry registry)
    {
        super("Info Panel", Constants.FEATURE_INFO_PANEL_CONTROLLER, null, registry);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.controller.getWWd().addSelectListener(this);
    }

    protected AVList lastSelectedObject;

    public void selected(SelectEvent event)
    {
        try
        {
            if (event.isRollover())
            {
                if (this.lastSelectedObject == event.getTopObject())
                    return; // same thing selected

                if (this.lastSelectedObject != null)
                {
                    this.hideAnnotationPanel();
                    this.lastSelectedObject = null;
                }

                if (event.getTopObject() != null && event.getTopObject() instanceof AVList)
                {
                    String annoText = ((AVList) event.getTopObject()).getStringValue(Constants.INFO_PANEL_TEXT);
                    if (!WWUtil.isEmpty(annoText))
                    {
                        this.lastSelectedObject = (AVList) event.getTopObject();
                        this.showAnnotationPanel(annoText);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected void showAnnotationPanel(String annoText)
    {
        String text = this.splitLines(annoText);

        AnnotationAttributes attrs = this.getAnnotationPanelAttributes(text);
        int yOffset = Math.min(this.controller.getWWPanel().getSize().height - attrs.getSize().height, 250);
        Point location = new Point(10 + attrs.getSize().width / 2, yOffset);

        if (this.annotationPanel != null)
            this.annotationPanel.setAttributes(this.getAnnotationPanelAttributes(text));
        else
            this.annotationPanel = new ScreenAnnotation(annoText, location, getAnnotationPanelAttributes(text));

        this.annotationPanel.setScreenPoint(location);
        this.annotationPanel.setText(text);

        if (this.annotationLayer == null)
        {
            this.annotationLayer = new AnnotationLayer();
            this.annotationLayer.setPickEnabled(false);
        }

        this.annotationLayer.removeAllAnnotations();
        this.annotationLayer.addAnnotation(this.annotationPanel);
        if (!this.controller.getActiveLayers().contains(this.annotationLayer))
            this.controller.addInternalLayer(this.annotationLayer);
    }

    protected void hideAnnotationPanel()
    {
        if (this.annotationLayer != null)
        {
            this.annotationLayer.removeAllAnnotations();
            this.controller.getActiveLayers().remove(this.annotationLayer);
            this.annotationLayer.dispose();
            this.annotationLayer = null;
        }

        if (this.annotationPanel != null)
        {
            this.annotationPanel.dispose();
            this.annotationPanel = null;
        }
    }

    protected AnnotationAttributes getAnnotationPanelAttributes(String annoText)
    {
        AnnotationAttributes attrs = new AnnotationAttributes();

        attrs.setAdjustWidthToText(AVKey.SIZE_FIXED);
        attrs.setSize(this.computePanelSize(annoText));
        attrs.setFrameShape(AVKey.SHAPE_RECTANGLE);
        attrs.setTextColor(Color.WHITE);
        attrs.setBackgroundColor(new Color(0f, 0f, 0f, 0.6f));
        attrs.setCornerRadius(10);
        attrs.setInsets(new Insets(10, 10, 0, 0));
        attrs.setBorderColor(new Color(0xababab));
        attrs.setFont(Font.decode("Arial-PLAIN-12"));
        attrs.setTextAlign(AVKey.LEFT);

        return attrs;
    }

    /**
     * Determine the panel size needed to display the full annotation.
     *
     * @param annoText the annotation text.
     *
     * @return the required panel size.
     */
    protected Dimension computePanelSize(String annoText)
    {
        Dimension lengths = this.computeLengths(annoText);

        // The numbers used below are the average width of a character and average height of a line in Arial-Plain-12.
        int width = 7 * Math.min(lengths.width, this.maxLineLength);
        int height = lengths.height * 17;

        return new Dimension(width, height);
    }

    /**
     * Determine the number of lines in the annotation text and the length of the longest line.
     *
     * @param annoText the annotation text.
     *
     * @return the length of the longest line (width) and number of lines (height).
     */
    protected Dimension computeLengths(String annoText)
    {
        String[] lines = Util.splitLines(annoText);
        int lineLength = 0;
        for (String line : lines)
        {
            if (line.length() > lineLength)
                lineLength = line.length();
        }

        return new Dimension(lineLength + 5, lines.length + 1); // the 5 and 1 account for slight sizing discrepancies
    }

    /**
     * Split a collection of lines into a new collection whose lines are all less than the maximum allowed line length.
     *
     * @param origText the original lines.
     *
     * @return the new lines.
     */
    protected String splitLines(String origText)
    {
        StringBuilder newText = new StringBuilder();

        String[] lines = Util.splitLines(origText);
        for (String line : lines)
        {
            // Append the line to the output buffer if it's within size, other wise split it and append the result.
            newText.append(line.length() <= this.maxLineLength ? line : this.splitLine(line)).append("\n");
        }

        return newText.toString();
    }

    /**
     * Split a long line into several lines.
     *
     * @param origLine the original line
     *
     * @return a string with new-line characters at the line-split locations.
     */
    protected String splitLine(String origLine)
    {
        StringBuilder newLines = new StringBuilder();

        // Determine the line's current indent. Any indent added below must be added to it.
        String currentIndent = "";
        for (int i = 0; i < origLine.length(); i++)
        {
            if (origLine.charAt(i) == '\u00a0')
                currentIndent += HARD_SPACE;
            else
                break;
        }

        // Add the words of the line to a line builder until adding a word would exceed the max allowed length.
        StringBuilder line = new StringBuilder(currentIndent);
        String[] words = Util.splitWords(origLine, "[\u00a0 ]"); // either hard or soft space
        for (String word : words)
        {
            if (line.length() + 1 + word.length() + currentIndent.length() > this.maxLineLength)
            {
                if (newLines.length() == 0)
                    currentIndent += INDENT; // indent continuation lines
                newLines.append(line.toString());
                line = new StringBuilder("\n").append(currentIndent);
            }

            // Add a space in front of the word if it's not the first word.
            if (!line.toString().endsWith(HARD_SPACE))
                line.append(HARD_SPACE);
            line.append(word);
        }

        // Add the final words to the split lines.
        if (line.length() > 1)
            newLines.append(line.toString());

        return newLines.toString();
    }
}
