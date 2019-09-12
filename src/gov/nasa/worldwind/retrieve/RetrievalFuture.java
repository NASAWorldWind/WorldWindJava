/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

/**
 * @author Tom Gaskins
 * @version $Id: RetrievalFuture.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface RetrievalFuture extends java.util.concurrent.Future<Retriever>
{
    public Retriever getRetriever();
}
