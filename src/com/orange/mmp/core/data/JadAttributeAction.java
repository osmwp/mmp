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

/**
 * JAD settings and actions of a Mobile
 * 
 * @author Audrey KEDINGER
 *
 */
public final class JadAttributeAction {
	
	/**
	 * ADD Action type
	 */
	private static final String ADD_ACTION = "add";
	
	/**
	 * MODIFY Action type
	 */
	private static final String MODIFY_ACTION = "modify";
	
	/**
	 * DELETE Action type
	 */
	private static final String DELETE_ACTION = "delete";
	
	/**
	 * Indicates to apply action when signed 
	 */
	private static final String APPLY_CASE_SIGNED = "signed";
	
	/**
	 * Indicates to apply action when unsigned 
	 */
	private static final String APPLY_CASE_UNSIGNED = "unsigned";
	
	/**
	 * Indicates to always apply action
	 */
	private static final String APPLY_CASE_ALWAYS = "always";
	
	/**
	 * Indicates to never apply action 
	 */
	private static final String APPLY_CASE_NEVER = "never";
	
	/**
	 * Enum Type for Jad ACTION
	 */
	public enum Action {
		ADD(ADD_ACTION),
		MODIFY(MODIFY_ACTION),
		DELETE(DELETE_ACTION);
		
		/**
		 * The String form of the Action
		 */
		private String action;
		
		/**
		 * Default constructor
		 */
		private Action(String action) {
			this.action = action;
		}
		
		/**
		 * String form accessor
		 */
		public String action() {
			return this.action;
		}
		
		/**
		 * Serialized version of the Action
		 */
		public String toString() {
			return this.action;
		}		
	}
	
	/**
	 *	Enum type to indicate when the Action must be applied 
	 */
	public enum ApplyCase {
		SIGNED(APPLY_CASE_SIGNED),
		UNSIGNED(APPLY_CASE_UNSIGNED),
		ALWAYS(APPLY_CASE_ALWAYS),
		NEVER(APPLY_CASE_NEVER);
		
		/**
		 * The String form of the applyCase
		 */
		private String applyCase;
		
		/**
		 * Default constructor
		 */
		private ApplyCase(String applyCase) {
			this.applyCase = applyCase;
		}
		
		/**
		 *	String form accessor 
		 */
		public String applyCase() {
			return this.applyCase;
		}
		
		/**
		 * Serialized version of the ApplyCase
		 */
		public String toString() {
			return this.applyCase;
		}
	}
	
	/**
	 * Jad Attribute Action type
	 */
	private Action action;
	
	/**
	 * Jad Attribute name
	 */
	private String attribute;
	
	/**
	 * Jad Attribute value
	 */
	private String value;
	
	/**
	 * Jad Attribute strict type
	 */
	private boolean strict;
	
	/**
	 * Indicates to apply JadAttribuute to JAD
	 */
	private ApplyCase inJad;
	
	/**
	 * Indicates to apply JadAttribute to MANIFEST
	 */
	private ApplyCase inManifest;
	
	/**
	 *  Tools method used to get an Action from its String form
	 */
	public static Action actionFromString(String action){
		if(MODIFY_ACTION.equals(action)) return Action.MODIFY;
		else if(DELETE_ACTION.equals(action)) return Action.DELETE;
		else return Action.ADD;
	}
	
	/**
	 *  Tools method used to get an ApplyCase from its String form
	 */
	public static ApplyCase applyCaseFromString(String applyCase){
		if(APPLY_CASE_SIGNED.equals(applyCase)) return ApplyCase.SIGNED;
		else if(APPLY_CASE_UNSIGNED.equals(applyCase)) return ApplyCase.UNSIGNED;
		else if(APPLY_CASE_ALWAYS.equals(applyCase)) return ApplyCase.ALWAYS;
		else return ApplyCase.NEVER;
	}
	
	
	public boolean isStrict() {
		return this.strict;
	}
	
	public ApplyCase getInJad() {
		return this.inJad;
	}
	
	public ApplyCase getInManifest() {
		return this.inManifest;
	}
	
	public Action getAction() {
		return this.action;
	}
	
	public boolean isAddAction() {
		return this.action == Action.ADD;
	}
	
	public boolean isModifyAction() {
		return this.action == Action.MODIFY;
	}
	
	public boolean isDeleteAction() {
		return this.action == Action.DELETE;
	}
	
	public String getAttribute() {
		return this.attribute;
	}
	
	public String getValue() {
		return this.value;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * @param attribute the attribute to set
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @param strict the strict to set
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	/**
	 * @param inJad the inJad to set
	 */
	public void setInJad(ApplyCase inJad) {
		this.inJad = inJad;
	}

	/**
	 * @param inManifest the inManifest to set
	 */
	public void setInManifest(ApplyCase inManifest) {
		this.inManifest = inManifest;
	}
	
}