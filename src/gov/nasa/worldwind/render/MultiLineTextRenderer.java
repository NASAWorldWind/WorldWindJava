/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.regex.*;

/**
 * Multi line, rectangle bound text renderer with (very) minimal html support. <p> The {@link MultiLineTextRenderer}
 * (MLTR) handles wrapping, measuring and drawing of multiline text strings using Sun's JOGL {@link TextRenderer}. </p>
 * <p> A multiline text string is a character string containing new line characters in between lines. </p> <p> MLTR can
 * handle both regular text with new line separators and a very minimal implementation of HTML. Each type of text has
 * its own methods though. </p>  <p><b>Usage:</b></p>  <p>Instantiation:</p> <p> The MLTR needs a Font or a
 * TextRenderer to be instantiated. This will be the font used for text drawing, wrapping and measuring. For HTML
 * methods this font will be considered as the document default font. </p>
 * <pre>
 * Font font = Font.decode("Arial-PLAIN-12");
 * MultiLineTextRenderer mltr = new MultiLineTextRenderer(font);
 * </pre>
 * or
 * <pre>
 * TextRenderer tr = new TextRenderer(Font.decode("Arial-PLAIN-10"));
 * MultiLineTextRenderer mltr = new MultiLineTextRenderer(tr);
 * </pre>
 *  <p>Drawing regular text:</p>
 * <pre>
 * String text = "Line one.\nLine two.\nLine three...";
 * int x = 10;             // Upper left corner of text rectangle.
 * int y = 200;            // Origin at bottom left of screen.
 * int lineHeight = 14;    // Line height in pixels.
 * Color color = Color.RED;
 * mltr.setTextColor(color);
 * mltr.getTextRenderer().begin3DRendering();
 * mltr.draw(text, x, y, lineHeight);
 * mltr.getTextRenderer().end3DRendering();
 * </pre>
 * <p>Wrapping text to fit inside a width and optionally a height</p> <p> The MLTR wrap method will insert new line
 * characters inside the text so that it fits a given width in pixels. </p> <p> If a height dimension above zero is
 * specified too, the text will be truncated if needed, and a continuation string will be appended to the last line. The
 * continuation string can be set with mltr.setContinuationString(); </p>
 * <pre>
 * // Fit inside 300 pixels, no height constraint
 * String wrappedText = mltr.wrap(text, new Dimension(300, 0));
 * // Fit inside 300x400 pixels, text may be truncated
 * String wrappedText = mltr.wrap(text, new Dimension(300, 400));
 * </pre>
 * <p>Measuring text</p>
 * <pre>
 * Rectangle2D textBounds = mltr.getBounds(text);
 * </pre>
 * <p> The textBounds rectangle returned contains the width and height of the text as it would be drawn with the current
 * font. </p> <p> Note that textBounds.minX is the number of lines found and textBounds.minY is the maximum line height
 * for the font used. This value can be safely used as the lineHeight argument when drawing - or can even be ommited
 * after a getBounds: draw(text, x, y); ... </p>  <p><b>HTML support</b></p> <p> Supported tags are: <ul>
 * <li>&lt;p&gt;&lt;/p&gt;, &lt;br&gt; &lt;br /&gt;</li> <li>&lt;b&gt;&lt;/b&gt;</li> <li>&lt;i&gt;&lt;/i&gt;</li>
 * <li>&lt;a href="..."&gt;&lt;/a&gt;</li> <li>&lt;font color="#ffffff"&gt;&lt;/font&gt;</li> </ul> ... 
 *  <p> See {@link AbstractAnnotation}.drawAnnotation() for more usage details. </p>
 *
 * @author Patrick Murris
 * @version $Id: MultiLineTextRenderer.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class MultiLineTextRenderer
{
    protected TextRenderer textRenderer;
    protected int lineSpacing = 0;            // Inter line spacing in pixels
    protected int lineHeight = 14;            // Will be set by getBounds() or by application
    protected String textAlign = AVKey.LEFT;  // Text alignment
    protected String continuationString = "...";
    protected Color textColor = Color.DARK_GRAY;
    protected Color backColor = Color.LIGHT_GRAY;
    protected Color linkColor = Color.BLUE;

    // HTML Picking
    protected boolean isPicking = false;
    protected DrawContext drawContext;
    protected PickSupport pickSupport;
    protected Object pickObject;
    protected Position pickPosition;

    public MultiLineTextRenderer(TextRenderer textRenderer)
    {
        if (textRenderer == null)
        {
            String msg = Logging.getMessage("nullValue.TextRendererIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.textRenderer = textRenderer;
    }

    public MultiLineTextRenderer(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.textRenderer = new TextRenderer(font, true, false);
        this.textRenderer.setUseVertexArrays(false);
    }

    /**
     * Get the current TextRenderer.
     *
     * @return the current TextRenderer.
     */
    public TextRenderer getTextRenderer()
    {
        return this.textRenderer;
    }

    /**
     * Get the current line spacing height in pixels.
     *
     * @return the current line spacing height in pixels.
     */
    public int getLineSpacing()
    {
        return this.lineSpacing;
    }

    /**
     * Set the current line spacing height in pixels.
     *
     * @param height the line spacing height in pixels.
     */
    public void setLineSpacing(int height)
    {
        this.lineSpacing = height;
    }

    /**
     * Get the current line height in pixels.
     *
     * @return the current line height in pixels.
     */
    public int getLineHeight()
    {
        return this.lineHeight;
    }

    /**
     * Set the current line height in pixels.
     *
     * @param height the current line height in pixels.
     */
    public void setLineHeight(int height)
    {
        this.lineHeight = height;
    }

    /**
     * Get the current text alignment. Can be one of {@link AVKey#LEFT} the default, {@link AVKey#CENTER} or {@link
     * AVKey#RIGHT}.
     *
     * @return the current text alignment.
     */
    public String getTextAlign()
    {
        return this.textAlign;
    }

    /**
     * Set the current text alignment. Can be one of {@link AVKey#LEFT} the default, {@link AVKey#CENTER} or {@link
     * AVKey#RIGHT}.
     *
     * @param align the current text alignment.
     */
    public void setTextAlign(String align)
    {
        if (!align.equals(AVKey.LEFT) && !align.equals(AVKey.CENTER) && !align.equals(AVKey.RIGHT))
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", align);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.textAlign = align;
    }

    /**
     * Get the current text color.
     *
     * @return the current text color.
     */
    public Color getTextColor()
    {
        return this.textColor;
    }

    /**
     * Set the text renderer color.
     *
     * @param color the color to use when drawing text.
     */
    public void setTextColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.textColor = color;
        this.textRenderer.setColor(color);
    }

    /**
     * Get the background color used for EFFECT_SHADOW and EFFECT_OUTLINE.
     *
     * @return the current background color used when drawing shadow or outline..
     */
    public Color getBackColor()
    {
        return this.backColor;
    }

    /**
     * Set the background color used for EFFECT_SHADOW and EFFECT_OUTLINE.
     *
     * @param color the color to use when drawing shadow or outline.
     */
    public void setBackColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.backColor = color;
    }

    /**
     * Get the current link color.
     *
     * @return the current link color.
     */
    public Color getLinkColor()
    {
        return this.linkColor;
    }

    /**
     * Set the link color.
     *
     * @param color the color to use when drawing hyperlinks.
     */
    public void setLinkColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.linkColor = color;
    }

    /**
     * Set the character string appended at the end of text truncated during a wrap operation when exceeding the given
     * height limit.
     *
     * @param s the continuation character string.
     */
    public void setContinuationString(String s)
    {
        this.continuationString = s;
    }

    /**
     * Get the maximum line height for the given text renderer.
     *
     * @param tr the TextRenderer.
     *
     * @return the maximum line height.
     */
    public double getMaxLineHeight(TextRenderer tr)
    {
        // Check underscore + capital E with acute accent
        return tr.getBounds("_\u00c9").getHeight();
    }

    //** Plain text support ******************************************************
    //****************************************************************************

    /**
     * Returns the bounding rectangle for a multi-line string.
     * <p>
     * Note that the X component of the rectangle is the number of lines found in the text and the Y component of the
     * rectangle is the max line height encountered.
     * <p>
     * Note too that this method will automatically set the current line height to the max height found.
     *
     * @param text the multi-line text to evaluate.
     *
     * @return the bounding rectangle for the string.
     */
    public Rectangle getBounds(String text)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int width = 0;
        int maxLineHeight = 0;
        String[] lines = text.split("\n");
        for (String line : lines)
        {
            Rectangle2D lineBounds = this.textRenderer.getBounds(line);
            width = (int) Math.max(lineBounds.getWidth(), width);
            maxLineHeight = (int) Math.max(lineBounds.getHeight(), lineHeight);
        }
        // Make sure we have the highest line height
        maxLineHeight = (int) Math.max(getMaxLineHeight(this.textRenderer), maxLineHeight);
        // Set current line height for future draw
        this.lineHeight = maxLineHeight;
        // Compute final height using maxLineHeight and number of lines
        return new Rectangle(lines.length, lineHeight, width,
            lines.length * maxLineHeight + (lines.length - 1) * this.lineSpacing);
    }

    /**
     * Draw a multi-line text string with bounding rectangle top starting at the y position. Depending on the current
     * textAlign, the x position is either the rectangle left side, middle or right side.
     * <p>
     * Uses the current line height.
     *
     * @param text the multi-line text to draw.
     * @param x    the x position for top left corner of text rectangle.
     * @param y    the y position for top left corner of the text rectangle.
     */
    public void draw(String text, int x, int y)
    {
        this.draw(text, x, y, this.lineHeight);
    }

    /**
     * Draw a multi-line text string with bounding rectangle top starting at the y position. Depending on the current
     * textAlign, the x position is either the rectangle left side, middle or right side.
     * <p>
     * Uses the current line height and the given effect.
     *
     * @param text   the multi-line text to draw.
     * @param x      the x position for top left corner of text rectangle.
     * @param y      the y position for top left corner of the text rectangle.
     * @param effect the effect to use for the text rendering. Can be one of <code>EFFECT_NONE</code>,
     *               <code>EFFECT_SHADOW</code> or <code>EFFECT_OUTLINE</code>.
     */
    public void draw(String text, int x, int y, String effect)
    {
        this.draw(text, x, y, this.lineHeight, effect);
    }

    /**
     * Draw a multi-line text string with bounding rectangle top starting at the y position. Depending on the current
     * textAlign, the x position is either the rectangle left side, middle or right side.
     * <p>
     * Uses the given line height and effect.
     *
     * @param text           the multi-line text to draw.
     * @param x              the x position for top left corner of text rectangle.
     * @param y              the y position for top left corner of the text rectangle.
     * @param textLineHeight the line height in pixels.
     * @param effect         the effect to use for the text rendering. Can be one of <code>EFFECT_NONE</code>,
     *                       <code>EFFECT_SHADOW</code> or <code>EFFECT_OUTLINE</code>.
     */
    public void draw(String text, int x, int y, int textLineHeight, String effect)
    {
        if (effect == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (effect.equals(AVKey.TEXT_EFFECT_SHADOW))
        {
            this.textRenderer.setColor(backColor);
            this.draw(text, x + 1, y - 1, textLineHeight);
            this.textRenderer.setColor(textColor);
        }
        else if (effect.equals(AVKey.TEXT_EFFECT_OUTLINE))
        {
            this.textRenderer.setColor(backColor);
            this.draw(text, x, y + 1, textLineHeight);
            this.draw(text, x + 1, y, textLineHeight);
            this.draw(text, x, y - 1, textLineHeight);
            this.draw(text, x - 1, y, textLineHeight);
            this.textRenderer.setColor(textColor);
        }
        // Draw normal text
        this.draw(text, x, y, textLineHeight);
    }

    /**
     * Draw a multi-line text string with bounding rectangle top starting at the y position. Depending on the current
     * textAlign, the x position is either the rectangle left side, middle or right side.
     * <p>
     * Uses the given line height.
     *
     * @param text           the multi-line text to draw.
     * @param x              the x position for top left corner of text rectangle.
     * @param y              the y position for top left corner of the text rectangle.
     * @param textLineHeight the line height in pixels.
     */
    public void draw(String text, int x, int y, int textLineHeight)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String[] lines = text.split("\n");
        for (String line : lines)
        {
            int xAligned = x;
            if (this.textAlign.equals(AVKey.CENTER))
                xAligned = x - (int) (this.textRenderer.getBounds(line).getWidth() / 2);
            else if (this.textAlign.equals(AVKey.RIGHT))
                xAligned = x - (int) (this.textRenderer.getBounds(line).getWidth());
            y -= textLineHeight;
            this.textRenderer.draw3D(line, xAligned, y, 0, 1);
            y -= this.lineSpacing;
        }
    }

    /**
     * Draw text with unique colors word bounding rectangles and add each as a pickable object to the provided
     * PickSupport instance.
     *
     * @param text           the multi-line text to draw.
     * @param x              the x position for top left corner of text rectangle.
     * @param y              the y position for top left corner of the text rectangle.
     * @param textLineHeight the line height in pixels.
     * @param dc             the current DrawContext.
     * @param pickSupport    the PickSupport instance to be used.
     * @param refObject      the user reference object associated with every picked word.
     * @param refPosition    the user reference Position associated with every picked word.
     */
    public void pick(String text, int x, int y, int textLineHeight,
        DrawContext dc, PickSupport pickSupport, Object refObject, Position refPosition)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (pickSupport == null)
        {
            String msg = Logging.getMessage("nullValue.PickSupportIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String[] lines = text.split("\n");
        for (String line : lines)
        {
            int xAligned = x;
            if (this.textAlign.equals(AVKey.CENTER))
                xAligned = x - (int) (this.textRenderer.getBounds(line).getWidth() / 2);
            else if (this.textAlign.equals(AVKey.RIGHT))
                xAligned = x - (int) (this.textRenderer.getBounds(line).getWidth());
            y -= textLineHeight;
            drawLineWithUniqueColors(line, xAligned, y, dc, pickSupport, refObject, refPosition);
            y -= this.lineSpacing;
        }
    }

    protected void drawLineWithUniqueColors(String text, int x, int y,
        DrawContext dc, PickSupport pickSupport, Object refObject, Position refPosition)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        //float spaceWidth = this.textRenderer.getCharWidth(' ');
        float drawX;
        String source = text.trim();
        int start = 0;
        int end = source.indexOf(' ', start + 1);
        while (start < source.length())
        {
            if (end == -1)
                end = source.length();   // last word
            // Extract a 'word' which is in fact a space and a word except for first word
            String word = source.substring(start, end);
            // Measure word and already draw line part - from line beginning
            Rectangle2D wordBounds = this.textRenderer.getBounds(word);
            Rectangle2D drawnBounds = this.textRenderer.getBounds(source.substring(0, start));
            drawX = x + (start > 0 ? (float) drawnBounds.getWidth() + (float) drawnBounds.getX() : 0);
            // Add pickable object
            Color color = dc.getUniquePickColor();
            int colorCode = color.getRGB();
            PickedObject po = new PickedObject(colorCode, refObject, refPosition, false);
            po.setValue(AVKey.TEXT, word.trim());
            pickSupport.addPickableObject(po);
            // Draw word rectangle
            gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
            drawFilledRectangle(dc, drawX + wordBounds.getX(), y - wordBounds.getHeight() - wordBounds.getY(),
                wordBounds.getWidth(), wordBounds.getHeight());
            // Move forward in source string
            start = end;
            if (start < source.length() - 1)
            {
                end = source.indexOf(' ', start + 1);
            }
        }
    }

    /**
     * Add 'new line' characters inside a string so that it's bounding rectangle tries not to exceed the given dimension
     * width.
     * <p>
     * If the dimension height is more than zero, the text will be truncated accordingly and the continuation string
     * will be appended to the last line.
     * <p>
     * Note that words will not be split and at least one word will be used per line so the longest word defines the
     * final width of the bounding rectangle. Each line is trimmed of leading and trailing spaces.
     *
     * @param text   the text string to wrap
     * @param width  the maximum width in pixels the text can occupy.
     * @param height if not zero, the maximum height in pixels the text can occupy.
     *
     * @return the wrapped string.
     */
    public String wrap(String text, int width, int height)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String[] lines = text.split("\n");
        StringBuffer wrappedText = new StringBuffer();
        // Wrap each line
        for (int i = 0; i < lines.length; i++)
        {
            lines[i] = this.wrapLine(lines[i], width);
        }
        // Concatenate all lines in one string with new line separators
        // between lines - not at the end
        // Checks for height limit.
        int currentHeight = 0;
        boolean heightExceeded = false;
        double maxLineHeight = getMaxLineHeight(this.textRenderer);
        for (int i = 0; i < lines.length && !heightExceeded; i++)
        {
            String[] subLines = lines[i].split("\n");
            for (int j = 0; j < subLines.length && !heightExceeded; j++)
            {
                if (height <= 0 || currentHeight + maxLineHeight <= height)
                {
                    wrappedText.append(subLines[j]);
                    currentHeight += maxLineHeight + this.lineSpacing;
                    if (j < subLines.length - 1)
                        wrappedText.append('\n');
                }
                else
                {
                    heightExceeded = true;
                }
            }
            if (i < lines.length - 1 && !heightExceeded)
                wrappedText.append('\n');
        }
        // Add continuation string if text truncated
        if (heightExceeded)
        {
            if (wrappedText.length() > 0)
                wrappedText.deleteCharAt(wrappedText.length() - 1); // Remove excess new line
            wrappedText.append(this.continuationString);
        }
        return wrappedText.toString();
    }

    // Wrap one line to fit the given width
    protected String wrapLine(String text, int width)
    {
        StringBuffer wrappedText = new StringBuffer();
        // Single line - trim leading and trailing spaces
        String source = text.trim();
        Rectangle2D lineBounds = this.textRenderer.getBounds(source);
        if (lineBounds.getWidth() > width)
        {
            // Split single line to fit preferred width
            StringBuffer line = new StringBuffer();
            int start = 0;
            int end = source.indexOf(' ', start + 1);
            while (start < source.length())
            {
                if (end == -1)
                    end = source.length();   // last word
                // Extract a 'word' which is in fact a space and a word
                String word = source.substring(start, end);
                String linePlusWord = line + word;
                if (this.textRenderer.getBounds(linePlusWord).getWidth() <= width)
                {
                    // Keep adding to the current line
                    line.append(word);
                }
                else
                {
                    // Width exceeded
                    if (line.length() != 0)
                    {
                        // Finish current line and start new one
                        wrappedText.append(line);
                        wrappedText.append('\n');
                        line.delete(0, line.length());
                        line.append(word.trim());  // get read of leading space(s)
                    }
                    else
                    {
                        // Line is empty, force at least one word
                        line.append(word.trim());
                    }
                }
                // Move forward in source string
                start = end;
                if (start < source.length() - 1)
                {
                    end = source.indexOf(' ', start + 1);
                }
            }
            // Gather last line
            wrappedText.append(line);
        }
        else
        {
            // Line doesn't need to be wrapped
            wrappedText.append(source);
        }
        return wrappedText.toString();
    }

    //** Very very simple html support *******************************************
    // Handles <P></P>, <BR /> or <BR>, <B></B>, <I></I>, <A HREF="..."></A>
    // and <font color="#ffffff"></font>.
    //****************************************************************************

    protected static Pattern SGMLPattern = Pattern.compile("<[^\\s].*?>"); // Find sgml tags
    protected static Pattern SGMLOrSpacePattern = Pattern.compile("(<[^\\s].*?>)|(\\s)"); // Find sgml tags or spaces

    /**
     * Return true if the text contains some sgml tags.
     *
     * @param text The text string to evaluate.
     *
     * @return true if the string contains sgml or html tags
     */
    public static boolean containsHTML(String text)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Matcher matcher = SGMLPattern.matcher(text);
        return matcher.find();
    }

    /**
     * Remove new line characters then replace BR and P tags with appropriate new lines.
     *
     * @param text The html text string to process.
     *
     * @return The processed text string.
     */
    public static String processLineBreaksHTML(String text)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        text = text.replaceAll("\n", ""); // Remove all new line characters
        text = text.replaceAll("(?i)<br\\s?.*?>", "\n"); // Replace <br ...> with one new line
        text = text.replaceAll("(?i)<p\\s?.*?>", ""); // Replace <p ...> with nothing
        text = text.replaceAll("(?i)</p>", "\n\n"); // Replace </p> with two new line
        return text;
    }

    /**
     * Remove all HTML tags from a text string.
     *
     * @param text the string to filter.
     *
     * @return the filtered string.
     */
    public static String removeTagsHTML(String text)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return text.replaceAll("<[^\\s].*?>", "");
    }

    /**
     * Extract an attribute value from a HTML tag string. The attribute is expected to be formed on the pattern:
     * name="...". Other variants will likely fail.
     *
     * @param text          the HTML tage string.
     * @param attributeName the attribute name.
     *
     * @return the attribute value found. Null if empty or not found.
     */
    public static String getAttributeFromTagHTML(String text, String attributeName)
    {
        if (text == null || attributeName == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Look for name="..." - will not work for other variants
        Pattern pattern = Pattern.compile("(?i)" + attributeName.toLowerCase() + "=\"([^\"].*?)\"");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
            return matcher.group(1);

        return null;
    }

    // --- HTML Word iterator based methods --------------------------------

    /**
     * Returns the bounding rectangle for a multi-line html string.
     *
     * @param text      the multi-line html text to evaluate.
     * @param renderers a {@link TextRendererCache} instance.
     *
     * @return the bounding rectangle for the rendered text.
     */
    public Rectangle getBoundsHTML(String text, TextRendererCache renderers)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (renderers == null)
        {
            String msg = Logging.getMessage("nullValue.TextRendererCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        DrawState ds = new DrawState(renderers, this.textRenderer.getFont(), null, this.textColor);
        return getTextBoundsHTML(text, ds).getBounds();
    }

    protected Rectangle2D getTextBoundsHTML(String text, DrawState ds)
    {
        double width = 0;
        double height = 0;
        String[] lines = text.split("\n");
        for (String line : lines)
        {
            Rectangle2D lineBounds = getLineBoundsHTML(line, ds);
            width = Math.max(lineBounds.getWidth(), width);
            height += lineBounds.getHeight() + this.lineSpacing;
        }
        height -= this.lineSpacing;  // remove last line spacing

        return new Rectangle2D.Double(0, 0, width, height);
    }

    protected Rectangle2D getLineBoundsHTML(String line, DrawState ds)
    {
        double width = 0;
        double height = getMaxLineHeight(ds.textRenderer);
        Iterator wi = new WordIteratorHTML(line);
        while (wi.hasNext())
        {
            // Acumulate words dimensions
            Rectangle2D wordBounds = getWordBoundsHTML((String) wi.next(), ds);
            width += wordBounds.getWidth();
            height = Math.max(wordBounds.getHeight(), height);
            // Count a space between words - not after last.
            if (wi.hasNext())
                width += ds.textRenderer.getCharWidth(' ');
        }

        return new Rectangle2D.Double(0, 0, width, height);
    }

    protected Rectangle2D getWordBoundsHTML(String word, DrawState ds)
    {
        double width = 0;
        double height = getMaxLineHeight(ds.textRenderer);
        int start = 0;
        String part;
        Rectangle2D partBounds;
        Matcher matcher = SGMLOrSpacePattern.matcher(word);  // html tags or spaces
        while (matcher.find())
        {
            if (!matcher.group().equals(" "))  // html tag - not a space
            {
                if (matcher.start() > start)
                {
                    // Measure part
                    part = word.substring(start, matcher.start());
                    partBounds = ds.textRenderer.getBounds(part);
                    width += partBounds.getWidth() + partBounds.getX();
                }
                // Apply html tag to draw state
                ds.updateFromHTMLTag(matcher.group(), false);
                height = Math.max(getMaxLineHeight(ds.textRenderer), height);
                start = matcher.end();
            }
        }
        if (start < word.length())
        {
            // Measure remaining part if any
            part = word.substring(start, word.length());
            partBounds = ds.textRenderer.getBounds(part);
            width += partBounds.getWidth() + partBounds.getX();
        }

        return new Rectangle2D.Double(0, 0, width, height);
    }

    /**
     * Add 'new line' characters inside an html text string so that it's bounding rectangle tries not to exceed the
     * given dimension width.
     * <p>
     * If the dimension height is more than zero, the text will be truncated accordingly and the continuation string
     * will be appended to the last line.
     * <p>
     * Note that words will not be split and at least one word will be used per line so the longest word defines the
     * final width of the bounding rectangle. Each line is trimmed of leading and trailing spaces.
     *
     * @param text      the html text string to wrap
     * @param width     the maximum width in pixels one text line can occupy.
     * @param height    if not zero, the maximum height the text can occupy.
     * @param renderers a {@link TextRendererCache} instance.
     *
     * @return the wrapped html string
     */
    public String wrapHTML(String text, double width, double height, TextRendererCache renderers)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (renderers == null)
        {
            String msg = Logging.getMessage("nullValue.TextRendererCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        DrawState ds = new DrawState(renderers, this.textRenderer.getFont(), null, this.textColor);
        return wrapTextHTML(text, width, height, ds);
    }

    protected String wrapTextHTML(String text, double width, double height, DrawState ds)
    {
        // Save passed draw state in case we need to trim text later
        DrawState savedState = new DrawState(ds);

        StringBuffer wrappedText = new StringBuffer();
        String[] lines = text.split("\n");
        for (String line : lines)
        {
            wrappedText.append(wrappedText.length() > 0 ? "\n" : "");
            wrappedText.append(wrapLineHTML(line, width, ds));
        }

        if (height > 0)
            return trimTextHTML(wrappedText.toString(), height, savedState);

        return wrappedText.toString();
    }

    protected String trimTextHTML(String text, double height, DrawState ds)
    {
        StringBuffer wrappedText = new StringBuffer();
        double currentHeight = 0;
        String[] lines = text.split("\n");
        for (String line : lines)
        {
            Rectangle2D lineBounds = getLineBoundsHTML(line, ds);
            if (currentHeight + lineBounds.getHeight() <= height)
            {
                wrappedText.append(wrappedText.length() > 0 ? "\n" : "");
                wrappedText.append(line);
                currentHeight += lineBounds.getHeight() + this.lineSpacing;
            }
            else
            {
                // Text is longer then allowed. Truncate and add continuation string
                wrappedText.append(this.continuationString);
                break;
            }
        }

        return wrappedText.toString();
    }

    protected String wrapLineHTML(String line, double width, DrawState ds)
    {
        // Save passed draw state in case we need to wrap
        DrawState savedState = new DrawState(ds);

        // Measure line - note this updates the caller draw state
        Rectangle2D lineBounds = getLineBoundsHTML(line, ds);
        if (lineBounds.getWidth() <= width)
            return line;

        // The line needs to be wrapped
        double spaceWidth, wordWidth, lineWidth = 0;
        StringBuffer wrappedText = new StringBuffer();
        WordIteratorHTML wi = new WordIteratorHTML(line);
        while (wi.hasNext())
        {
            String word = wi.next();
            spaceWidth = savedState.textRenderer.getCharWidth(' ');
            wordWidth = getWordBoundsHTML(word, savedState).getWidth();
            if (lineWidth == 0 || lineWidth + wordWidth + (lineWidth > 0 ? spaceWidth : 0) <= width)
            {
                // Add space and word to line
                wrappedText.append(lineWidth > 0 ? " " : "");
                wrappedText.append(word);
                lineWidth += wordWidth + (lineWidth > 0 ? spaceWidth : 0);
            }
            else
            {
                // Width exceeded, start new line
                wrappedText.append("\n");
                wrappedText.append(word);
                lineWidth = wordWidth;
            }
        }

        return wrappedText.toString();
    }

    /**
     * Draw text with unique colors word bounding rectangles and add each as a pickable object to the provided
     * PickSupport instance.
     *
     * @param text        the multi-line text to draw.
     * @param x           the x position for top left corner of text rectangle.
     * @param y           the y position for top left corner of the text rectangle.
     * @param renderers   a {@link TextRendererCache} instance.
     * @param dc          the current DrawContext.
     * @param pickSupport the PickSupport instance to be used.
     * @param refObject   the user reference object associated with every picked word.
     * @param refPosition the user reference Position associated with every picked word.
     */
    public void pickHTML(String text, int x, int y, TextRendererCache renderers, DrawContext dc,
        PickSupport pickSupport, Object refObject, Position refPosition)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (pickSupport == null)
        {
            String msg = Logging.getMessage("nullValue.PickSupportIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Set picking
        this.drawContext = dc;
        this.pickSupport = pickSupport;
        this.pickObject = refObject;
        this.pickPosition = refPosition;
        this.isPicking = true;

        // Draw
        try
        {
            drawHTML(text, x, y, renderers);
        }
        finally
        {
            this.isPicking = false;
        }
    }

    /**
     * Draw a multi-line html text string with bounding rectangle top starting at the y position. The x position is
     * eiher the rectangle left side, middle or right side depending on the current text alignement.
     *
     * @param text      the multi-line text to draw
     * @param x         the x position for top left corner of text rectangle
     * @param y         the y position for top left corner of the text rectangle
     * @param renderers a {@link TextRendererCache} instance.
     */
    public void drawHTML(String text, double x, double y, TextRendererCache renderers)
    {
        if (text == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (renderers == null)
        {
            String msg = Logging.getMessage("nullValue.TextRendererCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Draw attributes
        DrawState ds = new DrawState(renderers, this.textRenderer.getFont(), null, this.textColor);
        drawTextHTML(text, x, y, ds);
    }

    protected void drawTextHTML(String text, double x, double y, DrawState ds)
    {
        if (!this.isPicking)
            ds.textRenderer.begin3DRendering();
        try
        {
            ds.textRenderer.setColor(this.textColor);

            Rectangle2D lineBounds;
            double drawX, drawY = y;
            String[] lines = text.split("\n");
            for (String line : lines)
            {
                // Set line start x
                drawX = x;
                lineBounds = getTextBoundsHTML(line, new DrawState(ds));
                if (this.textAlign.equals(AVKey.CENTER))
                    drawX = x - lineBounds.getWidth() / 2;
                else if (this.textAlign.equals(AVKey.RIGHT))
                    drawX = x - lineBounds.getWidth();

                // Skip line height
                drawY -= lineBounds.getHeight();
                // Draw line
                drawLineHTML(line, drawX, drawY, ds);
                // Skip line spacing
                drawY -= this.lineSpacing;
            }
        }
        finally
        {
            if (!this.isPicking)
                ds.textRenderer.end3DRendering();
        }
    }

    protected void drawLineHTML(String line, double x, double y, DrawState ds)
    {
        String word;
        Rectangle2D wordBounds;
        WordIteratorHTML wi = new WordIteratorHTML(line);
        double drawX = x;
        while (wi.hasNext())
        {
            word = wi.next();
            wordBounds = getWordBoundsHTML(word, new DrawState(ds));
            if (this.isPicking)
                pickWordHTML(word, drawX, y, ds);
            else
                drawWordHTML(word, drawX, y, ds);
            drawX += wordBounds.getWidth() + ds.textRenderer.getCharWidth(' ');
        }
    }

    protected void drawWordHTML(String word, double x, double y, DrawState ds)
    {
        double drawX = x;
        int start = 0;
        String part;
        Rectangle2D partBounds;
        Matcher matcher = SGMLOrSpacePattern.matcher(word);  // html tags or spaces
        while (matcher.find())
        {
            if (!matcher.group().equals(" "))  // html tag - not a space
            {
                if (matcher.start() > start)
                {
                    // Draw part
                    part = word.substring(start, matcher.start());
                    partBounds = ds.textRenderer.getBounds(part);
                    ds.textRenderer.draw(part, (int) drawX, (int) y);
                    drawX += partBounds.getWidth() + partBounds.getX();
                }
                // Apply html tag to draw state
                ds.updateFromHTMLTag(matcher.group(), true);
                start = matcher.end();
            }
        }
        if (start < word.length())
        {
            // Draw remaining part if any
            part = word.substring(start, word.length());
            ds.textRenderer.draw(part, (int) drawX, (int) y);
        }
    }

    protected void pickWordHTML(String word, double x, double y, DrawState ds)
    {
        double drawX = x;
        int start = 0;
        String part;
        Rectangle2D partBounds;
        boolean expandStart = true;
        Matcher matcher = SGMLOrSpacePattern.matcher(word);  // html tags or spaces
        while (matcher.find())
        {
            if (!matcher.group().equals(" "))  // html tag - not a space
            {
                if (matcher.start() > start)
                {
                    // Pick part
                    part = word.substring(start, matcher.start());
                    partBounds = ds.textRenderer.getBounds(part);
                    pickWordPartHTML(part, drawX, y, partBounds, ds, expandStart);
                    expandStart = false;
                    drawX += partBounds.getWidth() + partBounds.getX();
                }
                // Apply html tag to draw state
                ds.updateFromHTMLTag(matcher.group(), false);
                start = matcher.end();
            }
        }
        if (start < word.length())
        {
            // Pick remaining part if any
            part = word.substring(start, word.length());
            partBounds = ds.textRenderer.getBounds(part);
            pickWordPartHTML(part, drawX, y, partBounds, ds, expandStart);
        }
    }

    protected void pickWordPartHTML(String word, double x, double y, Rectangle2D partBounds, DrawState ds,
        boolean expandStart)
    {
        String hyperlink = ds.getDrawAttributes().hyperlink;
        // Extend pick rectangle width to fill a bit more then half a space before and after the word.
        // Extend height a little too.
        double spaceWidth = ds.textRenderer.getCharWidth(' ') * 1.5;
        double height = this.getMaxLineHeight(ds.textRenderer);
        Rectangle2D pickBounds;
        if (expandStart)
        {
            pickBounds = new Rectangle2D.Double(0, 0, partBounds.getWidth() + partBounds.getX() + spaceWidth,
                height * 1.1);
            x -= spaceWidth / 2;
        }
        else
        {
            pickBounds = new Rectangle2D.Double(0, 0, partBounds.getWidth() + partBounds.getX() + spaceWidth / 2,
                height * 1.1);
        }

        pickWord(word, hyperlink, x, y, pickBounds,
            this.drawContext, this.pickSupport, this.pickObject, this.pickPosition);
    }

    protected void pickWord(String word, String hyperlink, double drawX, double drawY, Rectangle2D wordBounds,
        DrawContext dc, PickSupport pickSupport, Object refObject, Position refPosition)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Add pickable object
        Color color = dc.getUniquePickColor();
        int colorCode = color.getRGB();
        PickedObject po = new PickedObject(colorCode, refObject, refPosition, false);
        po.setValue(AVKey.TEXT, removeTagsHTML(word.trim()));
        if (hyperlink != null)
            po.setValue(AVKey.URL, hyperlink);
        pickSupport.addPickableObject(po);
        // Draw word rectangle
        gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
        drawFilledRectangle(dc, drawX, drawY - wordBounds.getHeight() / 4,  // TODO: handle font descent properly
            wordBounds.getWidth(), wordBounds.getHeight());
    }

    // Draw a filled rectangle
    protected void drawFilledRectangle(DrawContext dc, double x, double y, double width, double height)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex3d(x, y, 0);
        gl.glVertex3d(x + width - 1, y, 0);
        gl.glVertex3d(x + width - 1, y + height - 1, 0);
        gl.glVertex3d(x, y + height - 1, 0);
        gl.glVertex3d(x, y, 0);
        gl.glEnd();
    }

    // -- HTML word iterator ------------------------------------

    /**
     * Iterates through words from an HTML text line. Note that returned 'words' can contain html tags at any place,
     * before, inside or after the word.
     */
    public static class WordIteratorHTML implements Iterator<String>
    {
        protected ArrayList<String> words;
        protected int nextWord = -1;
        protected static Pattern SGMLOrSpacePattern = Pattern.compile("(<[^\\s].*?>)|(\\s)");

        public WordIteratorHTML(String text)
        {
            Matcher matcher = SGMLOrSpacePattern.matcher(text);
            this.words = new ArrayList<String>();

            int start = 0;
            while (matcher.find())
            {
                if (matcher.group().equals(" "))
                {
                    // Space found, add word to list
                    addWord(text.substring(start, matcher.end()));
                    start = matcher.end(); // move after space found
                }
            }
            // Add end of text if any
            if (start < text.length())
                addWord(text.substring(start));

            // Set next word index
            if (this.words.size() > 0)
                this.nextWord = 0;
        }

        protected void addWord(String word)
        {
            word = word.trim();
            if (word.length() > 0)
                words.add(word);
        }

        public boolean hasNext()
        {
            return this.nextWord != -1 && this.nextWord < words.size();
        }

        public String next()
        {
            return words.get(this.nextWord++);
        }

        public void remove()
        {
        }
    }

    // -- HTML Draw state handling -----------------------------------

    protected class DrawState
    {
        protected class DrawAttributes
        {
            protected final Font font;
            protected String hyperlink;
            protected final Color color;

            public DrawAttributes(Font font, String hyperlink, Color color)
            {
                this.font = font;
                this.hyperlink = hyperlink;
                this.color = color;
            }
        }

        protected ArrayList<DrawAttributes> stack = new ArrayList<DrawAttributes>();
        protected TextRendererCache renderers;
        public TextRenderer textRenderer;
        protected Pattern SGMLPattern = Pattern.compile("(<[^\\s].*?>)");

        public DrawState(TextRendererCache renderers, Font font, String hyperlink, Color color)
        {
            this.push(new DrawAttributes(font, hyperlink, color));
            this.renderers = renderers;
            this.textRenderer = getTextRenderer(font);
        }

        public DrawState(DrawState ds)
        {
            for (DrawAttributes da : ds.stack)
            {
                this.push(new DrawAttributes(da.font, da.hyperlink, da.color));
            }
            this.renderers = ds.renderers;
            this.textRenderer = ds.textRenderer;
        }

        public DrawAttributes getDrawAttributes()
        {
            if (this.stack.size() < 1)
                return null;
            return this.stack.get(this.stack.size() - 1);
        }

        protected TextRenderer getTextRenderer(Font font)
        {
            return OGLTextRenderer.getOrCreateTextRenderer(this.renderers, font);
        }

        protected Font getFont(Font font, boolean isBold, boolean isItalic)
        {
            int fontStyle = isBold ? (isItalic ? Font.BOLD | Font.ITALIC : Font.BOLD)
                : (isItalic ? Font.ITALIC : Font.PLAIN);
            return font.deriveFont(fontStyle);
        }

        // Update DrawState from html text
        public void updateFromHTMLText(String text, boolean startStopRendering)
        {
            Matcher matcher = SGMLPattern.matcher(text);
            while (matcher.find())
            {
                updateFromHTMLTag(matcher.group(), startStopRendering);
            }
        }

        // Update DrawState from html tag
        public void updateFromHTMLTag(String tag, boolean startStopRendering)
        {
            DrawAttributes da = getDrawAttributes();
            boolean fontChanged = false;

            if (tag.equalsIgnoreCase("<b>"))
            {
                this.push(new DrawAttributes(getFont(da.font, true, da.font.isItalic()),
                    da.hyperlink, da.color));
                fontChanged = true;
            }
            else if (tag.equalsIgnoreCase("</b>"))
            {
                this.pop();
                fontChanged = true;
            }
            else if (tag.equalsIgnoreCase("<i>"))
            {
                this.push(new DrawAttributes(getFont(da.font, da.font.isBold(), true),
                    da.hyperlink, da.color));
                fontChanged = true;
            }
            else if (tag.equalsIgnoreCase("</i>"))
            {
                this.pop();
                fontChanged = true;
            }
            else if (tag.toLowerCase().startsWith("<a "))
            {
                this.push(new DrawAttributes(da.font,
                    MultiLineTextRenderer.getAttributeFromTagHTML(tag, "href"), applyTextAlpha(linkColor)));
                if (startStopRendering)
                    this.textRenderer.setColor(applyTextAlpha(linkColor));
            }
            else if (tag.equalsIgnoreCase("</a>"))
            {
                this.pop();
                if (startStopRendering)
                    this.textRenderer.setColor(getDrawAttributes().color);
            }
            else if (tag.toLowerCase().startsWith("<font "))
            {
                String colorCode = MultiLineTextRenderer.getAttributeFromTagHTML(tag, "color");
                if (colorCode != null)
                {
                    Color color = da.color;
                    try
                    {
                        color = applyTextAlpha(Color.decode(colorCode));
                    }
                    catch (Exception ignore)
                    {
                    }
                    this.push(new DrawAttributes(da.font, da.hyperlink, color));
                    if (startStopRendering)
                        this.textRenderer.setColor(color);
                }
            }
            else if (tag.equalsIgnoreCase("</font>"))
            {
                this.pop();
                if (startStopRendering)
                    this.textRenderer.setColor(getDrawAttributes().color);
            }

            if (fontChanged)
            {
                // Terminate current rendering
                if (startStopRendering)
                    this.textRenderer.end3DRendering();
                // Get new text renderer
                da = getDrawAttributes();
                this.textRenderer = getTextRenderer(da.font);
                // Resume rendering
                if (startStopRendering)
                {
                    this.textRenderer.begin3DRendering();
                    this.textRenderer.setColor(da.color);
                }
            }
        }

        protected void push(DrawAttributes da)
        {
            this.stack.add(da);
        }

        protected void pop()
        {
            if (this.stack.size() > 1)
                this.stack.remove(this.stack.size() - 1);
        }

        protected Color applyTextAlpha(Color color)
        {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                color.getAlpha() * textColor.getAlpha() / 255);
        }
    }
}
