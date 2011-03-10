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
package com.orange.mmp.dao.flf;

import java.util.HashMap;

import com.orange.mmp.core.ApplicationListener;
import com.orange.mmp.core.MMPRuntimeException;
import com.orange.mmp.dao.Dao;
import com.orange.mmp.dao.DaoManager;
import com.orange.mmp.dao.MMPDaoException;
import com.orange.mmp.core.MMPException;

/**
 * Spring DAO Support class for file acces based DAO
 * @author milletth
 *
 */
public class FlfDaoManager implements DaoManager, ApplicationListener {

	/**
	 * Contains list of DAO handled by this Manager
	 */
	@SuppressWarnings("unchecked")
	private HashMap<Object, FlfDao> daoMap;
	
	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#initialize()
	 */
	@SuppressWarnings("unchecked")
	public void initialize() throws MMPException {
		if(this.daoMap != null){
			for(FlfDao flfDao : this.daoMap.values()){
				try{
					flfDao.checkDaoConfig();
				}catch(MMPDaoException me){
					throw new MMPRuntimeException("Failed to initialize DAO Manager", me);
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.core.ApplicationListener#shutdown()
	 */
	public void shutdown() throws MMPException {
		//NOP		
	}

	/* (non-Javadoc)
	 * @see com.orange.mmp.dao.DaoManager#getDao(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Dao getDao(Object daoKey) throws MMPDaoException {
		return this.daoMap.get(daoKey);
	}

	/**
	 * @param daoMap the daoMap to set
	 */
	@SuppressWarnings("unchecked")
	public void setDaoMap(HashMap<Object, FlfDao> daoMap) {
		this.daoMap = daoMap;
	}
}
