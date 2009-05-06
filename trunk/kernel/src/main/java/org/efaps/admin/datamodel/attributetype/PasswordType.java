/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.query.CachedResult;

/**
 * Passwords a stored in a two way encryption in the eFaps-Database. That means
 * the password is encrypted before storing it in the eFaps-Database, so that it
 * can not be read. After retrieving it from the database the password is
 * decrypted and is available in clear text. This is necessary because e.g. the
 * CRAM-MD5 autorisation needs the password in clear text.
 *
 * @author jmox
 *
 * @version $Id$
 */
public class PasswordType extends StringType {

  /**
   * Logging instance used to give logging information of this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PasswordType.class);

  @Override
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException {
    _stmt.setString(_indexes.get(0), decryptEncrypt(getValue(), false));
  }

  /**
   * @todo test that only one value is given for indexes
   */
  @Override
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes) {
    String ret = _rs.getString(_indexes.get(0).intValue());
    if (ret != null) {
      ret = ret.trim();
    }
    ret = decryptEncrypt(ret, true);
    setValue(ret);
    return ret;
  }

  /**
   * The localised string and the internal string value are equal. So the
   * internal value can be set directly with method {@link #setValue}.
   *
   * @param _value
   *          new value to set
   */
  @Override
  public void set(final Object _value) {
    if (_value instanceof String) {
      setValue((String) _value);
    } else if (_value != null) {
      setValue(_value.toString());
    }
  }

  /**
   * Returns encrypted by salt password. Uses SHA-1 Message Digest Algorithm as
   * defined in NIST's FIPS 180-1. The output of this algorithm is a 160-bit
   * digest.
   *
   * @param _password unencrypted password
   * @return encrypted by salt password
   */
  private String decryptEncrypt(final String _password, final boolean _decrypt) {
     String ret = null;
    try {
      final char[] password = "password".toCharArray();
      // Salt
      final byte[] salt = {
          (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
          (byte) 0x56, (byte) 0x34, (byte) 0xE3, (byte) 0x03
      };
      // Iteration count
      final int count = 19;

      // Create PBE parameter set
      final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
      final PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, count);

      final SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      final SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

      final Cipher cipher = Cipher.getInstance(pbeKey.getAlgorithm());

      if (_decrypt) {
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        // Decode base64 to get bytes
        final byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(_password);
        // Decrypt
        final byte[] ciphertext = cipher.doFinal(dec);

        ret = new String(ciphertext, "UTF8");

      } else {
        cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
        final byte[] pwdText = _password.getBytes("UTF8");
        // Encrypt the cleartext
        final byte[] ciphertext = cipher.doFinal(pwdText);

        ret = new sun.misc.BASE64Encoder().encode(ciphertext);
      }

    } catch (final NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final NoSuchPaddingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvalidKeySpecException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvalidKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvalidAlgorithmParameterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalBlockSizeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final BadPaddingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return ret;
  }

}
