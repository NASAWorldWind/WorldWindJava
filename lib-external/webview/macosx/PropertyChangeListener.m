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
#import "PropertyChangeListener.h"

/*
    Version $Id: PropertyChangeListener.m 1171 2013-02-11 21:45:02Z dcollins $
 */
@implementation PropertyChangeListener

/* JNI class, member, and method info global variables. The first parameter specifies the global variable name. */
static JNF_CLASS_CACHE(PropertyChangeListener_class, "java/beans/PropertyChangeListener");
static JNF_MEMBER_CACHE(PropertyChangeListener_propertyChange, PropertyChangeListener_class, "propertyChange", "(Ljava/beans/PropertyChangeEvent;)V");

- (void)propertyChange
{
    // Keep the AppKit thread attached to the JVM as long as it's alive. This causes subsequent calls to JNFObtainEnv to
    // return quickly, and reduces the overhead of frequently using PropertyChangeListener to execute Java code on the AppKit
    // thread.
    JNFThreadContext tc = JNFThreadDetachOnThreadDeath;
    JNIEnv *env = NULL;
    @try
    {
        env = JNFObtainEnv(&tc);
        JNFCallVoidMethod(env, [self jObject], PropertyChangeListener_propertyChange, NULL);
    }
    @finally
    {
        if (env != NULL)
            JNFReleaseEnv(env, &tc);
    }
}

@end