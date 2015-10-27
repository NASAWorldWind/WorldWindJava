/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.webview;

import java.net.URL;

/**
 * @author pabercrombie
 * @version $Id: WebResourceResolver.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface WebResourceResolver
{
    URL resolve(String address);
}
