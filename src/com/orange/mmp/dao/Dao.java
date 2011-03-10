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
package com.orange.mmp.dao;

/**
 * Generic DAO interface definition
 * 
 * @author Thomas MILLET
 *
 */
public interface Dao<T>{

	/**
     * Indicates the timestamp of the last data update
     * @return The date of the last update in a long
     * @throws MMPDaoException
     */
    public long getLastUpdateTimestamp() throws MMPDaoException;
    
	/**
	 * Create a new entry of <T>
	 * 
	 * @param entry A <T> instance to update or create
	 * @return The persisted <T> instance
	 * @throws MMPDaoException
	 */
	public T createOrUdpdate(T entry) throws MMPDaoException;

	/**
	 * Delete <T> persisted instance
	 * 
	 * @param entry The <T> instance to delete 
	 * @throws MMPDaoException
	 */
	public void delete(T entry) throws MMPDaoException;

	/**
	 * Find <T> entries based on <T> template, never returns null but an empty array
	 * 
	 * @param entrie The <T> instance template
	 * @return An array of matching <T> entries
	 * @throws MMPDaoException
	 */
	public T[] find(T entry) throws MMPDaoException;

	/**
	 * List all available <T> entries, never returns null but an empty array
	 * 
	 * @return An array of available <T> entries
	 * @throws MMPDaoException
	 */
	public T[] list() throws MMPDaoException;
}
