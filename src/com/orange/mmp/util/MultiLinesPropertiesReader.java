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
package com.orange.mmp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tools class used to handle Properties file using multilines
 * 
 * @author ake
 *
 */
public class MultiLinesPropertiesReader extends BufferedReader {
	private static final Pattern PROPERTY_REGEXP = Pattern.compile("[^:\\s]+\\s*:\\s*.*");

	private List<String> bufferedLines = new ArrayList<String>();
	private List<List<String>> physicalLines = new ArrayList<List<String>>();
	private int lineIndex = -1;

	public MultiLinesPropertiesReader(Reader isr) {
		super(isr);
	}

	private void savePhysicalLine(int index, String line) throws IOException {
		if(index > this.physicalLines.size())
			throw new IOException("read error : line index "+(index-1)+" missing");
		else if(index == this.physicalLines.size())
			this.physicalLines.add(new ArrayList<String>());
		this.physicalLines.get(index).add(line);
	}

	private String _readLine(int lineIndex, boolean buffering) throws IOException {
		if(this.bufferedLines.size() > 0) {
			String buffLine = this.bufferedLines.remove(0);
			savePhysicalLine(lineIndex, buffLine);
			return buffLine.trim();
		}
		String currLine = super.readLine();
		if(currLine != null) {
			if(buffering)
				this.bufferedLines.add(currLine);
			else
				savePhysicalLine(lineIndex, currLine);
			currLine = currLine.trim();
		}
		return currLine;
	}

	public synchronized String readLine() throws IOException {
		StringBuilder line = new StringBuilder();
		int lineIndex = ++this.lineIndex;
		String str1 = _readLine(lineIndex, false);
		if(str1 != null) {
			line.append(str1);
			String strn = "";
			Matcher pm = null;
			do {
				line.append(strn);
				strn = _readLine(lineIndex, true);
				if(strn == null) break;
				pm = PROPERTY_REGEXP.matcher(strn);
			}
			while(! pm.matches());
		}
		return line.length() > 0 ? line.toString() : null;
	}

	public synchronized String[] getLastReadLines() {
		Collection<String> lastLines = this.physicalLines.get(this.lineIndex);
		return lastLines.toArray(new String[lastLines.size()]);
	}

	public List<List<String>> getPhysicalReadLines() {
		return this.physicalLines;
	}
}
