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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 128 implementation of Cipher
 * @author milletth
 *
 */
public class CipherAESImpl implements com.orange.mmp.crypto.Cipher {
	
    /**
     * Static initialization vector for AES encryption
     */
    private static final IvParameterSpec IV = new IvParameterSpec("0000000000000000".getBytes());
    
    /**
     * The inner data to encode/decode
     */
    private byte[] content;

    /**
     * The inner scret key used to encode/decode
     */
    private SecretKey secretKey;

    /*
     * (non-Javadoc)
     * @see com.orange.mmp.crypto.Cipher#decode()
     */
	public byte[] decode() throws CipheringException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
			cipher.init(Cipher.DECRYPT_MODE,this.secretKey,IV);
			return cipher.doFinal(this.content);
		} catch(NoSuchAlgorithmException nsae){
        	throw new CipheringException(nsae.getMessage(),nsae);
        } catch(NoSuchPaddingException nspe){
        	throw new CipheringException(nspe.getMessage(),nspe);
        } catch(NoSuchProviderException nspre){
        	throw new CipheringException(nspre.getMessage(),nspre);
        } catch(BadPaddingException bpe){
        	throw new CipheringException(bpe.getMessage(),bpe);
        } catch(IllegalBlockSizeException ibse){
        	throw new CipheringException(ibse.getMessage(),ibse);
    	} catch(InvalidKeyException ike){
    		throw new CipheringException(ike.getMessage(),ike);
    	} catch(InvalidAlgorithmParameterException iape){
    		throw new CipheringException(iape.getMessage(),iape);
    	} catch(IllegalArgumentException iae){
    		throw new CipheringException(iae.getMessage(),iae);
    	}
	}

	/*
	 * (non-Javadoc)
	 * @see com.orange.mmp.crypto.Cipher#encode()
	 */
	public byte[] encode() throws CipheringException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
			cipher.init(Cipher.ENCRYPT_MODE,this.secretKey,IV);
			return cipher.doFinal(this.content);
		} catch(NoSuchAlgorithmException nsae){
    		throw new CipheringException(nsae.getMessage(),nsae);
        } catch(NoSuchPaddingException nspe){
        	throw new CipheringException(nspe.getMessage(),nspe);
        } catch(NoSuchProviderException nspre){
        	throw new CipheringException(nspre.getMessage(),nspre);
        } catch(BadPaddingException bpe){
        	throw new CipheringException(bpe.getMessage(),bpe);
        } catch(IllegalBlockSizeException ibse){
        	throw new CipheringException(ibse.getMessage(),ibse);
    	} catch(InvalidKeyException ike){
    		throw new CipheringException(ike.getMessage(),ike);
    	} catch(InvalidAlgorithmParameterException iape){
    		throw new CipheringException(iape.getMessage(),iape);
    	} catch(IllegalArgumentException iae){
    		throw new CipheringException(iae.getMessage(),iae);
    	}
	}

	public void init(byte[] content, String secretKey) {
		if(content != null) this.content = content;
    	if(secretKey != null) this.secretKey = new SecretKeySpec(secretKey.getBytes(), "AES");
	}

}
