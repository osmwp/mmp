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
package com.orange.mmp.i18n;

import java.util.Locale;
import java.util.Map;

/**
 * I18N manager interface
 * 
 * @author tml
 *
 */
public interface InternationalizationManager{

	/**
	 * Get a localized message based on its code from the default MessageSource
	 * 
	 * @param code The code in i18n ressources
	 * @param args The arguments if needed
	 * @param defaultMessage The default message if lookup failed
	 * @param locale The locale to use
	 * 
	 * @return A localized message
	 */
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) throws MMPI18NException;
	
	/**
	 * Get a localized message based on its code and its name from modules
	 * 
	 * @param messageSourceName The message source name
	 * @param code The code in i18n ressources
	 * @param args The arguments if needed
	 * @param defaultMessage The default message if lookup failed
	 * @param locale The locale to use
	 * 
	 * @return A localized message
	 */
	public String getMessage(String messageSourceName, String code, Object[] args, String defaultMessage, Locale locale) throws MMPI18NException;
	
	/**
	 * Get the localization map of a message source and a language.
	 * @param messageSource Message source
	 * @param locale Locale
	 * @return Map of localization data
	 */
	public Map<?,?> getLocalizationMap(final String messageSource, final Locale locale);
}
