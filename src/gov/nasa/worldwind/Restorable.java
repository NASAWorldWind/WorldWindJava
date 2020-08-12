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

package gov.nasa.worldwind;

/**
 * Restorable is an interface for describing the current state of an object, and restoring an object's
 * state. Object state descriptions will be formatted as an XML document string. This allows the state description
 * to be located in a file, reside in a database, or be passed over a network.
 * <p>
 * The exact structure of the XML document is the responsibility of the implementation. However, to encourage data
 * sharing between similar implementations, each implementation of Restorable should design
 * <code>restoreState</code> to accept and ignore unknown structures in state documents. Otherwise, implementations
 * should clearly document how they will behave when encountering an unknown structure.
 * <p>
 * See the WorldWideWeb Consortium's (W3C) documentation on
 * <a href="http://www.w3.org/TR/xml11/">Extensible Markup Language (XML) 1.1 </a> for information on XML.
 *
 * @author dcollins
 * @version $Id: Restorable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Restorable
{
    /**
     * Returns an XML document string describing the object's state. This state can be restored later by calling
     * <code>restoreState</code> and passing the XML document.
     * 
     * @return an XML document string describing the object's state.
     */
    String getRestorableState();

    /**
     * Restores the object's state to what is described in the specified XML document string.
     *
     * @param stateInXml an XML document string describing an object's state.
     */
    void restoreState(String stateInXml);
}
