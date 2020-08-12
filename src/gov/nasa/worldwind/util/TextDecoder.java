/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
     * <p>
     * <em>The update time does not change until {@link #getDecodedText()} is called.</em> An application should call
     * {@link #getDecodedText()}, and then call this method to compare the timestamp with some previous timestamp to
     * determine if the decoded text has changed since {@link #getDecodedText()} was last called.
     *
     * @return The time (as returned by {@code System.currentTimeMillis()}) at which the decoded text last changed.
     *         Returns zero if called before the text is decoded.
     */
    long getLastUpdateTime();
}
