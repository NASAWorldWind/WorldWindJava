/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;

import java.util.*;

/**
 * Tokenizer to read coordinate values from KML coordinate string. The components of each coordinate tuple are separated
 * by commas, as defined by the KML spec, coordinate tuples are comma separated, and each tuple is separated from the
 * surrounding tuples by whitespace. For example:
 * <pre>
 * -18.3,23.5,0 -19.3,23.4,1 -20.0,23.5,2
 * </pre>
 * <p/>
 * However, some KML files do not follow the spec and embed white space within the coordinate tuples. This tokenizer
 * attempts to be lenient with whitespace handling. If a tuple ends with a comma, the tokenizer considers the next token
 * in the input stream to be part of the same coordinate, not the start of a new coordinate.
 * <p/>
 * For example:
 * <pre>
 * -18.3,23.56,9     34.9, 56.0, 2     56.9, 19     90.0,23.9,44
 * </pre>
 * Will be tokenized to four coordinates: (23.56, -18.3, 9), (56.0, 34.9, 2), (56.9, 19, 0), and (90, 23.9, 44).
 * <p/>
 * The tokenizer also handles coordinate strings with no embedded white space. For example:
 * <pre>
 * -18.3,23.56,9,34.9,56.0,2
 * </pre>
 * Will be tokenized to two coordinates: (23.56, -18.3, 9), (56.0, 34.9, 2)
 *
 * @author pabercrombie
 * @version $Id: KMLCoordinateTokenizer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLCoordinateTokenizer
{
    protected int i;
    protected char[] buffer;

    protected List<String> words = new ArrayList<String>(3);

    protected StringBuilder nextWord = new StringBuilder();

    protected boolean inWord;
    protected boolean afterComma = false;

    /**
     * Create a tokenizer to read coordinates from a string.
     *
     * @param s String to read from.
     */
    public KMLCoordinateTokenizer(String s)
    {
        this.buffer = s.trim().toCharArray();
    }

    /**
     * Are there more coordinates to read?
     *
     * @return True if there are more coordinates to read from the string.
     */
    public boolean hasMoreTokens()
    {
        return i < buffer.length;
    }

    /**
     * Read the next {@link Position} from the coordinate string.
     *
     * @return Next Position, or null if an error occurs while parsing the position (number format exception, etc).
     *
     * @throws NumberFormatException if the coordinates cannot be parsed to a number.
     */
    public Position nextPosition() throws NumberFormatException
    {
        this.words.clear();

        while (this.i < this.buffer.length)
        {
            char ch = this.buffer[this.i++];

            if (Character.isWhitespace(ch))
            {
                if (this.inWord)
                    wordBoundary();

                // If the last separator was a comma, don't break. Wait for another word.
                if (!this.afterComma && this.words.size() >= 2)
                    break;
            }
            else if (ch == ',')
            {
                if (this.inWord)
                    wordBoundary();

                this.afterComma = true;

                // Three words make a complete coordinate. Break out of the loop and return the coordinate.
                if (this.words.size() >= 3)
                    break;
            }
            else
            {
                this.inWord = true;
                this.afterComma = false;
                this.nextWord.append(ch);
            }
        }

        if (this.inWord)
            this.wordBoundary();

        return this.makePosition();
    }

    protected Position makePosition()
    {
        if (this.words.size() > 2)
            return Position.fromDegrees(Double.valueOf(this.words.get(1)), Double.valueOf(this.words.get(0)),
                Double.valueOf(this.words.get(2)));
        else if (this.words.size() == 2)
            return Position.fromDegrees(Double.valueOf(this.words.get(1)), Double.valueOf(this.words.get(0)));
        return null;
    }

    protected void wordBoundary()
    {
        this.inWord = false;
        this.words.add(this.nextWord.toString());
        this.nextWord = new StringBuilder();
    }
}
