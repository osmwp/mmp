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
package com.orange.mmp.api.ws.jsonrpc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jabsorb.localarg.LocalArgResolveException;
import org.jabsorb.localarg.LocalArgResolver;

import com.orange.mmp.context.UserContext;


/**
 * Local args resolver used to pass UserContext as parameter
 * of RPC method. The UserContext instance is build on the current user's session
*/
public class UserContextArgResolver implements LocalArgResolver
{
    /* (non-Javadoc)
     * @see org.jabsorb.localarg.LocalArgResolver#resolveArg(java.lang.Object)
     */
    public Object resolveArg(Object context) throws LocalArgResolveException{
    	UserContext userContext = null;

    	if (context instanceof HttpServletRequest){
    		HttpSession session = ((HttpServletRequest)context).getSession(false);
    		if(session != null){
    			userContext = (UserContext)session.getAttribute(UserContext.USER_CTX_STR_KEY);
    		}
    		if(userContext == null){
    			userContext = (UserContext)((HttpServletRequest)context).getAttribute(UserContext.USER_CTX_STR_KEY);
    		}
    	}

    	return userContext;
    }
}
