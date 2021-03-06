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
package com.orange.mmp.module;

import com.orange.mmp.core.data.Module;

/**
 * Class handling module events
 * 
 * @author Thomas MILLET
 *
 */
public class ModuleEvent {
	
	/**
	 * Event type for module addition
	 */
	public static final int MODULE_ADDED = 0x01;
	
	/**
	 * Event type for module start
	 */
	public static final int MODULE_STARTED = 0x02;
	
	/**
	 * Event type for module stop
	 */
	public static final int MODULE_STOPPED = 0x04;
	
	/**
	 * Event type for module update
	 */
	public static final int MODULE_UPDATED = 0x08;
	
	/**
	 * Event type for module removal
	 */
	public static final int MODULE_REMOVED = 0x10;
		
	/**
	 * The event type
	 */
	private int type;

	/**
	 * The target module
	 */
	private Module module;
	
	/**
	 * Default constructor	
	 * @param type The module event type
	 * @param module The module event target
	 */
	public ModuleEvent(int type, Module module) {
		super();
		this.type = type;
		this.module = module;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * @param module the module to set
	 */
	public void setModule(Module module) {
		this.module = module;
	}	
}
