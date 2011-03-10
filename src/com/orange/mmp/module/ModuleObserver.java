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


/**
 * Observer for modules life cycle in ModuleContainer<br/>
 * This interface must be implemented by components which must
 * be notified of module addition/removal on ModuleContainer.
 *  
 * @author Thomas MILLET
 *
 */
public interface ModuleObserver {
	
	/**
	 * Called when module events are triggered
	 * @param module The added module
	 */
	public void onModuleEvent(ModuleEvent moduleEvent);

}
