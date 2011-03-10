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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.orange.mmp.util.MultiLinesPropertiesReader;



public class PropertiesFile {


	protected char separator = '=';

	/**
	 * Holds the properties lines of the file.
	 * Multi-lines properties are seen as a unique string here
	 */
	private Vector<String> propStrings = new Vector<String>();

	/**
	 * Holds the properties "physical" lines of the file.
	 * Physical indicates that multi-lines properties are also splitted as multi-lines here.
	 */
	private List<String[]> physStrings = new ArrayList<String[]>();
	
	private int nbPhysLines = 0;

	private String toSingleString(String[] strings) {
		StringBuilder str = new StringBuilder();
		for(String s : strings) str.append(s.trim());
		return str.toString();
	}
	
	/**
	 * Adds a line to the file.
	 */
	public int add(String... s) {
		int result = size();
		insert(result, s);
		return result;
	}

	/**
	 * Assigns all values from another file.
	 */
	public void assign(JadFile jad) {
		assign(jad, false);
	}

	/**
	 * Assigns values from another file. If the manifest parameter is true,
	 * only values valid for a MANIFEST.MF file are copied.
	 */
	public void assign(JadFile jad, boolean manifest) {
		clear();

		for (int i = 0; i < jad.size(); i++) {
			if (manifest) {
				add(jad.get(i, false));
			}
			else {
				add(jad.get(i));
			}
		}
	}

	/**
	 * Clear the file.
	 */
	public void clear() {
		this.propStrings = new Vector<String>();
		this.physStrings = new ArrayList<String[]>();
		this.nbPhysLines = 0;
	}

	/**
	 * Deletes a line from the file.
	 */
	public void delete(int index) {
		this.propStrings.removeElementAt(index);
		String[] removed = this.physStrings.remove(index);
		this.nbPhysLines -= removed.length;
	}

	/**
	 * Returns a line from the file.
	 */
	public String get(int index) {
		return (String) this.propStrings.elementAt(index);
	}

	/**
	 * Returns a line from the file.
	 */
	protected String get(int index, boolean compactLines) {
		if(compactLines) return get(index);
		int pointer = 0;
		do {
			int size = this.physStrings.get(pointer).length;
			if(index < size) break;
			index -= size;
			pointer++;
		}
		while(index > 0);
		return (String) this.physStrings.get(pointer)[index];
	}
	
	/**
	 * Gets the key stored in the given line (everything before the first separator),
	 * or null if the line doesn't contain a key.
	 */
	public String getName(int i) {
		String result = get(i);
		int p = result.indexOf(separator);
		if (p != -1) {
			result = result.substring(0, p);
		}
		else
			result = null;

		return result;
	}

	/**
	 * Gets the value belonging to the given key, or null if the key is not
	 * found.
	 */
	public String getValue(String name) {
		int i = indexOfName(name);
		if (i != -1) {
			String result = get(i);
			i = result.indexOf(separator);
			result = result.substring(i + 1);
			return result.trim();
		}
		else {
			return null;
		}
	}

	/**
	 * Gets the int value belonging to the given key, or null if the key is not
	 * found.
	 */
	public int getIntValue(String name) {
		int i = indexOfName(name);
		if (i != -1) {
			String result = get(i);
			i = result.indexOf(separator);
			result = result.substring(i + 1);
			result.trim();
			int intResult = new Integer(result).intValue();
			return intResult;
		} else {
			return -1;
		}
	}
	
