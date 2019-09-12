/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;

/**
 * @author dcollins
 * @version $Id: SlideShowAnnotation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SlideShowAnnotation extends DialogAnnotation
{
    public static final String INCREASE = "SlideShowAnnotation.Increase";
    public static final String DECREASE = "SlideShowAnnotation.Decrease";

    protected static final String PLAY_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-play.png";
    protected static final String PAUSE_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-pause.png";
    protected static final String PREVIOUS_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-previous.png";
    protected static final String NEXT_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-next.png";
    protected static final String BEGIN_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-start.png";
    protected static final String END_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/16x16-button-end.png";
    protected static final String INCREASE_IMAGE_PATH
        = "gov/nasa/worldwindx/examples/images/16x16-button-window-increase.png";
    protected static final String DECREASE_IMAGE_PATH
        = "gov/nasa/worldwindx/examples/images/16x16-button-window-decrease.png";
    protected static final String BUSY_IMAGE_PATH = "images/indicator-32.gif";

    protected static final String PLAY_TOOLTIP_TEXT = "Play slide show";
    protected static final String PAUSE_TOOLTIP_TEXT = "Pause slide show";
    protected static final String PREVIOUS_TOOLTIP_TEXT = "Previous image";
    protected static final String NEXT_TOOLTIP_TEXT = "Next image";
    protected static final String BEGIN_TOOLTIP_TEXT = "First image";
    protected static final String END_TOOLTIP_TEXT = "Last image";
    protected static final String INCREASE_TOOLTIP_TEXT = "Increase slide show size";
    protected static final String DECREASE_TOOLTIP_TEXT = "Decrease slide show size";

    protected Annotation titleLabel;
    protected Annotation positionLabel;
    protected ImageAnnotation imageAnnotation;
    protected ButtonAnnotation playButton;
    protected ButtonAnnotation previousButton;
    protected ButtonAnnotation nextButton;
    protected ButtonAnnotation beginButton;
    protected ButtonAnnotation endButton;
    protected ButtonAnnotation sizeButton;

    public SlideShowAnnotation(Position position)
    {
        super(position);
    }

    public Annotation getTitleLabel()
    {
        return this.titleLabel;
    }

    public Annotation getPositionLabel()
    {
        return this.positionLabel;
    }

    public ImageAnnotation getImageAnnotation()
    {
        return this.imageAnnotation;
    }

    public ButtonAnnotation getPlayButton()
    {
        return this.playButton;
    }

    public ButtonAnnotation getPreviousButton()
    {
        return this.previousButton;
    }

    public ButtonAnnotation getNextButton()
    {
        return this.nextButton;
    }

    public ButtonAnnotation getBeginButton()
    {
        return this.beginButton;
    }

    public ButtonAnnotation getEndButton()
    {
        return this.endButton;
    }

    public ButtonAnnotation getSizeButton()
    {
        return this.sizeButton;
    }

    @SuppressWarnings( {"StringEquality"})
    public void setPlayButtonState(String state)
    {
        if (state == AVKey.PLAY)
        {
            this.playButton.setImageSource(PLAY_IMAGE_PATH);
            this.playButton.setToolTipText(PLAY_TOOLTIP_TEXT);
        }
        else if (state == AVKey.PAUSE)
        {
            this.playButton.setImageSource(PAUSE_IMAGE_PATH);
            this.playButton.setToolTipText(PAUSE_TOOLTIP_TEXT);
        }
    }

    @SuppressWarnings( {"StringEquality"})
    public void setSizeButtonState(String state)
    {
        if (state == INCREASE)
        {
            this.sizeButton.setImageSource(INCREASE_IMAGE_PATH);
            this.sizeButton.setToolTipText(INCREASE_TOOLTIP_TEXT);
        }
        else if (state == DECREASE)
        {
            this.sizeButton.setImageSource(DECREASE_IMAGE_PATH);
            this.sizeButton.setToolTipText(DECREASE_TOOLTIP_TEXT);
        }
    }

    //**************************************************************//
    //********************  Annotation Components  *****************//
    //**************************************************************//

    protected void initComponents()
    {
        super.initComponents();

        this.titleLabel = new ScreenAnnotation("", new java.awt.Point());
        this.positionLabel = new ScreenAnnotation("", new java.awt.Point());
        this.imageAnnotation = new ImageAnnotation();
        this.playButton = new ButtonAnnotation(PLAY_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.previousButton = new ButtonAnnotation(PREVIOUS_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.nextButton = new ButtonAnnotation(NEXT_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.beginButton = new ButtonAnnotation(BEGIN_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.endButton = new ButtonAnnotation(END_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.sizeButton = new ButtonAnnotation(INCREASE_IMAGE_PATH, DEPRESSED_MASK_PATH);
        this.getBusyImage().setImageSource(BUSY_IMAGE_PATH);

        this.setupTitle(this.titleLabel);
        this.setupPositionLabel(this.positionLabel);
        this.setupImage(this.imageAnnotation);

        this.playButton.setActionCommand(AVKey.PLAY);
        this.previousButton.setActionCommand(AVKey.PREVIOUS);
        this.nextButton.setActionCommand(AVKey.NEXT);
        this.beginButton.setActionCommand(AVKey.BEGIN);
        this.endButton.setActionCommand(AVKey.END);
        this.sizeButton.setActionCommand(AVKey.RESIZE);

        this.playButton.addActionListener(this);
        this.previousButton.addActionListener(this);
        this.nextButton.addActionListener(this);
        this.beginButton.addActionListener(this);
        this.endButton.addActionListener(this);
        this.sizeButton.addActionListener(this);

        this.playButton.setToolTipText(PLAY_TOOLTIP_TEXT);
        this.previousButton.setToolTipText(PREVIOUS_TOOLTIP_TEXT);
        this.nextButton.setToolTipText(NEXT_TOOLTIP_TEXT);
        this.beginButton.setToolTipText(BEGIN_TOOLTIP_TEXT);
        this.endButton.setToolTipText(END_TOOLTIP_TEXT);
        this.sizeButton.setToolTipText(INCREASE_TOOLTIP_TEXT);
    }

    protected void layoutComponents()
    {
        super.layoutComponents();

        Annotation controlsContainer = new ScreenAnnotation("", new java.awt.Point());
        {
            this.setupContainer(controlsContainer);
            controlsContainer.setLayout(new AnnotationFlowLayout(AVKey.HORIZONTAL, AVKey.CENTER, 6, 0)); // hgap, vgap
            controlsContainer.addChild(this.beginButton);
            controlsContainer.addChild(this.previousButton);
            controlsContainer.addChild(this.playButton);
            controlsContainer.addChild(this.nextButton);
            controlsContainer.addChild(this.endButton);
        }

        Annotation contentContainer = new ScreenAnnotation("", new java.awt.Point());
        {
            this.setupContainer(contentContainer);
            contentContainer.setLayout(new AnnotationFlowLayout(AVKey.VERTICAL, AVKey.CENTER, 0, 16)); // hgap, vgap
            contentContainer.addChild(this.titleLabel);
            contentContainer.addChild(this.imageAnnotation);
            contentContainer.addChild(controlsContainer);
        }

        AnnotationNullLayout layout = (AnnotationNullLayout) this.getLayout();
        this.addChild(contentContainer);
        this.addChild(this.positionLabel);
        this.addChild(this.sizeButton);
        // Force the busy image to draw on top of its siblings.
        this.removeChild(this.getBusyImage());
        this.addChild(this.getBusyImage());
        layout.setConstraint(this.positionLabel, AVKey.SOUTHWEST);
        layout.setConstraint(this.sizeButton, AVKey.SOUTHEAST);
        layout.setConstraint(this.getBusyImage(), AVKey.CENTER);
    }

    protected void setupTitle(Annotation annotation)
    {
        this.setupLabel(annotation);

        AnnotationAttributes attribs = annotation.getAttributes();
        attribs.setFont(java.awt.Font.decode("Arial-BOLD-14"));
        attribs.setSize(new java.awt.Dimension(260, 0));
        attribs.setTextAlign(AVKey.CENTER);
    }

    protected void setupPositionLabel(Annotation annotation)
    {
        this.setupLabel(annotation);

        AnnotationAttributes attribs = annotation.getAttributes();
        attribs.setFont(java.awt.Font.decode("Arial-BOLD-12"));
    }

    protected void setupImage(ImageAnnotation annotation)
    {
        AnnotationAttributes attribs = annotation.getAttributes();
        attribs.setBorderWidth(2);
        attribs.setBorderColor(new java.awt.Color(60, 60, 60));

        annotation.setFitSizeToImage(false);
        annotation.setUseImageAspectRatio(true);
    }
}
