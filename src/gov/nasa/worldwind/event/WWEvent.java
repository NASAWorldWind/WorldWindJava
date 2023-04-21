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

import java.util.EventObject;

/**
 * WWEvent is the base class which all WorldWind event objects derive from. It extends Java's base {@link
 * java.util.EventObject} by adding the capability to consume the event by calling {@link #consume()}. Consuming a
 * WWEvent prevents is from being processed in the default manner by the source that originated the event. If the event
 * cannot be consumed, calling {@code consume()} has no effect, though {@link #isConsumed()} returns whether or not
 * {@code consume()} has been called.
 *
 * @author dcollins
 * @version $Id: WWEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWEvent extends EventObject
{
    /** Denotes whether or not the event has been consumed. Initially {@code false}. */
    protected boolean consumed;

    /**
     * Creates a new WWEvent with the object that originated the event.
     *
     * @param source the object that originated the event.
     *
     * @throws IllegalArgumentException if the source is {@code null}.
     */
    public WWEvent(Object source)
    {
        super(source);
    }

    /**
     * Consumes the event so it will not be processed in the default manner by the source which originated it. This does
     * nothing if the event cannot be consumed.
     */
    public void consume()
    {
        this.consumed = true;
    }

    /**
     * Returns whether or not the event has been consumed.
     * <p>
     * Note: if the event cannot be consumed, this still returns {@code true} if {@link #consume()} has been called,
     * though this has no effect.
     *
     * @return {@code true} if the event has been consumed, and {@code false} otherwise.
     */
    public boolean isConsumed()
    {
        return this.consumed;
    }
}