	public boolean deleteValue(String name) {
		int i = indexOfName(name);
		if (i != -1) {
			delete(i);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Finds a whole line in a file. Returns either the index at which the
	 * line was found, or -1 if the line doesn't exist.
	 */
	public int indexOf(String s) {
		String t = s.toLowerCase();
		for (int i = 0; i < size(); i++) {
			if (get(i).toLowerCase().equals(t)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds a key in a JAD file. Returns either the index at which the
	 * key was found, or -1 if the line doesn't exist.
	 */
	public int indexOfName(String name) {
		String s = name.toLowerCase() + separator;
		int i = 0;
		while (i < size()) {
			if (get(i).toLowerCase().startsWith(s)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Inserts a line into the JAD file.
	 */
	public void insert(int index, String... s) {
		this.propStrings.insertElementAt(toSingleString(s), index);
		this.physStrings.add(index, s);
		this.nbPhysLines += s.length;
	}

	/**
	 * Replaces a line in the JAD file.
	 */
	public void set(int index, String... s) {
		this.propStrings.setElementAt(toSingleString(s), index);
		String[] replaced = this.physStrings.set(index, s);
		this.nbPhysLines += (s.length - replaced.length);
	}

	/**
	 * Sets the value of the given key, replacing a previous definition if one
	 * exists.
	 */
	public void setValue(String name, String value) {
		setValue(name, value, true);
	}
	
	/**
	 * Sets the value of the given key, replacing a previous definition if one
	 * exists but not add it if not exists.
	 */
	public void setValue(String name, String value, boolean addIfNotExists) {
		int i = indexOfName(name);
		if (i == -1) {
			if (addIfNotExists && (value != null) && (!value.equals(""))) {
				add(name + ": " + value);
			}
		}
		else {
			if ((value != null) && (!value.equals(""))) {
				set(i, name + ": " + value);
			}
			else {
				delete(i);
			}
		}
	}

	/**
	 * Returns the number of lines in the JAD file.
	 */
	public int size() {
		return this.propStrings.size();
	}
	
	protected int size(boolean compactLines) {
		if(compactLines) return size();
		return this.nbPhysLines;
	}

	/**
	 * Returns the number of MIDlet's in the JAD file.
	 */
	public int getMIDletCount() {
		int i = 1;

		while (indexOfName("MIDlet-" + i) != -1) {
			i++;
		}

		return i - 1;
	}

	private void load(Reader isr) throws IOException {
		clear ();
		MultiLinesPropertiesReader reader = new MultiLinesPropertiesReader (isr);
		String s = reader.readLine();
		while (s != null) {
			/*
			 * I don't think we need line wrapping, because we never handle
			 * manifests in their internal form, and JADs don't do line wrapping.
			 */
			/*
            if (s.startsWith(" ")) {
                set(size() - 1, get(size() - 1) + s.trim());
            }
            else {
                add(s);
            }
			 */
			if (!"".equals(s.trim())) {
				add(reader.getLastReadLines());
			}

			s = reader.readLine();
		}

		reader.close();
	}


	/**
	 * Loads the JAD file from a physical disk file.
	 */
	public void load(String filename, String encoding) throws IOException {
		if (encoding != null) {
			load (new InputStreamReader(new FileInputStream(filename), encoding));
		}
		else {
			load (new InputStreamReader(new FileInputStream(filename)));
		}
	}

	/**
	 * Loads the JAD file from a physical disk file.
	 */
	public void load(java.io.File file) throws IOException {
		load (new InputStreamReader(new FileInputStream(file)));
	}

	/**
	 * Save the JAD file to a physical disk file.
	 */
	public void save(String filename, String encoding) throws IOException {
		save(filename, encoding, false);
	}
	
	public void save(String filename, String encoding, boolean compactLines) throws IOException {
		OutputStreamWriter osw;
		if (encoding != null) {
			osw = new OutputStreamWriter(new FileOutputStream(filename), encoding);
		}
		else {
			osw = new OutputStreamWriter(new FileOutputStream(filename));
		}

		BufferedWriter writer = new BufferedWriter(osw);

		for (int i = 0; i < size(compactLines); i++) {
			String s = get(i, compactLines);
			if ((s != null) && (s.length() != 0)) {
				writer.write(get(i, compactLines));
				writer.newLine();
			}
		}

		writer.close();
	}

	/**
	 * Save the JAD file to a physical disk file.
	 */
	public void save(OutputStream stream) throws IOException {
		save(stream, false);
	}
	
	public void save(OutputStream stream, boolean compactLines) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

		for (int i = 0; i < size(compactLines); i++) {
			String s = get(i, compactLines);
			if ((s != null) && (s.length() != 0)) {
				writer.write(get(i, compactLines));
				writer.newLine();
			}
		}

		writer.flush();
	}

	public Vector<String> getKeys() {
		Vector<String> result = new Vector<String>();

		int j;
		for (int i = 0; i < size(); i++) {
			String tmpKey = get(i);
			j = tmpKey.indexOf(separator);
			tmpKey = tmpKey.substring(0, j);
			result.add(tmpKey);
		}

		return result;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

	public Vector<String> getStrings() {
		return this.propStrings;
	}

}