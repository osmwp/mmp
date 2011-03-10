/*
 * Copyright (C) 2010 France Telecom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orange.mmp.midlet;

/**
 * Midlets Constants
 * 
 * @author Thomas MILLET
 *
 */
public class Constants {

    /**
     * Character used for line feed
     */
    public static final String LINE_FEED = "\n";
    
    /**
     * Default mobile used when user-agent not found
     */
    public static String DEFAULT_MOBILE = "Default";

     /***************************
     * 	   HTTP HEADERS
     ****************************/
    
    /**
     * HTTP header indicating the JAR content type
     */
    public static final String HTTP_HEADER_JARCONTENT = "application/java-archive";

    /**
     * HTTP header indicating the JAD content type
     */
    public static final String HTTP_HEADER_JADCONTENT = "text/vnd.sun.j2me.app-descriptor";
    
    /***************************
     * 	      JAD FILE
     ****************************/

    /**
     * The Mobile Client version entry key in JAD File
     */
    public static final String JAD_PARAMETER_VERSION = "MIDlet-Version";

    /**
     * The Mobile Client JAR URL entry key in JAD File
     */
    public static final String JAD_PARAMETER_JAR_URL = "MIDlet-Jar-URL";

    /**
     * The Mobile Client JAR SIZE entry key in JAD File
     */
    public static final String JAD_PARAMETER_JAR_SIZE = "MIDlet-Jar-Size";

    /**
     * The Mobile Client application name entry key in JAD File
     */
    public static final String JAD_PARAMETER_APPNAME = "MIDlet-Name";

    /**
     * The Mobile Client launcher entry key in JAD File
     */
    public static final String JAD_PARAMETER_LAUNCHER = "MIDlet-1";

    /**
     * The Zip entry indicating the manifest in Midlet file
     */
    public static final String JAR_MANIFEST_ENTRY = "META-INF/MANIFEST.MF";

    /**
     * Value used in jad to define the icon file of midlet
     */
    public static final String JAD_LAUNCHER_ICON = "icon.png";

    /**
     * Value used in jad to define the main class of midlet
     */
    public static final String JAD_LAUNCHER_MAINCLASS = "MiniPlayer";
    
    /**
     * The Mobile Client EULA URL entry key in JAD File
     */
    public static final String JAD_PARAMETER_WASSUP_HEADERS = "Wassup-Headers";
    
    /**
     * The Mobile Client PFS entry key in JAD File
     */
    public static final String JAD_PARAMETER_PFS = "PFS";
    
    /**
     * The Mobile UA Key entry key in JAD File
     */
    public static final String JAD_PARAMETER_UAKEY = "UAKey";
    
    /**
     * The Mobile Client feed PF entry key in JAD File
     */
    public static final String JAD_PARAMETER_PF_FEED = "PF-Feed";

    /**
     * The file extension used for jad files
     */
    public static final String JAD_FILE_EXTENSION = ".jad";

    /**
     * The file extension used for jar files
     */
    public static final String JAR_FILE_EXTENSION = ".jar";
    
    /**
     * The Mobile Client JAR optional permissions
     */
    public static final String JAD_PARAMETER_OPT_PERMISSIONS = "MIDlet-Permissions-Opt";
    
    /**
     * The Mobile Client extra CSS entry key in JAD File
     */
    public static final String JAD_PARAMETER_MEMO_EXTRA_CSS = "MeMo-Extra-CSS";
}
