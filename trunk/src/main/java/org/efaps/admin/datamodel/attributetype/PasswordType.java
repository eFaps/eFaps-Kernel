/*
 * Copyright 2003 - 2010 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.admin.datamodel.attributetype;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.query.CachedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Passwords a stored in a two way encryption in the eFaps-Database. That means
 * the password is encrypted before storing it in the eFaps-Database, so that it
 * can not be read. After retrieving it from the database the password is
 * decrypted and is available in clear text. This is necessary because e.g. the
 * CRAM-MD5 authorization needs the password in clear text.
 *
 * @author The eFaps Team
 *
 * @version $Id$
 */
public class PasswordType
    extends StringType
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PasswordType.class);

    /**
     * The localized string and the internal string value are equal. So the
     * internal value can be set directly with method {@link #setValue}.
     *
     * @param _values new value to set
     * @return String
     */
    @Override
    protected String eval(final Object... _values)
    {
        return decryptEncrypt(super.eval(_values), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
    {
        String ret = _rs.getString(_indexes.get(0).intValue());
        if (ret != null) {
            ret = ret.trim();
        }
        ret = decryptEncrypt(ret, true);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
    {
        Object ret = null;
        if (_objectList.size() < 1) {
            ret = null;
        } else if (_objectList.size() > 1) {
            final List<String> list = new ArrayList<String>();
            for (final Object object : _objectList) {
                list.add((object == null) ? "" : decryptEncrypt(object.toString().trim(), true));
            }
            ret = list;
        } else {
            final Object object = _objectList.get(0);
            ret = (object == null) ? "" : decryptEncrypt(object.toString().trim(), true);
        }
        return ret;
    }

    /**
     * Returns encrypted/decrypted by salt password. Uses SHA-1 Message Digest
     * Algorithm as defined in NIST's FIPS 180-1. The output of this algorithm
     * is a 160-bit digest.
     *
     * @param _password     password to encrypt/decrypt
     * @param _decrypt      <i>true</i> to decrypt or <i>false</i> to encrypt
     * @return decrypted / encrypted by salt password
     */
    private String decryptEncrypt(final String _password,
                                  final boolean _decrypt)
    {
        String ret = null;
        if (_password != null) {
            try {
                final char[] password = "password".toCharArray();
                // Salt
                final byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x34,
                                      (byte) 0xE3, (byte) 0x03 };
                // Iteration count
                final int count = 19;

                // Create PBE parameter set
                final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
                final PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, count);
                // TODO make this part configurable
                final SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
                final SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

                final Cipher cipher = Cipher.getInstance(pbeKey.getAlgorithm());

                if (_decrypt) {
                    cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
                    // Decode base64 to get bytes
                    final byte[] dec = Base64.decodeBase64(_password);
                    // Decrypt
                    final byte[] ciphertext = cipher.doFinal(dec);

                    ret = new String(ciphertext, "UTF8");

                } else {
                    cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
                    final byte[] pwdText = _password.getBytes("UTF8");
                    // Encrypt the cleartext
                    final byte[] ciphertext = cipher.doFinal(pwdText);

                    ret = Base64.encodeBase64String(ciphertext);
                }
                // TODO rework errorhandling
            } catch (final NoSuchAlgorithmException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            } catch (final NoSuchPaddingException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            } catch (final InvalidKeySpecException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            } catch (final InvalidKeyException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            } catch (final InvalidAlgorithmParameterException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            } catch (final IllegalBlockSizeException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            } catch (final BadPaddingException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            } catch (final UnsupportedEncodingException e) {
                PasswordType.LOG.error("Encryption/Decryption failed!", e);
            }
        }
        return ret;
    }
}
