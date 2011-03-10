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

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Abstraction of version field.
 * 
 * Implement Comparable interface.
 * 
 * @author Thomas MILLET
 *
 */
@SuppressWarnings("serial")
public class Version implements Serializable, Comparable<Version>{

	/**
	 * String representation of the version
	 */
	private String value;
	
	/**
	 * Inner representation of the version
	 */
	private int[] innerView;
	
	/**
	 * No Arg constructor
	 */
	public Version(){
		super();
	}
	
	/**
	 * Default constructor
	 * @param version using format yy.xx.bb (ex: 1.5.0)
	 */
	public Version(String version){
		this.value = version;
		this.parseVersion();
	}
	
	/**
	 * Inner method used to build the inner version based
	 * on its string representation
	 */
	protected void parseVersion(){
		if(this.value != null){
			StringTokenizer tokenizer = new StringTokenizer(this.value,".",false);			
			this.innerView = new int[tokenizer.countTokens()];
			try{	
				int offset = 0;
				while(tokenizer.hasMoreElements()){					
					this.innerView[offset++] = Integer.parseInt(tokenizer.nextToken());
				}
			}catch(NumberFormatException nfe){
				this.innerView = null;
			}
		}
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Serialization
	 */
	public String toString(){
		return this.value;
	}
	
	/**
	 * Inner representation getter
	 * @return The inner representation in an array of int
	 */
	public int[] toIntArray(){
		return this.innerView;
	}
	
	public int compareTo(Version remoteVersion) {
		if(this.innerView != null && remoteVersion.toIntArray() != null){
			if(this.innerView.length >= remoteVersion.toIntArray().length){
				for(int offset = 0; offset < remoteVersion.toIntArray().length; offset++){
					if(this.innerView[offset] < remoteVersion.toIntArray()[offset]) return -1;
					else if(this.innerView[offset] > remoteVersion.toIntArray()[offset]) return 1;
				}
				for(int offset = remoteVersion.toIntArray().length; offset < this.innerView.length; offset++){
					if(this.innerView[offset] > 0) return 1;
				}
			}
			else{
				for(int offset = 0; offset < this.innerView.length; offset++){
					if(this.innerView[offset] < remoteVersion.toIntArray()[offset]) return -1;
					else if(this.innerView[offset] > remoteVersion.toIntArray()[offset]) return 1;
				}
				for(int offset = this.innerView.length; offset < remoteVersion.toIntArray().length; offset++){
					if(remoteVersion.toIntArray()[offset] > 0) return -1;
				}
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Version)	return this.compareTo((Version)obj) == 0;
		else return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;
		if(this.innerView != null){
			int pow=1;
			for(int digit : this.innerView){
				hashCode+=digit*pow;
				pow*=10;
			}
		}
		return hashCode;
	}

}
