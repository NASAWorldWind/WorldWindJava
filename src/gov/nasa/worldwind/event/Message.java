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

package gov.nasa.worldwind.event;

/**
 * General purpose message event.
 *
 * @author pabercrombie
 * @version $Id: Message.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Message extends WWEvent
{
    /** Message name. */
    protected String name;
    /** Time at which the message was sent. */
    protected long when;

    /**
     * Create a message. The message will be timestamped with the current system time.
     *
     * @param name   The name of the message.
     * @param source The object that generated the message.
     */
    public Message(String name, Object source)
    {
        this(name, source, System.currentTimeMillis());
    }

    /**
     * Create a message, with a timestamp.
     *
     * @param name   The name of the message.
     * @param source The object that generated the message.
     * @param when   The timestamp to apply to the message.
     */
    public Message(String name, Object source, long when)
    {
        super(source);
        this.name = name;
        this.when = when;
    }

    /**
     * Indicates the message name.
     *
     * @return The message name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Indicates the time at which the message was sent.
     *
     * @return Time, in milliseconds since the Epoch, at which the message was sent.
     */
    public long getWhen()
    {
        return this.when;
    }
}
