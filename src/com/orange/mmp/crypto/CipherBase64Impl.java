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
package com.orange.mmp.crypto;

import org.apache.commons.codec.binary.Base64;

/**
 * Base64 Cipher implementation
 * 
 * @author rhwm3785
 *
 */
public class CipherBase64Impl implements Cipher {
	
    /**
     * The inner data to encode/decode
     */
    private byte[] content;

    /*
     * (non-Javadoc)
     * @see com.orange.mmp.crypto.Cipher#decode()
     */
	public byte[] decode() throws CipheringException {
		return Base64.decodeBase64(this.content);
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.crypto.Cipher#encode()
	 */
	public byte[] encode() throws CipheringException {
		return Base64.encodeBase64(this.content);
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.crypto.Cipher#init(java.lang.String, java.lang.String)
	 */
	public void init(byte[] content, String secretKey) {
		if(content != null) this.content = content;
	}

}
