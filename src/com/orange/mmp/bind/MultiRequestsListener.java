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

/**
 * Interface used by asynchronous REST clients to add response using key.
 * This interface is also used to check if listener has all expected responses.
 * @author milletth
 *
 */
public interface MultiRequestsListener {

    /**
     * Called when a new response is available for this listener
     * @param reponseName The key value of the response
     * @param responseObject The response object (XML binding)
     */
    public void onResponse(String reponseName, Object responseObject);

    /**
     * Indicates if listener has received all awaited responses
     * @return true when all responses are received, false otherwise
     */
    public boolean isComplete();
}
