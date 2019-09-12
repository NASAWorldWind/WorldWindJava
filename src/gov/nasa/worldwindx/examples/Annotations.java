/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.PowerOfTwoPaddedImage;

import com.jogamp.opengl.*;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.URL;

/**
 * Illustrates how to use a WorldWind <code>{@link Annotation}</code> to display on-screen information to the user in
 * the form of a text label with an optional image. Annotations may be attached to a geographic position or a point on
 * the screen. They provide support for multi-line text, simple HTML text markup, and many styling attributes such as
 * font face, size and colors, background shape and background image.
 *
 * @author Patrick Murris
 * @version $Id: Annotations.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Annotations extends ApplicationTemplate
{
    @SuppressWarnings("unchecked")
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        // Above mean sea level globe annotation
        private class AMSLGlobeAnnotation extends GlobeAnnotation
        {
            public AMSLGlobeAnnotation(String text, Position position)
            {
                super(text, position);
            }

            public Vec4 getAnnotationDrawPoint(DrawContext dc)
            {
                return dc.getGlobe().computePointFromPosition(this.getPosition().getLatitude(),
                    this.getPosition().getLongitude(),
                    this.getPosition().getElevation() * dc.getVerticalExaggeration());
            }
        }

        private AnnotationLayer layer;
        private Annotation currentAnnotation;

        // Static
        private final static PowerOfTwoPaddedImage IMAGE_WWJ_SPLASH =
            PowerOfTwoPaddedImage.fromPath("images/400x230-splash-nww.png");
        private final static PowerOfTwoPaddedImage IMAGE_NASA =
            PowerOfTwoPaddedImage.fromPath("images/32x32-icon-nasa.png");
        private final static PowerOfTwoPaddedImage IMAGE_EARTH =
            PowerOfTwoPaddedImage.fromPath("images/32x32-icon-earth.png");

        // UI components
        private JTextArea inputTextArea;
        private JCheckBox cbAdjustWidth;
        private JSlider widthSlider, heightSlider;
        private JSlider scaleSlider, opacitySlider, cornerRadiusSlider, borderWidthSlider, stippleFactorSlider;
        private JComboBox cbFontName, cbFontStyle, cbFontSize, cbTextAlign, cbShape, cbLeader;
        private JComboBox cbImage, cbImageRepeat, cbTextEffect;
        private JSlider leaderGapWidthSlider;
        private JSlider imageOpacitySlider, imageScaleSlider, imageOffsetXSlider, imageOffsetYSlider;
        private JSlider offsetXSlider, offsetYSlider;
        private JSlider distanceMinScaleSlider, distanceMaxScaleSlider, distanceMinOpacitySlider;
        private JSlider highlightScaleSlider;
        private JSpinner insetsTop, insetsRight, insetsBottom, insetsLeft;
        private JButton btTextColor, btBackColor, btBorderColor;
        private JComboBox cbTextColorAlpha, cbBackColorAlpha, cbBorderColorAlpha;
        private JButton btApply, btRemove;

        private boolean suspendUpdate = false;
        private Color savedBorderColor;
        private BufferedImage savedImage;

        private Annotation lastPickedObject;

        public AppFrame()
        {
            // Create example annotations
            this.setupAnnotations();

            // Add control panel
            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);
            this.enableControlPanel(false);

            // Add a select listener to select or highlight annotations on rollover
            this.setupSelection();
        }

        private void setupAnnotations()
        {
            GlobeAnnotation ga;

            // Create a renderable layer
            RenderableLayer rl = new RenderableLayer();
            rl.setName("Annotations (stand alone)");
            insertBeforeCompass(this.getWwd(), rl);

            // Add above ground level annotation with fixed height in real world
            ga = new GlobeAnnotation("AGL Annotation\nElev 1000m", Position.fromDegrees(10, 25, 1000));
            ga.setHeightInMeter(10e3); // ten kilometer hight
            rl.addRenderable(ga);

            // Add above mean sea level annotation with fixed height in real world
            AMSLGlobeAnnotation amsla = new AMSLGlobeAnnotation("AMSL Annotation\nAlt 1000m",
                Position.fromDegrees(10, 20, 1000));
            amsla.setHeightInMeter(10e3); // ten kilometer hight
            rl.addRenderable(amsla);

            // Create an annotation with an image and some text below it
            ga = this.makeTopImageBottomTextAnnotation(IMAGE_WWJ_SPLASH, "Text below image",
                Position.fromDegrees(0, -40, 0));
            rl.addRenderable(ga);

            // Create an AnnotationLayer with lots of annotations
            this.layer = new AnnotationLayer();

            // Create default attributes
            AnnotationAttributes defaultAttributes = new AnnotationAttributes();
            defaultAttributes.setCornerRadius(10);
            defaultAttributes.setInsets(new Insets(8, 8, 8, 8));
            defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
            defaultAttributes.setTextColor(Color.WHITE);
            defaultAttributes.setDrawOffset(new Point(25, 25));
            defaultAttributes.setDistanceMinScale(.5);
            defaultAttributes.setDistanceMaxScale(2);
            defaultAttributes.setDistanceMinOpacity(.5);
            defaultAttributes.setLeaderGapWidth(14);
            defaultAttributes.setDrawOffset(new Point(20, 40));

            // Add some annotations to the layer
            // NOTE: use unicode for annotation text

            // Towns
            AnnotationAttributes townAttr = new AnnotationAttributes();
            townAttr.setDefaults(defaultAttributes);
            townAttr.setFont(Font.decode("Arial-BOLD-12"));
            layer.addAnnotation(new GlobeAnnotation("MONACO", Position.fromDegrees(43.7340, 7.4211, 0), townAttr));
            layer.addAnnotation(new GlobeAnnotation("NICE", Position.fromDegrees(43.696, 7.27, 0), townAttr));
            layer.addAnnotation(new GlobeAnnotation("ANTIBES", Position.fromDegrees(43.5810, 7.1248, 0), townAttr));
            layer.addAnnotation(new GlobeAnnotation("CANNES", Position.fromDegrees(43.5536, 7.0171, 0), townAttr));
            layer.addAnnotation(new GlobeAnnotation("GRASSE", Position.fromDegrees(43.6590, 6.9240, 0), townAttr));
            layer.addAnnotation(new GlobeAnnotation("FREJUS", Position.fromDegrees(43.4326, 6.7356, 0), townAttr));
            layer.addAnnotation(
                new GlobeAnnotation("SAINTE MAXIME", Position.fromDegrees(43.3087, 6.6353, 0), townAttr));
            layer.addAnnotation(
                new GlobeAnnotation("SAINT TROPEZ", Position.fromDegrees(43.2710, 6.6386, 0), townAttr));
            layer.addAnnotation(new GlobeAnnotation("TOULON", Position.fromDegrees(43.1264, 5.9126, 0), townAttr));
            layer.addAnnotation(new GlobeAnnotation("MARSEILLE", Position.fromDegrees(43.2904, 5.3806, 0), townAttr));
            layer.addAnnotation(
                new GlobeAnnotation("AIX EN PROVENCE", Position.fromDegrees(43.5286, 5.4485, 0), townAttr));
            // Special places
            AnnotationAttributes spAttr = new AnnotationAttributes();
            spAttr.setDefaults(defaultAttributes);
            spAttr.setFont(Font.decode("Arial-BOLDITALIC-10"));
            spAttr.setTextColor(Color.YELLOW);
            layer.addAnnotation(new GlobeAnnotation("A\u00e9roport International\nNice C\u00f4te d'Azur",
                Position.fromDegrees(43.6582, 7.2167, 0), spAttr));
            layer.addAnnotation(new GlobeAnnotation("Sophia Antipolis",
                Position.fromDegrees(43.6222, 7.0474, 0), spAttr));

            // Geographical features - use a common default AnnotationAttributes object
            AnnotationAttributes geoAttr = new AnnotationAttributes();
            geoAttr.setDefaults(defaultAttributes);
            geoAttr.setFrameShape(AVKey.SHAPE_NONE);  // No frame
            geoAttr.setFont(Font.decode("Arial-ITALIC-12"));
            geoAttr.setTextColor(Color.GREEN);
            geoAttr.setTextAlign(AVKey.CENTER);
            geoAttr.setDrawOffset(new Point(0, 5)); // centered just above
            geoAttr.setEffect(AVKey.TEXT_EFFECT_OUTLINE);  // Black outline
            geoAttr.setBackgroundColor(Color.BLACK);
            layer.addAnnotation(new GlobeAnnotation("Mont Chauve\nFort militaire\nAlt: 853m",
                Position.fromDegrees(43.7701, 7.2544, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Mont Agel\nFort militaire\nAlt: 1148m",
                Position.fromDegrees(43.7704, 7.4203, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Cap Ferrat",
                Position.fromDegrees(43.6820, 7.3290, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Gorges du Loup",
                Position.fromDegrees(43.7351, 6.9988, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Cap d'Antibes",
                Position.fromDegrees(43.5526, 7.1297, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Iles de L\u00e9rins",
                Position.fromDegrees(43.5125, 7.0467, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Montagne du Cheiron\nAlt: 1778m",
                Position.fromDegrees(43.8149, 6.9669, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Giens",
                Position.fromDegrees(43.0394, 6.1384, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Iles de Porquerolles",
                Position.fromDegrees(42.9974, 6.2147, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Ile du Levent",
                Position.fromDegrees(43.0315, 6.4702, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Ile de Port Cros",
                Position.fromDegrees(43.0045, 6.3959, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Montagne Sainte Victoire\nAlt: 1010m",
                Position.fromDegrees(43.5319, 5.6120, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Sainte Baume\nAlt: 1147m",
                Position.fromDegrees(43.3373, 5.8008, 0), geoAttr));
            layer.addAnnotation(new GlobeAnnotation("Pic de l'Ours\nAlt: 496m",
                Position.fromDegrees(43.4763, 6.9042, 0), geoAttr));

            // Water bodies - ellipse shape and centered text
            AnnotationAttributes waterAttr = new AnnotationAttributes();
            waterAttr.setDefaults(defaultAttributes);
            waterAttr.setFrameShape(AVKey.SHAPE_ELLIPSE);
            waterAttr.setTextAlign(AVKey.CENTER);
            waterAttr.setFont(Font.decode("Arial-ITALIC-12"));
            waterAttr.setTextColor(Color.CYAN);
            waterAttr.setInsets(new Insets(8, 12, 9, 12));
            layer.addAnnotation(new GlobeAnnotation("Lac de Sainte Croix",
                Position.fromDegrees(43.7720, 6.1879, 0), waterAttr));
            layer.addAnnotation(new GlobeAnnotation("Lac de Castillon",
                Position.fromDegrees(43.9008, 6.5348, 0), waterAttr));
            layer.addAnnotation(new GlobeAnnotation("Lac de Serre Pon\u00e7on",
                Position.fromDegrees(44.5081, 6.3242, 0), waterAttr));

            // Longer text, custom colors and text align
            ga = new GlobeAnnotation("Transition Permien-Trias\nDate: 251Ma \nPlus grand \u00e9pisode "
                + "d'extinction massive.",
                Position.fromDegrees(44.0551, 7.1215, 0), Font.decode("Arial-ITALIC-12"), Color.DARK_GRAY);
            ga.getAttributes().setTextAlign(AVKey.RIGHT);
            ga.getAttributes().setBackgroundColor(new Color(.8f, .8f, .8f, .7f));
            ga.getAttributes().setBorderColor(Color.BLACK);
            layer.addAnnotation(ga);

            // With HTML tags and background image no repeat
            ga = new GlobeAnnotation("<p>\n<b><font color=\"#664400\">LA CLAPI\u00c8RE</font></b><br />\n<i>Alt: "
                + "1100-1700m</i>\n</p>\n<p>\n<b>Glissement de terrain majeur</b> dans la haute Tin\u00e9e, sur "
                + "un flanc du <a href=\"http://www.mercantour.eu\">Parc du Mercantour</a>, Alpes Maritimes.\n</p>\n"
                + "<p>\nRisque aggrav\u00e9 d'<b>inondation</b> du village de <i>Saint \u00c9tienne de Tin\u00e9e</i> "
                + "juste en amont.\n</p>",
                Position.fromDegrees(44.2522, 6.9424, 0), Font.decode("Serif-PLAIN-14"), Color.DARK_GRAY);
            ga.setMinActiveAltitude(10e3);
            ga.setMaxActiveAltitude(1e6);
            ga.getAttributes().setTextAlign(AVKey.RIGHT);
            ga.getAttributes().setBackgroundColor(new Color(1f, 1f, 1f, .7f));
            ga.getAttributes().setBorderColor(Color.BLACK);
            ga.getAttributes().setSize(
                new Dimension(220, 0));  // Preferred max width, no length limit (default max width is 160)
            ga.getAttributes().setImageSource(IMAGE_EARTH.getPowerOfTwoImage());
            ga.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
            ga.getAttributes().setImageOpacity(.6);
            ga.getAttributes().setImageScale(.7);
            ga.getAttributes().setImageOffset(new Point(7, 7));
            layer.addAnnotation(ga);

            // With some border stippling, width and antialias
            ga = new GlobeAnnotation("Latitude: 44.0 N\nLongitude: 7.0 W",
                Position.fromDegrees(44.0000, 7.000, 0), Font.decode("Arial-ITALIC-12"), Color.DARK_GRAY);
            ga.getAttributes().setTextAlign(AVKey.CENTER);
            ga.getAttributes().setBackgroundColor(new Color(.9f, .9f, .8f, .7f));
            ga.getAttributes().setBorderColor(Color.BLACK);
            ga.getAttributes().setBorderWidth(2);
            ga.getAttributes().setBorderStippleFactor(3);
            layer.addAnnotation(ga);

            // With background texture repeat Y
            ga = new GlobeAnnotation("SAHARA DESERT\n\nThe Sahara is technically the world's second largest desert "
                + "after Antarctica.\n\nAt over 9,000,000 square kilometres (3,500,000 sq mi), it covers most parts "
                + "of northern Africa. ", Position.fromDegrees(22, 12, 0), Font.decode("Arial-BOLD-12"));
            ga.getAttributes().setDefaults(defaultAttributes);
            ga.getAttributes().setImageSource(IMAGE_NASA.getPowerOfTwoImage());
            ga.getAttributes().setImageRepeat(AVKey.REPEAT_Y);
            ga.getAttributes().setImageOpacity(.6);
            ga.getAttributes().setImageScale(.7);
            ga.getAttributes().setImageOffset(new Point(1, 1));
            ga.getAttributes().setInsets(new Insets(6, 28, 6, 6));
            layer.addAnnotation(ga);

            // Splash screen with NPOT background texture
            ga = new GlobeAnnotation("Java SDK", Position.fromDegrees(20, 0, 0), Font.decode("Arial-BOLD-14"));
            ga.getAttributes().setTextAlign(AVKey.RIGHT);
            ga.getAttributes().setImageSource(IMAGE_WWJ_SPLASH.getPowerOfTwoImage());
            ga.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
            ga.getAttributes().setImageScale(.5);    // scale texture to half size
            ga.getAttributes().setSize(new Dimension(200, 115));  // use this dimensions (half texture)
            ga.getAttributes().setAdjustWidthToText(
                AVKey.SIZE_FIXED);  // use strict dimension - dont follow text width
            ga.getAttributes().setCornerRadius(0);
            layer.addAnnotation(ga);

            // With background pattern and forced height
            AnnotationAttributes patternAttr = new AnnotationAttributes();
            patternAttr.setDefaults(defaultAttributes);
            patternAttr.setFont(Font.decode("Arial-BOLD-16"));
            patternAttr.setTextColor(Color.GRAY);
            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(10, 100, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.GRADIENT_VLINEAR,
                new Dimension(32, 128), 1f, Color.WHITE, new Color(0f, 0f, 0f, 0f)));  // White to transparent
            ga.getAttributes().setSize(new Dimension(200, 128));  // force height to 128
            layer.addAnnotation(ga);
            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(10, 110, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.GRADIENT_VLINEAR,
                new Dimension(32, 64), 1f, Color.LIGHT_GRAY, Color.WHITE));  // gray/white
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);
            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(10, 120, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.PATTERN_DIAGONAL_UP,
                Color.YELLOW));  // yellow stripes
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);

            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(0, 100, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.GRADIENT_HLINEAR,
                new Dimension(256, 32), 1f, Color.WHITE, new Color(0f, 0f, 0f, 0f)));  // White to transparent
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);
            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(0, 110, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.GRADIENT_HLINEAR,
                new Dimension(32, 64), 1f, Color.LIGHT_GRAY, Color.WHITE));  // gray/white
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);
            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(0, 120, 0), patternAttr);
            ga.getAttributes().setImageSource(
                PatternFactory.createPattern(PatternFactory.PATTERN_SQUARES, Color.YELLOW));  // yellow circles
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);

            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(-10, 100, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.GRADIENT_HLINEAR,
                new Dimension(16, 16), 1f, Color.BLACK, Color.WHITE));  // Black to white
            ga.getAttributes().setImageRepeat(AVKey.REPEAT_Y);
            ga.getAttributes().setBackgroundColor(Color.WHITE);
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);
            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(-10, 110, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.GRADIENT_VLINEAR,
                new Dimension(16, 16), 1f, Color.BLACK, Color.WHITE));  // Black to white
            ga.getAttributes().setImageRepeat(AVKey.REPEAT_X);
            ga.getAttributes().setBackgroundColor(Color.WHITE);
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);
            ga = new GlobeAnnotation("Background patterns...", Position.fromDegrees(-10, 120, 0), patternAttr);
            ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.PATTERN_HVLINE,
                .15f, Color.GREEN));  // green + lines
            ga.getAttributes().setImageScale(.4);
            ga.getAttributes().setSize(new Dimension(200, 128));
            layer.addAnnotation(ga);
            // Shows pattern scale effect on circles pattern
            for (int i = 1; i <= 10; i++)
            {
                ga = new GlobeAnnotation("Pattern scale:" + (float) i / 10,
                    Position.fromDegrees(-20, 97 + i * 3, 0), patternAttr);
                ga.getAttributes().setImageSource(PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLES,
                    (float) i / 10, Color.LIGHT_GRAY));
                ga.getAttributes().setImageScale(.4);
                ga.getAttributes().setSize(new Dimension(160, 60));
                layer.addAnnotation(ga);
            }

            // Using a GlobeAnnotation subclass to override drawing
            class SimpleGlobeAnnotation extends GlobeAnnotation
            {
                Font font = Font.decode("Arial-PLAIN-12");

                public SimpleGlobeAnnotation(String text, Position position)
                {
                    super(text, position);
                }

                protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale)
                {
                    GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                    gl.glTranslated(x, y, 0);
                    gl.glScaled(scale, scale, 1);
                }

                protected void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition)
                {
                    if (dc.isPickingMode())
                        return;

                    TextRenderer textRenderer = this.getTextRenderer(dc, this.font);

                    // Draw text centered just above the screen point - use annotation's colors
                    String text = getText().split("\n")[0]; // First line only
                    int textWidth = (int) textRenderer.getBounds(text).getWidth();
                    Color textColor = this.modulateColorOpacity(this.getAttributes().getTextColor(), opacity);
                    Color backColor = this.modulateColorOpacity(this.getAttributes().getBackgroundColor(), opacity);
                    textRenderer.begin3DRendering();
                    textRenderer.setColor(backColor);
                    textRenderer.draw(text, -textWidth / 2 + 1, 12 - 1);   // Background 'shadow'
                    textRenderer.setColor(textColor);
                    textRenderer.draw(text, -textWidth / 2, 12);           // Foreground text
                    textRenderer.end3DRendering();

                    // Draw little square around screen point - use annotation's color
                    Color borderColor = this.getAttributes().getBorderColor();
                    this.applyColor(dc, borderColor, opacity, false);
                    // Draw 3x3 shape from its bottom left corner
                    GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                    gl.glDisable(GL.GL_LINE_SMOOTH);
                    gl.glDisable(GL2.GL_LINE_STIPPLE);
                    gl.glLineWidth(1);
                    gl.glTranslated(-1, -1, 0);
                    FrameFactory.drawShape(dc, AVKey.SHAPE_RECTANGLE, 3, 3, GL.GL_LINE_STRIP, 0);
                }
            }

            ga = new SimpleGlobeAnnotation("Mount Rainier\nAlt: 4392m", Position.fromDegrees(46.8534, -121.7609, 0));
            layer.addAnnotation(ga);
            ga = new SimpleGlobeAnnotation("Mount Adams\nAlt: 3742m", Position.fromDegrees(46.2018, -121.4931, 0));
            layer.addAnnotation(ga);
            ga = new SimpleGlobeAnnotation("Mount Saint Helens\nAlt: 4392m",
                Position.fromDegrees(46.1991, -122.1882, 0));
            layer.addAnnotation(ga);

            // Using an anonymous subclass to change annotation text on the fly
            ga = new GlobeAnnotation("DRAG ME!", Position.fromDegrees(42, -118, 0), Font.decode("Arial-BOLD-18"))
            {
                public void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition)
                {
                    // if annotation has moved, set its text
                    if (getPosition().getLatitude().degrees != 42 || getPosition().getLongitude().degrees != -118)
                        setText(String.format("Lat %7.4f\u00B0\nLon %7.4f\u00B0", getPosition().getLatitude().degrees,
                            getPosition().getLongitude().degrees));

                    // Keep rendering
                    super.doDraw(dc, width, height, opacity, pickPosition);
                }
            };
            layer.addAnnotation(ga);

            // Using post drawing code in an anonymous subclass
            ga = new GlobeAnnotation("Annotation with extra frames drawn by a render delegate.",
                Position.fromDegrees(40, -116, 0), Font.decode("Serif-BOLD-18"), Color.DARK_GRAY)
            {
                public void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition)
                {
                    // Let normal rendering happen
                    super.doDraw(dc, width, height, opacity, pickPosition);

                    java.awt.Rectangle insetBounds = this.computeInsetBounds(width, height);
                    java.awt.Rectangle freeBounds = this.computeFreeBounds(dc, width, height);

                    // Draw second light gray frame outside draw rectangle
                    // Refers to scaleFactor, alphaFactor, drawRectangle and freeRectangle which have been
                    // set during drawing.
                    this.applyColor(dc, Color.BLACK, 0.5 * opacity, true);
                    // Translate to draw area bottom left corner, 3 pixels outside
                    GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                    gl.glTranslated(insetBounds.x - 3, insetBounds.y - 3, 0);
                    FrameFactory.drawShape(dc, AVKey.SHAPE_RECTANGLE, insetBounds.width + 6,
                        insetBounds.height + 6, GL.GL_LINE_STRIP, 4);

                    // Draw another frame in the free space if any
                    if (freeBounds.height > 0)
                    {
                        gl.glTranslated(+3, +3, 0);
                        FrameFactory.drawShape(dc, AVKey.SHAPE_ELLIPSE, freeBounds.width,
                            freeBounds.height, GL.GL_TRIANGLE_FAN, 0);
                    }
                }
            };
            ga.getAttributes().setTextAlign(AVKey.CENTER);
            ga.getAttributes().setBackgroundColor(new Color(1f, 1f, 1f, .7f));
            ga.getAttributes().setBorderColor(Color.BLACK);
            ga.getAttributes().setSize(new Dimension(160, 200));
            layer.addAnnotation(ga);

            // Using a ScreenAnnotation
            ScreenAnnotation sa = new ScreenAnnotation("Fixed position annotation", new Point(20, 50));
            sa.getAttributes().setDefaults(defaultAttributes);
            sa.getAttributes().setCornerRadius(0);
            sa.getAttributes().setSize(new Dimension(200, 0));
            sa.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED); // use strict dimension width - 200
            sa.getAttributes().setDrawOffset(new Point(100, 0)); // screen point is annotation bottom left corner
            sa.getAttributes().setHighlightScale(1);             // No highlighting either
            layer.addAnnotation(sa);

            makeRelativeAnnotations(layer);

            // Add layer to the layer list and update the layer panel
            insertBeforeCompass(this.getWwd(), layer);
        }

        public void makeRelativeAnnotations(AnnotationLayer layer)
        {
            AnnotationAttributes defaultAttributes = new AnnotationAttributes();
            defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, 0f));
            defaultAttributes.setTextColor(Color.YELLOW);
            defaultAttributes.setLeaderGapWidth(14);
            defaultAttributes.setCornerRadius(0);
            defaultAttributes.setSize(new Dimension(300, 0));
            defaultAttributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT); // use strict dimension width - 200
            defaultAttributes.setFont(Font.decode("Arial-BOLD-24"));
            defaultAttributes.setBorderWidth(0);
            defaultAttributes.setHighlightScale(1);             // No highlighting either
            defaultAttributes.setCornerRadius(0);

            ScreenRelativeAnnotation lowerLeft = new ScreenRelativeAnnotation("Lower Left", .1, 0.01);
            lowerLeft.setKeepFullyVisible(true);
            lowerLeft.setXMargin(5);
            lowerLeft.setYMargin(5);
            lowerLeft.getAttributes().setDefaults(defaultAttributes);

            ScreenRelativeAnnotation upperLeft = new ScreenRelativeAnnotation("Upper Left", 0.1, 0.99);
            upperLeft.setKeepFullyVisible(true);
            upperLeft.setXMargin(5);
            upperLeft.setYMargin(5);
            upperLeft.getAttributes().setDefaults(defaultAttributes);

            ScreenRelativeAnnotation upperRight = new ScreenRelativeAnnotation("Upper Right", 0.99, 0.99);
            upperRight.setKeepFullyVisible(true);
            upperRight.setXMargin(5);
            upperRight.setYMargin(5);
            upperRight.getAttributes().setDefaults(defaultAttributes);

            ScreenRelativeAnnotation lowerRight = new ScreenRelativeAnnotation("Lower Right", 0.99, 0.01);
            lowerRight.setKeepFullyVisible(true);
            lowerRight.setXMargin(5);
            lowerRight.setYMargin(5);
            lowerRight.getAttributes().setDefaults(defaultAttributes);

            ScreenRelativeAnnotation center = new ScreenRelativeAnnotation("Center", 0.5, 0.5);
            center.setKeepFullyVisible(true);
            center.setXMargin(5);
            center.setYMargin(5);
            center.getAttributes().setDefaults(defaultAttributes);

            layer.addAnnotation(lowerLeft);
            layer.addAnnotation(upperLeft);
            layer.addAnnotation(upperRight);
            layer.addAnnotation(lowerRight);
            layer.addAnnotation(center);
        }

        public GlobeAnnotation makeTopImageBottomTextAnnotation(PowerOfTwoPaddedImage image, String text,
            Position position)
        {
            // Create annotation
            GlobeAnnotation ga = new GlobeAnnotation(text, position);
            int inset = 10; // pixels
            ga.getAttributes().setInsets(new Insets(image.getOriginalHeight() + inset * 2, inset, inset, inset));
            ga.getAttributes().setImageSource(image.getPowerOfTwoImage());
            ga.getAttributes().setImageOffset(new Point(inset, inset));
            ga.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
            ga.getAttributes().setImageOpacity(1);
            ga.getAttributes().setSize(new Dimension(image.getOriginalWidth() + inset * 2, 0));
            ga.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
            ga.getAttributes().setBackgroundColor(Color.WHITE);
            ga.getAttributes().setTextColor(Color.BLACK);
            ga.getAttributes().setBorderColor(Color.BLACK);
            return ga;
        }

        // --- Selection ---------------------------------------

        private void setupSelection()
        {
            // Add a select listener to select or highlight annotations on rollover
            this.getWwd().addSelectListener(new SelectListener()
            {
                private BasicDragger dragger = new BasicDragger(getWwd());

                public void selected(SelectEvent event)
                {
                    if (event.hasObjects() && event.getTopObject() instanceof Annotation)
                    {
                        // Handle cursor change on hyperlink
                        if (event.getTopPickedObject().getValue(AVKey.URL) != null)
                            ((Component) getWwd()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        else
                            ((Component) getWwd()).setCursor(Cursor.getDefaultCursor());
                    }

                    // Select/unselect on left click on annotations
                    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                    {
                        if (event.hasObjects())
                        {
                            if (event.getTopObject() instanceof Annotation)
                            {
                                // Check for text or url
                                PickedObject po = event.getTopPickedObject();
                                if (po.getValue(AVKey.TEXT) != null)
                                {
                                    System.out.println("Text: \"" + po.getValue(AVKey.TEXT) + "\" Hyperlink: "
                                        + po.getValue(AVKey.URL));
                                    if (po.getValue(AVKey.URL) != null)
                                    {
                                        // Try to launch a browser with the clicked URL
                                        try
                                        {
                                            BrowserOpener.browse(new URL((String) po.getValue(AVKey.URL)));
                                        }
                                        catch (Exception ignore)
                                        {
                                        }
                                    }
                                    if (AppFrame.this.currentAnnotation == event.getTopObject())
                                        return;
                                }
                                // Left click on an annotation - select
                                if (AppFrame.this.currentAnnotation != null)
                                {
                                    // Unselect current
                                    //AppFrame.this.currentAnnotation.getAttributes().setHighlighted(false);
                                    AppFrame.this.currentAnnotation.getAttributes().setBorderColor(
                                        AppFrame.this.savedBorderColor);
                                }
                                if (AppFrame.this.currentAnnotation != event.getTopObject())
                                {
                                    // Select new one if not current one already
                                    AppFrame.this.currentAnnotation = (Annotation) event.getTopObject();
                                    //AppFrame.this.currentAnnotation.getAttributes().setHighlighted(true);
                                    AppFrame.this.savedBorderColor = AppFrame.this.currentAnnotation
                                        .getAttributes().getBorderColor();
                                    AppFrame.this.savedImage = AppFrame.this.currentAnnotation.getAttributes()
                                        .getImageSource() instanceof BufferedImage ?
                                        (BufferedImage) AppFrame.this.currentAnnotation.getAttributes().getImageSource()
                                        : null;
                                    AppFrame.this.currentAnnotation.getAttributes().setBorderColor(Color.YELLOW);
                                }
                                else
                                {
                                    // Clear current annotation
                                    AppFrame.this.currentAnnotation = null; // switch off
                                }
                                // Update control panel
                                AppFrame.this.updateControlPanel();
                            }
                            else
                                System.out.println("Left click on " + event.getTopObject());
                        }
                    }
                    // Highlight on rollover
                    else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
                    {
                        AppFrame.this.highlight(event.getTopObject());
                    }
                    // Have drag events drag the selected object.
                    else if (event.getEventAction().equals(SelectEvent.DRAG_END)
                        || event.getEventAction().equals(SelectEvent.DRAG))
                    {
                        if (event.hasObjects())
                        {
                            // If selected annotation delegate dragging computations to a dragger.
                            if (event.getTopObject() == AppFrame.this.currentAnnotation)
                                this.dragger.selected(event);
                        }

                        // We missed any roll-over events while dragging, so highlight any under the cursor now,
                        // or de-highlight the dragged shape if it's no longer under the cursor.
                        if (event.getEventAction().equals(SelectEvent.DRAG_END))
                        {
                            PickedObjectList pol = getWwd().getObjectsAtCurrentPosition();
                            if (pol != null)
                            {
                                AppFrame.this.highlight(pol.getTopObject());
                                AppFrame.this.getWwd().redraw();
                            }
                        }
                    }
                }
            });
        }

        private void highlight(Object o)
        {
            // Manage highlighting of Annotations.
            if (this.lastPickedObject == o)
                return; // same thing selected

            // Turn off highlight if on.
            if (this.lastPickedObject != null) // && this.lastPickedObject != this.currentAnnotation)
            {
                this.lastPickedObject.getAttributes().setHighlighted(false);
                this.lastPickedObject = null;
            }

            // Turn on highlight if object selected.
            if (o != null && o instanceof Annotation)
            {
                this.lastPickedObject = (Annotation) o;
                this.lastPickedObject.getAttributes().setHighlighted(true);
            }
        }

        // -- Control panel ---------------------------------------------------------------

        private JPanel makeControlPanel()
        {

            //-- Annotation text area. ----------------------------------------------
            this.inputTextArea = new JTextArea();
            this.inputTextArea.setFont(new Font("Sans_Serif", Font.PLAIN, 16));
            this.inputTextArea.setLineWrap(true);
            this.inputTextArea.setWrapStyleWord(true);
            JScrollPane textScrollPane = new JScrollPane(this.inputTextArea);
            textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            textScrollPane.setPreferredSize(new Dimension(200, 100));
            textScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            //-- Width panel --------------------------------------------------------
            JPanel sizePanel = new JPanel(new GridLayout(0, 1, 0, 0));
            sizePanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), new TitledBorder("Width and Height")));
            this.widthSlider = new JSlider(JSlider.HORIZONTAL, 0, 800, 160);
            this.widthSlider.setMajorTickSpacing(100);
            this.widthSlider.setMinorTickSpacing(10);
            //this.widthSlider.setPaintTicks(true);
            this.widthSlider.setPaintLabels(true);
            this.widthSlider.setToolTipText("Preferred annotation width");
            this.widthSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            sizePanel.add(this.widthSlider);
            this.heightSlider = new JSlider(JSlider.HORIZONTAL, 0, 800, 0);
            this.heightSlider.setMajorTickSpacing(100);
            this.heightSlider.setMinorTickSpacing(10);
            //this.widthSlider.setPaintTicks(true);
            this.heightSlider.setPaintLabels(true);
            this.heightSlider.setToolTipText("Preferred annotation height, zero = no limit");
            this.heightSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            sizePanel.add(this.heightSlider);

            //-- Corner radius panel ----------------------------------------------------
            JPanel cornerRadiusPanel = new JPanel();
            cornerRadiusPanel.setLayout(new BoxLayout(cornerRadiusPanel, BoxLayout.X_AXIS));
            cornerRadiusPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            cornerRadiusPanel.add(new JLabel("Corner radius:"));
            cornerRadiusPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            this.cornerRadiusSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
            this.cornerRadiusSlider.setMajorTickSpacing(10);
            this.cornerRadiusSlider.setMinorTickSpacing(1);
            //this.cornerRadiusSlider.setPaintTicks(true);
            this.cornerRadiusSlider.setPaintLabels(true);
            this.cornerRadiusSlider.setToolTipText("Rounded corners radius");
            this.cornerRadiusSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            cornerRadiusPanel.add(this.cornerRadiusSlider);

            //-- Insets panel ----------------------------------------------------
            JPanel insetsPanel = new JPanel();
            insetsPanel.setLayout(new BoxLayout(insetsPanel, BoxLayout.X_AXIS));
            insetsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            insetsPanel.add(new JLabel("Insets:"));
            insetsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            this.insetsTop = new JSpinner();
            this.insetsTop.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            this.insetsRight = new JSpinner();
            this.insetsRight.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            this.insetsBottom = new JSpinner();
            this.insetsBottom.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            this.insetsLeft = new JSpinner();
            this.insetsLeft.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            insetsPanel.add(this.insetsTop);
            insetsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            insetsPanel.add(this.insetsRight);
            insetsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            insetsPanel.add(this.insetsBottom);
            insetsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            insetsPanel.add(this.insetsLeft);

            //-- Border width panel ----------------------------------------------------
            JPanel borderWidthPanel = new JPanel();
            borderWidthPanel.setLayout(new BoxLayout(borderWidthPanel, BoxLayout.X_AXIS));
            borderWidthPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            borderWidthPanel.add(new JLabel("Border width:"));
            borderWidthPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            this.borderWidthSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 10);
            this.borderWidthSlider.setMajorTickSpacing(10);
            this.borderWidthSlider.setMinorTickSpacing(1);
            //this.borderWidthSlider.setPaintTicks(true);
            this.borderWidthSlider.setPaintLabels(true);
            this.borderWidthSlider.setToolTipText("Border width 1/10th");
            this.borderWidthSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            borderWidthPanel.add(this.borderWidthSlider);

            //-- Stipple factor panel ----------------------------------------------------
            JPanel stippleFactorPanel = new JPanel();
            stippleFactorPanel.setLayout(new BoxLayout(stippleFactorPanel, BoxLayout.X_AXIS));
            stippleFactorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            stippleFactorPanel.add(new JLabel("Stipple factor:"));
            stippleFactorPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            this.stippleFactorSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 0);
            this.stippleFactorSlider.setMajorTickSpacing(1);
            this.stippleFactorSlider.setPaintLabels(true);
            this.stippleFactorSlider.setToolTipText("Border line pattern repeat factor");
            this.stippleFactorSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            stippleFactorPanel.add(this.stippleFactorSlider);

            //-- Scale and opacity panel ----------------------------------------------------
            JPanel scalePanel = new JPanel(new GridLayout(0, 1, 0, 0));
            scalePanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), new TitledBorder("Scale and Opacity")));
            this.scaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 30, 10);
            this.scaleSlider.setMajorTickSpacing(10);
            this.scaleSlider.setMinorTickSpacing(1);
            //this.scaleSlider.setPaintTicks(true);
            this.scaleSlider.setPaintLabels(true);
            this.scaleSlider.setToolTipText("Annotation scaling");
            this.scaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            scalePanel.add(this.scaleSlider);
            this.opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 10);
            this.opacitySlider.setMajorTickSpacing(1);
            //this.opacitySlider.setMinorTickSpacing(1);
            //this.opacitySlider.setPaintTicks(true);
            this.opacitySlider.setPaintLabels(true);
            this.opacitySlider.setToolTipText("Annotation opacity");
            this.opacitySlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            scalePanel.add(this.opacitySlider);

            // -- Font --------------------------------------------------------------
            JPanel fontPanel = new JPanel();
            fontPanel.setLayout(new BoxLayout(fontPanel, BoxLayout.X_AXIS));
            fontPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            fontPanel.add(new JLabel("Font"));
            fontPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            this.cbFontName = new JComboBox(new String[] {"Arial", "SansSerif", "Serif", "Courier", "Times",
                "Helvetica", "Trebuchet", "Tahoma"});
            this.cbFontName.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            fontPanel.add(this.cbFontName);
            fontPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            this.cbFontStyle = new JComboBox(new String[] {"Plain", "Bold", "Italic", "BoldItalic"});
            this.cbFontStyle.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            fontPanel.add(this.cbFontStyle);
            fontPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            this.cbFontSize = new JComboBox(new String[] {"10", "12", "14", "16", "18", "20", "24", "28", "34",
                "48", "64"});
            this.cbFontSize.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            fontPanel.add(this.cbFontSize);
            //fontPanel.add(Box.createRigidArea(new Dimension(10, 0)));

            // -- Text align panel -------------------------------------------------------
            final JPanel alignPanel = new JPanel(new GridLayout(0, 3, 5, 5));
            alignPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            alignPanel.add(new JLabel("Align & Effect:"));
            this.cbTextAlign = new JComboBox(new String[] {"Left", "Center", "Right"});
            this.cbTextAlign.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            alignPanel.add(this.cbTextAlign);
            this.cbTextEffect = new JComboBox(new String[] {"None", "Shadow", "Outline"});
            this.cbTextEffect.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            alignPanel.add(this.cbTextEffect);

            // -- Adjust width panel -------------------------------------------------------
            final JPanel adjustWidthPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            adjustWidthPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            this.cbAdjustWidth = new JCheckBox("Adjust width to text");
            this.cbAdjustWidth.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            this.cbAdjustWidth.setSelected(true);
            adjustWidthPanel.add(this.cbAdjustWidth);

            // -- Shape and leader panel -----------------------------------------------
            final JPanel shapePanel = new JPanel(new GridLayout(0, 2, 5, 5));
            shapePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            shapePanel.add(new JLabel("Shape:"));
            this.cbShape = new JComboBox(new String[] {"Rectangle", "Ellipse", "None"});
            this.cbShape.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            shapePanel.add(this.cbShape);
            shapePanel.add(new JLabel("Leader Shape:"));
            this.cbLeader = new JComboBox(new String[] {"Triangle", "None"});
            this.cbLeader.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            shapePanel.add(this.cbLeader);
            shapePanel.add(new JLabel("Leader Gap Width:"));
            this.leaderGapWidthSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, 40);
            this.leaderGapWidthSlider.setMajorTickSpacing(10);
            this.leaderGapWidthSlider.setMinorTickSpacing(1);
            this.leaderGapWidthSlider.setPaintLabels(true);
            this.leaderGapWidthSlider.setToolTipText("Leader gap width");
            this.leaderGapWidthSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            shapePanel.add(this.leaderGapWidthSlider);

            // -- Image select panel -----------------------------------------------
            final JPanel imagePanel = new JPanel(new GridLayout(0, 2, 5, 5));
            imagePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            imagePanel.add(new JLabel("Image:"));
            this.cbImage = new JComboBox(new String[] {"None", "Earth", "NASA", "WWJ Splash", "Custom"});
            this.cbImage.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            imagePanel.add(this.cbImage);
            imagePanel.add(new JLabel("Repeat:"));
            this.cbImageRepeat = new JComboBox(new String[] {"None", "Repeat-X", "Repeat-Y", "Repeat-XY"});
            this.cbImageRepeat.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            imagePanel.add(this.cbImageRepeat);

            //-- Image scale and opacity panel ----------------------------------------------
            JPanel imageScalePanel = new JPanel(new GridLayout(0, 1, 0, 0));
            imageScalePanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
                    new TitledBorder("Scale and Opacity")));
            this.imageScaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 30, 10);
            this.imageScaleSlider.setMajorTickSpacing(10);
            this.imageScaleSlider.setMinorTickSpacing(1);
            this.imageScaleSlider.setPaintLabels(true);
            this.imageScaleSlider.setToolTipText("Background image scaling");
            this.imageScaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            imageScalePanel.add(this.imageScaleSlider);
            this.imageOpacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 10);
            this.imageOpacitySlider.setMajorTickSpacing(1);
            this.imageOpacitySlider.setPaintLabels(true);
            this.imageOpacitySlider.setToolTipText("Background image opacity");
            this.imageOpacitySlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            imageScalePanel.add(this.imageOpacitySlider);

            //-- Image offset panel ----------------------------------------------
            JPanel imageOffsetPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            imageOffsetPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), new TitledBorder("Offset")));
            this.imageOffsetXSlider = new JSlider(JSlider.HORIZONTAL, -200, 200, 0);
            this.imageOffsetXSlider.setMajorTickSpacing(100);
            this.imageOffsetXSlider.setMinorTickSpacing(1);
            this.imageOffsetXSlider.setPaintLabels(true);
            this.imageOffsetXSlider.setToolTipText("Background image horizontal offset (X)");
            this.imageOffsetXSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            imageOffsetPanel.add(this.imageOffsetXSlider);
            this.imageOffsetYSlider = new JSlider(JSlider.HORIZONTAL, -200, 200, 0);
            this.imageOffsetYSlider.setMajorTickSpacing(100);
            this.imageOffsetXSlider.setMinorTickSpacing(1);
            this.imageOffsetYSlider.setPaintLabels(true);
            this.imageOffsetYSlider.setToolTipText("Background image vertical offset (Y)");
            this.imageOffsetYSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            imageOffsetPanel.add(this.imageOffsetYSlider);

            //-- Annotation offset panel ----------------------------------------------
            JPanel offsetPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            offsetPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), new TitledBorder("Draw offset")));
            this.offsetXSlider = new JSlider(JSlider.HORIZONTAL, -200, 200, 0);
            this.offsetXSlider.setMajorTickSpacing(100);
            this.offsetXSlider.setMinorTickSpacing(1);
            this.offsetXSlider.setPaintLabels(true);
            this.offsetXSlider.setToolTipText("Annotation horizontal offset (X)");
            this.offsetXSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            offsetPanel.add(this.offsetXSlider);
            this.offsetYSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 0);
            this.offsetYSlider.setMajorTickSpacing(100);
            this.offsetYSlider.setMinorTickSpacing(1);
            this.offsetYSlider.setPaintLabels(true);
            this.offsetYSlider.setToolTipText("Annotation vertical offset (Y)");
            this.offsetYSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            offsetPanel.add(this.offsetYSlider);

            //-- Annotation distance scalling panel ---------------------------------------
            JPanel distancePanel = new JPanel(new GridLayout(0, 1, 0, 0));
            distancePanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
                new TitledBorder("Distance min/max scale and opacity")));
            this.distanceMinScaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 30, 10);
            this.distanceMinScaleSlider.setMajorTickSpacing(10);
            this.distanceMinScaleSlider.setMinorTickSpacing(1);
            this.distanceMinScaleSlider.setPaintLabels(true);
            this.distanceMinScaleSlider.setToolTipText("Minimum scale 1/10th");
            this.distanceMinScaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            distancePanel.add(this.distanceMinScaleSlider);
            this.distanceMaxScaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 30, 10);
            this.distanceMaxScaleSlider.setMajorTickSpacing(10);
            this.distanceMaxScaleSlider.setMinorTickSpacing(1);
            this.distanceMaxScaleSlider.setPaintLabels(true);
            this.distanceMaxScaleSlider.setToolTipText("Maximum scale 1/10th");
            this.distanceMaxScaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            distancePanel.add(this.distanceMaxScaleSlider);
            this.distanceMinOpacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 10);
            this.distanceMinOpacitySlider.setMajorTickSpacing(1);
            this.distanceMinOpacitySlider.setPaintLabels(true);
            this.distanceMinOpacitySlider.setToolTipText("Minimum opacity");
            this.distanceMinOpacitySlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            distancePanel.add(this.distanceMinOpacitySlider);

            //-- Highlight scale panel ---------------------------------------
            JPanel highlightPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            highlightPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
                    new TitledBorder("Highlight scale")));
            this.highlightScaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 30, 10);
            this.highlightScaleSlider.setMajorTickSpacing(10);
            this.highlightScaleSlider.setMinorTickSpacing(1);
            this.highlightScaleSlider.setPaintLabels(true);
            this.highlightScaleSlider.setToolTipText("Highlight scale 1/10th");
            this.highlightScaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            highlightPanel.add(this.highlightScaleSlider);

            // -- Color panel -------------------------------------------------------
            final JPanel colorPanel = new JPanel(new GridLayout(0, 3, 5, 5));
            colorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            colorPanel.add(new JLabel("Text color:"));
            this.btTextColor = new JButton("");
            this.btTextColor.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    Color c = JColorChooser.showDialog(colorPanel,
                        "Choose a color...", ((JButton) event.getSource()).getBackground());
                    if (c != null)
                    {
                        ((JButton) event.getSource()).setBackground(c);
                        if (currentAnnotation != null)
                            updateAnnotation();
                    }
                }
            });
            colorPanel.add(this.btTextColor);
            this.cbTextColorAlpha = new JComboBox(new String[] {"10", "9", "8", "7", "6", "5", "4", "3", "2",
                "1", "0"});
            this.cbTextColorAlpha.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            colorPanel.add(this.cbTextColorAlpha);

            colorPanel.add(new JLabel("Back color:"));
            this.btBackColor = new JButton("");
            this.btBackColor.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    Color c = JColorChooser.showDialog(colorPanel,
                        "Choose a color...", ((JButton) event.getSource()).getBackground());
                    if (c != null)
                    {
                        ((JButton) event.getSource()).setBackground(c);
                        if (currentAnnotation != null)
                            updateAnnotation();
                    }
                }
            });
            colorPanel.add(this.btBackColor);
            this.cbBackColorAlpha = new JComboBox(new String[] {"10", "9", "8", "7", "6", "5", "4", "3", "2",
                "1", "0"});
            this.cbBackColorAlpha.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            colorPanel.add(this.cbBackColorAlpha);

            colorPanel.add(new JLabel("Border color:"));
            this.btBorderColor = new JButton("");
            this.btBorderColor.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    Color c = JColorChooser.showDialog(colorPanel,
                        "Choose a color...", ((JButton) event.getSource()).getBackground());
                    if (c != null)
                    {
                        ((JButton) event.getSource()).setBackground(c);
                        if (currentAnnotation != null)
                            updateAnnotation();
                    }
                }
            });
            colorPanel.add(this.btBorderColor);
            this.cbBorderColorAlpha = new JComboBox(new String[] {"10", "9", "8", "7", "6", "5", "4", "3", "2",
                "1", "0"});
            this.cbBorderColorAlpha.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });
            colorPanel.add(this.cbBorderColorAlpha);

            // -- Button panel ------------------------------------------------------
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(14, 4, 4, 4));

            // Apply changes button
            this.btApply = new JButton("Apply");
            this.btApply.setEnabled(false);
            this.btApply.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        updateAnnotation();
                    }
                }
            });

            // Add annotation button
            JButton btAdd = new JButton("Add new");
            btAdd.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    Position lookAtPos = computeGroundPosition(getWwd());
                    if (lookAtPos != null && inputTextArea.getText().length() > 0)
                    {
                        Annotation a = currentAnnotation;
                        currentAnnotation = new GlobeAnnotation(inputTextArea.getText(), lookAtPos);
                        updateAnnotation();
                        layer.addAnnotation(currentAnnotation);
                        currentAnnotation = a;
                        getWwd().redraw();
                    }
                }
            });

            // Remove button
            this.btRemove = new JButton("Remove");
            this.btRemove.setEnabled(false);
            this.btRemove.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    if (currentAnnotation != null)
                    {
                        layer.removeAnnotation(currentAnnotation);
                        // Clear current annotation
                        currentAnnotation = null;
                        inputTextArea.setText("");
                        widthSlider.setEnabled(false);
                        btApply.setEnabled(false);
                        btRemove.setEnabled(false);
                        getWwd().redraw();
                    }
                }
            });

            buttonPanel.add(btAdd);
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            buttonPanel.add(this.btApply);
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            buttonPanel.add(this.btRemove);
            //-- end button panel

            //-- Tabbed pane assembly ---------------
            JTabbedPane tabbedPane = new JTabbedPane();

            // Text and colors
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
            controlPanel.setToolTipText("Text and Colors");
            controlPanel.add(textScrollPane);
            controlPanel.add(fontPanel);
            controlPanel.add(alignPanel);
            controlPanel.add(colorPanel);
            tabbedPane.add(controlPanel);
            tabbedPane.setTitleAt(0, "Text");

            // Size, scale and opacity
            controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
            controlPanel.setToolTipText("Size, scale and opacity");
            controlPanel.add(adjustWidthPanel);
            controlPanel.add(sizePanel);
            controlPanel.add(scalePanel);
            tabbedPane.add(controlPanel);
            tabbedPane.setTitleAt(1, "Size");

            // Shape, insets, corner radius
            controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
            controlPanel.setToolTipText("Shape, insets, corner radius, border width and patterns");
            controlPanel.add(shapePanel);
            controlPanel.add(cornerRadiusPanel);
            controlPanel.add(insetsPanel);
            controlPanel.add(borderWidthPanel);
            controlPanel.add(stippleFactorPanel);
            tabbedPane.add(controlPanel);
            tabbedPane.setTitleAt(2, "Shape");

            // Image controls
            controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
            controlPanel.setToolTipText("Background image texture controls");
            controlPanel.add(imagePanel);
            controlPanel.add(imageScalePanel);
            controlPanel.add(imageOffsetPanel);
            tabbedPane.add(controlPanel);
            tabbedPane.setTitleAt(3, "Image");

            // Misc controls
            controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
            controlPanel.setToolTipText("Offset, distance scaling and fading, highlight");
            controlPanel.add(offsetPanel);
            controlPanel.add(distancePanel);
            controlPanel.add(highlightPanel);
            tabbedPane.add(controlPanel);
            tabbedPane.setTitleAt(4, "Misc.");

            //-- Combine tabbed panes with buttons at the bottom
            controlPanel = new JPanel();
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Annotation")));
            controlPanel.setToolTipText("Current or new annotation");
            controlPanel.setLayout(new BorderLayout());
            controlPanel.add(tabbedPane, BorderLayout.NORTH);
            controlPanel.add(buttonPanel, BorderLayout.SOUTH);

            return controlPanel;
        }

        // Update control panel from current annotation
        private void updateControlPanel()
        {
            if (this.currentAnnotation != null)
            {
                this.suspendUpdate = true;
                this.inputTextArea.setText(this.currentAnnotation.getText());
                this.widthSlider.setValue(this.currentAnnotation.getAttributes().getSize().width);
                this.heightSlider.setValue(this.currentAnnotation.getAttributes().getSize().height);
                this.scaleSlider.setValue((int) (this.currentAnnotation.getAttributes().getScale() * 10));
                this.opacitySlider.setValue((int) (this.currentAnnotation.getAttributes().getOpacity() * 10));
                this.cbAdjustWidth.setSelected(this.currentAnnotation.getAttributes()
                    .getAdjustWidthToText().equals(AVKey.SIZE_FIT_TEXT));

                Font font = currentAnnotation.getAttributes().getFont();
                if (font != null)
                {
                    this.cbFontName.setSelectedItem(font.getName());
                    this.cbFontStyle.setSelectedIndex(font.getStyle());
                    this.cbFontSize.setSelectedItem(String.valueOf(font.getSize()));
                }
                else
                {
                    this.cbFontName.setSelectedItem("Arial");
                    this.cbFontStyle.setSelectedItem("Plain");
                    this.cbFontSize.setSelectedItem("12");
                }
                Color color = currentAnnotation.getAttributes().getTextColor();
                String colorAlpha = color != null ? String.valueOf(Math.round((float) color.getAlpha() / 25.5f)) : "8";
                this.btTextColor.setBackground(color != null ? new Color(color.getRed(), color.getGreen(),
                    color.getBlue()) : Color.WHITE);
                this.cbTextColorAlpha.setSelectedItem(colorAlpha);

                color = currentAnnotation.getAttributes().getBackgroundColor();
                colorAlpha = color != null ? String.valueOf(Math.round((float) color.getAlpha() / 25.5f)) : "4";
                this.btBackColor.setBackground(color != null ? new Color(color.getRed(), color.getGreen(),
                    color.getBlue()) : Color.BLACK);
                this.cbBackColorAlpha.setSelectedItem(colorAlpha);

                color = this.savedBorderColor;
                colorAlpha = color != null ? String.valueOf(Math.round((float) color.getAlpha() / 25.5f)) : "7";
                this.btBorderColor.setBackground(color != null ? new Color(color.getRed(), color.getGreen(),
                    color.getBlue()) : Color.WHITE);
                this.cbBorderColorAlpha.setSelectedItem(colorAlpha);

                if (currentAnnotation.getAttributes().getTextAlign().equals(AVKey.LEFT))
                    cbTextAlign.setSelectedIndex(0);
                else if (currentAnnotation.getAttributes().getTextAlign().equals(AVKey.CENTER))
                    cbTextAlign.setSelectedIndex(1);
                else if (currentAnnotation.getAttributes().getTextAlign().equals(AVKey.RIGHT))
                    cbTextAlign.setSelectedIndex(2);

                if (currentAnnotation.getAttributes().getEffect().equals(AVKey.TEXT_EFFECT_NONE))
                    cbTextEffect.setSelectedIndex(0);
                else if (currentAnnotation.getAttributes().getEffect().equals(AVKey.TEXT_EFFECT_SHADOW))
                    cbTextEffect.setSelectedIndex(1);
                else if (currentAnnotation.getAttributes().getEffect().equals(AVKey.TEXT_EFFECT_OUTLINE))
                    cbTextEffect.setSelectedIndex(2);

                if (currentAnnotation.getAttributes().getFrameShape().equals(AVKey.SHAPE_RECTANGLE))
                    cbShape.setSelectedIndex(0);
                else if (currentAnnotation.getAttributes().getFrameShape().equals(AVKey.SHAPE_ELLIPSE))
                    cbShape.setSelectedIndex(1);
                else
                    cbShape.setSelectedIndex(2);

                if (currentAnnotation.getAttributes().getLeader().equals(AVKey.SHAPE_TRIANGLE))
                    cbLeader.setSelectedIndex(0);
                else
                    cbLeader.setSelectedIndex(1);

                this.leaderGapWidthSlider.setValue(currentAnnotation.getAttributes().getLeaderGapWidth());

                this.cornerRadiusSlider.setValue(currentAnnotation.getAttributes().getCornerRadius());

                Insets insets = currentAnnotation.getAttributes().getInsets();
                this.insetsTop.setValue(insets.top);
                this.insetsRight.setValue(insets.right);
                this.insetsBottom.setValue(insets.bottom);
                this.insetsLeft.setValue(insets.left);

                this.borderWidthSlider.setValue((int) (currentAnnotation.getAttributes().getBorderWidth() * 10));
                this.stippleFactorSlider.setValue(currentAnnotation.getAttributes().getBorderStippleFactor());

                if (currentAnnotation.getAttributes().getImageSource() != null)
                {
                    Object image = currentAnnotation.getAttributes().getImageSource();
                    if (image.equals(IMAGE_EARTH.getPowerOfTwoImage()))
                        this.cbImage.setSelectedIndex(1);
                    else if (image.equals(IMAGE_NASA.getPowerOfTwoImage()))
                        this.cbImage.setSelectedIndex(2);
                    else if (image.equals(IMAGE_WWJ_SPLASH.getPowerOfTwoImage()))
                        this.cbImage.setSelectedIndex(3);
                    else
                        this.cbImage.setSelectedIndex(4); // Custom
                }
                else
                {
                    this.cbImage.setSelectedIndex(0); // None
                }

                String imageRepeat = currentAnnotation.getAttributes().getImageRepeat();
                if (imageRepeat.equals(AVKey.REPEAT_NONE))
                    this.cbImageRepeat.setSelectedIndex(0);
                else if (imageRepeat.equals(AVKey.REPEAT_X))
                    this.cbImageRepeat.setSelectedIndex(1);
                else if (imageRepeat.equals(AVKey.REPEAT_Y))
                    this.cbImageRepeat.setSelectedIndex(2);
                else if (imageRepeat.equals(AVKey.REPEAT_XY))
                    this.cbImageRepeat.setSelectedIndex(3);

                this.imageScaleSlider.setValue((int) (this.currentAnnotation.getAttributes().getImageScale() * 10));
                this.imageOpacitySlider.setValue((int) (this.currentAnnotation.getAttributes().getImageOpacity() * 10));
                this.imageOffsetXSlider.setValue(this.currentAnnotation.getAttributes().getImageOffset().x);
                this.imageOffsetYSlider.setValue(this.currentAnnotation.getAttributes().getImageOffset().y);

                this.offsetXSlider.setValue(this.currentAnnotation.getAttributes().getDrawOffset().x);
                this.offsetYSlider.setValue(this.currentAnnotation.getAttributes().getDrawOffset().y);

                this.distanceMinScaleSlider.setValue(
                    (int) (this.currentAnnotation.getAttributes().getDistanceMinScale() * 10));
                this.distanceMaxScaleSlider.setValue(
                    (int) (this.currentAnnotation.getAttributes().getDistanceMaxScale() * 10));
                this.distanceMinOpacitySlider.setValue(
                    (int) (this.currentAnnotation.getAttributes().getDistanceMinOpacity() * 10));
                this.highlightScaleSlider.setValue(
                    (int) (this.currentAnnotation.getAttributes().getHighlightScale() * 10));

                AppFrame.this.btApply.setEnabled(true);
                AppFrame.this.btRemove.setEnabled(true);
                this.suspendUpdate = false;
                this.enableControlPanel(true);
            }
            else
            {
                this.enableControlPanel(false);
                this.inputTextArea.setText("Annotation text...");
                AppFrame.this.btApply.setEnabled(false);
                AppFrame.this.btRemove.setEnabled(false);
            }
        }

        private void enableControlPanel(boolean enabled)
        {
            this.inputTextArea.setEnabled(enabled);
            this.widthSlider.setEnabled(enabled);
            this.heightSlider.setEnabled(enabled);
            this.scaleSlider.setEnabled(enabled);
            this.opacitySlider.setEnabled(enabled);
            this.cbAdjustWidth.setEnabled(enabled);

            this.cbFontName.setEnabled(enabled);
            this.cbFontStyle.setEnabled(enabled);
            this.cbFontSize.setEnabled(enabled);

            this.btTextColor.setEnabled(enabled);
            this.cbTextColorAlpha.setEnabled(enabled);
            this.btBackColor.setEnabled(enabled);
            this.cbBackColorAlpha.setEnabled(enabled);
            this.btBorderColor.setEnabled(enabled);
            this.cbBorderColorAlpha.setEnabled(enabled);

            this.cbTextAlign.setEnabled(enabled);
            this.cbTextEffect.setEnabled(enabled);

            this.cbShape.setEnabled(enabled);
            this.cbLeader.setEnabled(enabled);
            this.leaderGapWidthSlider.setEnabled(enabled);

            this.cornerRadiusSlider.setEnabled(enabled);

            this.insetsTop.setEnabled(enabled);
            this.insetsRight.setEnabled(enabled);
            this.insetsBottom.setEnabled(enabled);
            this.insetsLeft.setEnabled(enabled);

            this.borderWidthSlider.setEnabled(enabled);
            this.stippleFactorSlider.setEnabled(enabled);

            this.cbImage.setEnabled(enabled);
            this.cbImageRepeat.setEnabled(enabled);
            this.imageScaleSlider.setEnabled(enabled);
            this.imageOpacitySlider.setEnabled(enabled);
            this.imageOffsetXSlider.setEnabled(enabled);
            this.imageOffsetYSlider.setEnabled(enabled);

            this.offsetXSlider.setEnabled(enabled);
            this.offsetYSlider.setEnabled(enabled);

            this.distanceMinScaleSlider.setEnabled(enabled);
            this.distanceMaxScaleSlider.setEnabled(enabled);
            this.distanceMinOpacitySlider.setEnabled(enabled);

            this.highlightScaleSlider.setEnabled(enabled);
        }

        // Update current annotation from control panel
        private void updateAnnotation()
        {
            if (this.currentAnnotation != null && !this.suspendUpdate)
            {
                this.currentAnnotation.setText(this.inputTextArea.getText());
                this.currentAnnotation.getAttributes().setSize(
                    new Dimension(this.widthSlider.getValue(), this.heightSlider.getValue()));
                this.currentAnnotation.getAttributes().setScale((double) this.scaleSlider.getValue() / 10);
                this.currentAnnotation.getAttributes().setOpacity((double) this.opacitySlider.getValue() / 10);
                this.currentAnnotation.getAttributes().setAdjustWidthToText(this.cbAdjustWidth.isSelected() ?
                    AVKey.SIZE_FIT_TEXT : AVKey.SIZE_FIXED);

                String fontString = this.cbFontName.getSelectedItem() + "-"
                    + this.cbFontStyle.getSelectedItem().toString().toUpperCase() + "-"
                    + this.cbFontSize.getSelectedItem();
                this.currentAnnotation.getAttributes().setFont(Font.decode(fontString));

                Color color = this.btTextColor.getBackground();
                int alpha = (int) (Float.valueOf((String) this.cbTextColorAlpha.getSelectedItem()) * 25.5f);
                this.currentAnnotation.getAttributes().setTextColor(
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

                color = this.btBackColor.getBackground();
                alpha = (int) (Float.valueOf((String) this.cbBackColorAlpha.getSelectedItem()) * 25.5f);
                this.currentAnnotation.getAttributes().setBackgroundColor(
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

                color = this.btBorderColor.getBackground();
                alpha = (int) (Float.valueOf((String) this.cbBorderColorAlpha.getSelectedItem()) * 25.5f);
                this.currentAnnotation.getAttributes().setBorderColor(
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                this.savedBorderColor = this.currentAnnotation.getAttributes().getBorderColor();

                switch (cbTextAlign.getSelectedIndex())
                {
                    case 0:
                    {
                        this.currentAnnotation.getAttributes().setTextAlign(AVKey.LEFT);
                        break;
                    }
                    case 1:
                    {
                        this.currentAnnotation.getAttributes().setTextAlign(AVKey.CENTER);
                        break;
                    }
                    case 2:
                    {
                        this.currentAnnotation.getAttributes().setTextAlign(AVKey.RIGHT);
                        break;
                    }
                }
                switch (cbTextEffect.getSelectedIndex())
                {
                    case 0:
                    {
                        this.currentAnnotation.getAttributes().setEffect(AVKey.TEXT_EFFECT_NONE);
                        break;
                    }
                    case 1:
                    {
                        this.currentAnnotation.getAttributes().setEffect(AVKey.TEXT_EFFECT_SHADOW);
                        break;
                    }
                    case 2:
                    {
                        this.currentAnnotation.getAttributes().setEffect(AVKey.TEXT_EFFECT_OUTLINE);
                        break;
                    }
                }
                switch (cbShape.getSelectedIndex())
                {
                    case 0:
                    {
                        this.currentAnnotation.getAttributes().setFrameShape(AVKey.SHAPE_RECTANGLE);
                        break;
                    }
                    case 1:
                    {
                        this.currentAnnotation.getAttributes().setFrameShape(AVKey.SHAPE_ELLIPSE);
                        break;
                    }
                    case 2:
                    {
                        this.currentAnnotation.getAttributes().setFrameShape(AVKey.SHAPE_NONE);
                        break;
                    }
                }
                switch (cbLeader.getSelectedIndex())
                {
                    case 0:
                    {
                        this.currentAnnotation.getAttributes().setLeader(AVKey.SHAPE_TRIANGLE);
                        break;
                    }
                    case 1:
                    {
                        this.currentAnnotation.getAttributes().setLeader(AVKey.SHAPE_NONE);
                        break;
                    }
                }
                currentAnnotation.getAttributes().setLeaderGapWidth(this.leaderGapWidthSlider.getValue());
                currentAnnotation.getAttributes().setCornerRadius(this.cornerRadiusSlider.getValue());
                currentAnnotation.getAttributes().setInsets(new Insets(
                    Integer.parseInt(insetsTop.getValue().toString()),
                    Integer.parseInt(insetsLeft.getValue().toString()),
                    Integer.parseInt(insetsBottom.getValue().toString()),
                    Integer.parseInt(insetsRight.getValue().toString())));

                currentAnnotation.getAttributes().setBorderWidth((double) borderWidthSlider.getValue() / 10);
                currentAnnotation.getAttributes().setBorderStippleFactor(stippleFactorSlider.getValue());

                switch (cbImage.getSelectedIndex())
                {
                    case 0:
                        currentAnnotation.getAttributes().setImageSource(null);
                        break;
                    case 1:
                        currentAnnotation.getAttributes().setImageSource(IMAGE_EARTH.getPowerOfTwoImage());
                        break;
                    case 2:
                        currentAnnotation.getAttributes().setImageSource(IMAGE_NASA.getPowerOfTwoImage());
                        break;
                    case 3:
                        currentAnnotation.getAttributes().setImageSource(IMAGE_WWJ_SPLASH.getPowerOfTwoImage());
                        break;
                    case 4:
                        currentAnnotation.getAttributes().setImageSource(savedImage);
                        break;
                }
                switch (cbImageRepeat.getSelectedIndex())
                {
                    case 0:
                        currentAnnotation.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
                        break;
                    case 1:
                        currentAnnotation.getAttributes().setImageRepeat(AVKey.REPEAT_X);
                        break;
                    case 2:
                        currentAnnotation.getAttributes().setImageRepeat(AVKey.REPEAT_Y);
                        break;
                    case 3:
                        currentAnnotation.getAttributes().setImageRepeat(AVKey.REPEAT_XY);
                        break;
                }

                this.currentAnnotation.getAttributes().setImageScale((double) this.imageScaleSlider.getValue() / 10);
                this.currentAnnotation.getAttributes().setImageOpacity(
                    (double) this.imageOpacitySlider.getValue() / 10);
                this.currentAnnotation.getAttributes().setImageOffset(new Point(imageOffsetXSlider.getValue(),
                    imageOffsetYSlider.getValue()));

                this.currentAnnotation.getAttributes().setDrawOffset(
                    new Point(offsetXSlider.getValue(), offsetYSlider.getValue()));

                this.currentAnnotation.getAttributes().setDistanceMinScale(
                    (double) this.distanceMinScaleSlider.getValue() / 10);
                this.currentAnnotation.getAttributes().setDistanceMaxScale(
                    (double) this.distanceMaxScaleSlider.getValue() / 10);
                this.currentAnnotation.getAttributes().setDistanceMinOpacity(
                    (double) this.distanceMinOpacitySlider.getValue() / 10);
                this.currentAnnotation.getAttributes().setHighlightScale(
                    (double) this.highlightScaleSlider.getValue() / 10);

                getWwd().redraw();
            }
        }

        private Position computeGroundPosition(WorldWindow wwd)
        {
            View view = wwd.getView();

            if (view == null)
                return null;

            return view.computePositionFromScreenPoint(view.getViewport().getWidth() / 2,
                view.getViewport().getHeight() / 2);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Annotations", AppFrame.class);
    }
}
