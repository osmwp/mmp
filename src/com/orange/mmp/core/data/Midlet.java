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
package com.orange.mmp.core.data;


/***
 * Abstraction class for Midlet
 * @author milletth
 *
 */
public class Midlet {

    /**
     * The Midlet base type (ex: BBTM_Light)
     */
    protected String type;

    /**
     * The Midlet version
     */
    protected Version version;

    /**
     * The Midlet JAD location
     */
    protected String jadLocation;

    /**
     * The Midlet JAR location
     */
    protected String jarLocation;


    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    /**
     * @return the jadLocation
     */
    public String getJadLocation() {
        return jadLocation;
    }

    /**
     * @param jadLocation the jadLocation to set
     */
    public void setJadLocation(String jadLocation) {
        this.jadLocation = jadLocation;
    }

    /**
     * @return the jarLocation
     */
    public String getJarLocation() {
        return jarLocation;
    }

    /**
     * @param jarLocation the jarLocation to set
     */
    public void setJarLocation(String jarLocation) {
        this.jarLocation = jarLocation;
    }


}
