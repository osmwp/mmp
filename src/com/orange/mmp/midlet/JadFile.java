/* -----------------------------------------------------------------------------
 * Antenna - An Ant-to-end solution for wireless Java 
 *
 * Copyright (c) 2002-2004 Joerg Pleumann <joerg@pleumann.de>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * -----------------------------------------------------------------------------
 */
package com.orange.mmp.midlet;


/**
 * A simple class to access the contents of a JAD file. The JAD file is held in
 * memory for easy access and has to be written back to disk later for the
 * changes to take effect. Actually this class contains a lot more methods than
 * needed for JAD management, but I didn't have the time to strip it down. :-)
 * The really important ones are load(), save(), getValue() and setValue().
 *
 * @author Joerg Pleumann &lt;joerg@pleumann.de&gt;
 */
public class JadFile extends PropertiesFile {
    
    /**
     * Inner class that represents the definition of a single MIDlet.
     */
    public class MIDletData {
        /**
         * The MIDlet's number.
         */
        private int number;

        /**
         * The MIDlet's name.
         */
        private String name;


        /**
         * The MIDlet's icon.
         */
        private String icon;

        /**
         * The MIDlet's main class.
         */
        private String cls;

        /**
         * Creates a new instance of the inner class.
         */
        private MIDletData(int number, String name, String icon, String cls) {
            this.number = number;
            this.name = name;
            this.icon = icon;
            if (cls != null) 
                this.cls = cls.replace('/', '.');
        }

        /**
         * Returns the MIDlet's number.
         */
        public int getNumber() {
            return number;
        }

        /**
         * Returns the MIDlet's name, or null, if the MIDlet doesn't have a name.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the MIDlet's main class, or null, if the MIDlet doesn't
         * have a name (which would be an error in the JAR file, of course.
         */
        public String getClassName() {
            return cls;
        }

        /**
         * Returns the MIDlet's icon, or null, if the MIDlet doesn't have an
         * icon.
         */
        public String getIcon() {
            return icon;
        }
    }
    
    public JadFile() {
    	separator = ':'; // override default separator to JAD separator
	}

    /**
     * Returns the definition of the given MIDlet, or null, if the MIDlet
     * doesn't exist. Note that MIDlet numbering starts at 1.
     */
    public MIDletData getMIDlet(int i) {
        String value = getValue("MIDlet-" + i);

        if (value == null)
            return null;

        int p1 = value.indexOf(',');

        String name = null;
        String icon = null;
        String cls = null;

        if (p1 != -1) {
            name = value.substring(0, p1).trim();
 
            int p2 = value.indexOf(',', p1 + 1);

            if (p2 != -1) {
                icon = value.substring(p1 + 1, p2).trim();
                cls = value.substring(p2 + 1).trim();
            }
            else {
                icon = value.substring(p1 + 1).trim();
            }
        }
        else {
            name = value.trim();
        }

        //System.out.println ("name: "+name+" icon: "+icon+ " cls: "+cls);

        if ("".equals(name)) name = null;
        if ("".equals(cls)) cls = null;
        if ("".equals(icon)) icon = null;

        return new MIDletData(i, name, icon, cls);
    }
}