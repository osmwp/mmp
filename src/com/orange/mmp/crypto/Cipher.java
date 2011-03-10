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

/**
 * Generic crypto interface use to encode/decode
 * secured data.
 * @author milletth
 */
public interface Cipher {

    /**
     * Initializes the context
     * @param content The string to decode/encode
     * @param secretKey The scret key used to cypher data (can be null)
     */
    public void init(byte[] content, String secretKey);

    /**
     * Encode the content and return it
     * @return The ciphered content
     * @throws CipheringException when an error occurs in encoding
     */
    public byte[] encode() throws CipheringException;

    /**
     * Decode the content and return it
     * @return The deciphered content
     * @throws CipheringException when an error occurs in decoding
     */
    public byte[] decode() throws CipheringException;
}
