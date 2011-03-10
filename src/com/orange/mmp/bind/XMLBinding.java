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
package com.orange.mmp.bind;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.orange.mmp.core.Constants;

/**
 * Base class for XML Binding IN and OUT
 * 
 * @author tml
 *
 */
public class XMLBinding {

	/**
	 * Default constructor
	 */
	public XMLBinding(){
		
	}	
	
	/**
	 * Convert an XML Input Stream in a Java Object based based on a Binding object
	 * 
	 * @param xmlInputStream The input stream containing the XML data
	 * @param bindingClass The binding Class used to unserialize (should be a POJO for default unserializer or JAXB annoted Class for specific needs) 
	 * 
	 * @return A Java Object based on bindingClass filled with xmlInputStream data  
	 * 
	 * @throws BindingException
	 */
	@SuppressWarnings("unchecked")
	public Object read(InputStream xmlInputStream, Class bindingClass) throws BindingException{
		JAXBContext jaxbContext = null;
		if(bindingClass == null) throw new BindingException("Binding Class should not be null");
		try{
			jaxbContext = JAXBContext.newInstance(bindingClass);
			Unmarshaller u = jaxbContext.createUnmarshaller();
			return u.unmarshal(xmlInputStream);
		}catch(JAXBException je){
			throw new BindingException(je);
		}
	}
	
	/**
	 * Convert an XML Input Stream in a Java Object based based on a JAXB binding package
	 * 
	 * @param xmlInputStream The input stream containing the XML data
	 * @param bindingPackage The binding package indicating JAXB mappings 
	 * @param bindingClassLoader The binding classloader for String package mode to indicate ClassLoader to use (null for System ClassLoader)
	 * 
	 * @return A Java Object based on bindingPackage filled with xmlInputStream data  
	 * 
	 * @throws BindingException
	 */
	public Object read(InputStream xmlInputStream, String bindingPackage, ClassLoader bindingClassLoader) throws BindingException {
		JAXBContext jaxbContext = null;
		if(bindingPackage == null) throw new BindingException("Binding Package should not be null");
		try{
			if(bindingClassLoader != null) jaxbContext = JAXBContext.newInstance(bindingPackage,bindingClassLoader);
			else jaxbContext = JAXBContext.newInstance(bindingPackage);
			Unmarshaller u = jaxbContext.createUnmarshaller();
			return u.unmarshal(xmlInputStream);
		}catch(JAXBException je){
			throw new BindingException(je);
		}
	}
	
	/**
	 * Write an XML stream from a Java Object based a binding class
	 * 
	 * @param xmlObject The source Object to serialize
	 * @param xmlOutputStream The output stream in which data will be written
	 * @param bindingClass The binding Class used to unserialize (should be a POJO for default unserializer or JAXB annoted Class for specific needs)<br/>
	 * Can also be null to used directly the binding object Class 
	 * @param rootElement The XML root element to use, null for bindingClass name
	 * 
	 * @throws BindingException
	 */
	@SuppressWarnings("unchecked")
	public void write(Object xmlObject, OutputStream xmlOutputStream, Class bindingClass, String rootElement) throws BindingException{
		JAXBContext jaxbContext = null;
		try{
			if(bindingClass == null) bindingClass = xmlObject.getClass();
			jaxbContext = JAXBContext.newInstance(bindingClass);
			if(rootElement == null) rootElement = bindingClass.getSimpleName();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, Constants.DEFAULT_ENCODING);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			marshaller.marshal(new JAXBElement(new QName(rootElement),bindingClass,xmlObject), xmlOutputStream);
		}catch(JAXBException je){
			throw new BindingException(je);
		}
	}
	
	/**
	 * Write an XML stream from a Java Object based on a JAXB binding package
	 * 
	 * @param xmlObject The source Object to serialize
	 * @param xmlOutputStream The output stream in which data will be written
	 * @param bindingPackage The binding package indicating JAXB mappings 
	 * @param bindingClassLoader The binding classloader for String package mode to indicate ClassLoader to use (null for System ClassLoader)
	 * 
	 * @throws BindingException
	 */
	public void write(Object xmlObject, OutputStream xmlOutputStream, String bindingPackage, ClassLoader bindingClassLoader) throws BindingException{
		JAXBContext jaxbContext = null;
		if(bindingPackage == null) throw new BindingException("Binding Package should not be null");
		try{
			if(bindingClassLoader != null) jaxbContext = JAXBContext.newInstance(bindingPackage,bindingClassLoader);
			else jaxbContext = JAXBContext.newInstance(bindingPackage);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, Constants.DEFAULT_ENCODING);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			marshaller.marshal(xmlObject,xmlOutputStream);
		}catch(JAXBException je){
			throw new BindingException(je);
		}
	}
	
}
