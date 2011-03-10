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
package com.orange.mmp.core.loader;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;


/**
 * BCEL implementation used to generate interfaces at runtime
 * 
 * Classes are defined using 3 fields :
 * 	- the full qualified class name
 *  - the list of methods based on their String representation
 * 
 * @author tml
 *
 */
public class InterfaceGenerator {

	/**
	 * Regex pattern used to parse string representation of methods
	 */
	Pattern methodPattern = Pattern.compile("^.* ([^ ]*) .*\\.([^(]*)[(]([^)]*)[)].*$");
		
	/**
	 * The full qualified name of the class to generate
	 */
	private String className;
		
	/**
	 * List of methods of class to generated based on their String representation
	 */
	private String[] methods;
	
	/**
	 * Resulting generated Class bytecode
	 */
	private byte[] byteCode;
			
	/**
	 * Get the generated class bytecode 
	 * 
	 * @return The generated class bytecode
	 * @throws DynamicClassException
	 */
	public byte[] getByteCode() throws DynamicClassException{
		if(this.byteCode == null){
			this.generateByteCode();
		}
		return this.byteCode;
	}
	
	/**
	 * Inner method used to generate bytecode 
	 */
	protected void generateByteCode() throws DynamicClassException{
		ClassGen classGen = new ClassGen(this.className,"java.lang.Object","<generated>", Constants.ACC_PUBLIC | Constants.ACC_INTERFACE, null);
		//Add Methods
		if(this.methods != null){
			for(String methodString : this.methods){
				classGen.addMethod(this.getBcelMethod(methodString, classGen));
			}
		}
		this.byteCode = classGen.getJavaClass().getBytes();
	}

	
	/**
	 *	Instanciate a BCEL Method from its string representation
	 * 
	 * @param methodString The method string representation
	 * @param classGen The ClassGen template used for this method
	 * @return The Bcel Method
	 */
	protected Method getBcelMethod(String methodString, ClassGen classGen) throws DynamicClassException{
		//Apply regex on method string
		Matcher matcher = methodPattern.matcher(methodString); 
		
		if(matcher.matches() && matcher.groupCount() == 3){
			//Return type
			Type returnType = this.getBcelType(matcher.group(1));
			//Method name
			String methodName = matcher.group(2);
			//Parameters
			StringTokenizer paramTokenizer = new StringTokenizer(matcher.group(3),",");
			Type[] paramTypes =  new Type[paramTokenizer.countTokens()];
			String[] paramNames = new String[paramTypes.length];
			int paramIndex = 0;
			while(paramTokenizer.hasMoreTokens()){
				paramTypes[paramIndex] = this.getBcelType(paramTokenizer.nextToken());
				paramNames[paramIndex] = "arg"+paramIndex;
				paramIndex++;
			}
			MethodGen methodGen = new MethodGen(Constants.ACC_PUBLIC | Constants.ACC_ABSTRACT,
												returnType,
												paramTypes,
												paramNames,
												methodName,
												classGen.getClassName(),
												new InstructionList(),
												classGen.getConstantPool());
			
			return methodGen.getMethod();
		}
		else throw new DynamicClassException("failed to parse method");
	}
	
	/**
	 * Get the BCEL type for a class name
	 * 
	 * @param className The full qualifed classname
	 * @return A BCEL Type
	 */
	protected Type getBcelType(String className){
		return Type.getType(Utility.getSignature(className));
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return the methods
	 */
	public String[] getMethods() {
		return methods;
	}

	/**
	 * @param methods the methods to set
	 */
	public void setMethods(String[] methods) {
		this.methods = methods;
	}
}
