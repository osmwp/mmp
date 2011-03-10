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
package com.orange.mmp.cache.ehcache;

import java.util.ArrayList;
import java.util.List;

import com.orange.mmp.cache.Cache;
import com.orange.mmp.cache.MMPCacheException;
import com.orange.mmp.core.data.Element;

/**
 * EHCache implementation of Cache
 * 
 * @author Thomas MILLET
 *
 */
public class EHCache implements Cache {

	/**
	 * Inner EHCache implementation
	 */
	private net.sf.ehcache.Cache ehCache;
	
	/**
	 * Build a new Cache from an EHCache 
	 */
	public EHCache(net.sf.ehcache.Cache ehCache) {
		this.ehCache = ehCache;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#clear()
	 */
	public void clear() throws MMPCacheException {
		this.ehCache.removeAll();
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#get(java.lang.Object)
	 */
	public Element get(Object key) throws MMPCacheException {
		net.sf.ehcache.Element element = this.ehCache.get(key);
		if(element != null) return new Element(element.getObjectKey(), element.getObjectValue());
		else return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#getElements()
	 */
	public List<Element> getElements() throws MMPCacheException {
		List<Element> elements = new ArrayList<Element>();
		for(Object key : this.ehCache.getKeys()){
			elements.add(new Element(key, this.ehCache.get(key).getObjectValue()));
		}
		return elements;
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#getKeys()
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getKeys() throws MMPCacheException {
		return this.ehCache.getKeys();
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#isKeyInCache(java.lang.Object)
	 */
	public boolean isKeyInCache(Object key) throws MMPCacheException {
		return (this.ehCache.get(key) != null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#isValueInCache(java.lang.Object)
	 */
	public boolean isValueInCache(Object value) throws MMPCacheException {
		return this.ehCache.isValueInCache(value);		
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#remove(java.lang.Object)
	 */
	public void remove(Object key) throws MMPCacheException {
		this.ehCache.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#set(com.orange.mmp.core.data.Element)
	 */
	public void set(Element element) throws MMPCacheException {
		this.ehCache.put(new net.sf.ehcache.Element(element.getKey(),element.getValue()));
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.cache.Cache#size()
	 */
	public int size() throws MMPCacheException {
		return this.ehCache.getSize();
	}

}
