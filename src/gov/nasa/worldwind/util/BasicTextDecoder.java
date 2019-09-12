/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

/**
 * Base class for text decoders. This decoder handles caching the decoded text, but does not provide any actual decoding
 * logic. This class is thread safe.
 *
 * @author pabercrombie
 * @version $Id: BasicTextDecoder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicTextDecoder implements TextDecoder
{
    protected String text;
    protected String decodedText;
    protected long lastUpdateTime;

    /** {@inheritDoc} */
    public synchronized void setText(String input)
    {
        this.text = input;
        this.decodedText = null;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /** {@inheritDoc} */
    public synchronized String getDecodedText()
    {
        if (this.decodedText == null)
        {
            this.decodedText = this.decode(this.text);
            this.lastUpdateTime = System.currentTimeMillis();
            this.text = null; // Release reference to source text
        }

        return this.decodedText;
    }

    /** {@inheritDoc} */
    public long getLastUpdateTime()
    {
        return this.lastUpdateTime;
    }

    /**
     * Decode the text. Subclasses may override this method to change the decoding.
     *
     * @param textToDecode The text to decode.
     *
     * @return Decoded text.
     */
    protected String decode(String textToDecode)
    {
        return textToDecode;
    }
}
