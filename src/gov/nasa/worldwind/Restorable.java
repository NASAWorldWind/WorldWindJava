/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
