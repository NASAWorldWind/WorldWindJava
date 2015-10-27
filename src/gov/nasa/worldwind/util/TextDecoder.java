/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

/**
 * A general purpose text decoder. A text decoder takes an input string and produces a decoded output string.
 *
 * @author pabercrombie
 * @version $Id: TextDecoder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface TextDecoder
{
    /**
     * Set the input text which the decoder will process.
     *
     * @param input Text to decode.
     */
    void setText(String input);

    /**
     * Get the decoded text. This method may be called many times. Implementations should cache decoding results that do
     * not need to be recomputed.
     *
     * @return Text after decoding.
     */
    String getDecodedText();

    /**
     * Get the time at which the decoded text last changed. The text can change because new source text is set, or
     * because an external resource required for decoding has been resolved.
     * <p/>
     * <em>The update time does not change until {@link #getDecodedText()} is called.</em> An application should call
     * {@link #getDecodedText()}, and then call this method to compare the timestamp with some previous timestamp to
     * determine if the decoded text has changed since {@link #getDecodedText()} was last called.
     *
     * @return The time (as returned by {@code System.currentTimeMillis()}) at which the decoded text last changed.
     *         Returns zero if called before the text is decoded.
     */
    long getLastUpdateTime();
}
