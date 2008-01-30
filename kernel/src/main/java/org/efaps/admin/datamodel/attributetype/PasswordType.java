/*
 * Copyright 2003-2008 The eFaps Team
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author grs
 * @author tmo
 * @version $Id$
 */
public class PasswordType extends StringType {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(PasswordType.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The localised string and the internal string value are equal. So the
   * internal value can be set directly with method {@link #setValue}.
   * 
   * @param _value
   *          new value to set
   */
  public void set(final Object _value) {
    if (_value instanceof String) {
      setValue(getEncryptPassword((String) _value));
    } else if (_value != null) {
      setValue(getEncryptPassword(_value.toString()));
    }
  }

  /**
   * Returns encrypted by salt password. Uses SHA-1 Message Digest Algorithm as
   * defined in NIST's FIPS 180-1. The output of this algorithm is a 160-bit
   * digest.
   * 
   * @param _password
   *          unencrypted password
   * @return encrypted by salt password
   * @exception EcfException
   *              if an error occurs
   */
  private String getEncryptPassword(final String _password) {
    String ret = null;

    byte[] encryptedPassword = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update("eFaps".getBytes());
      encryptedPassword = md.digest(_password.getBytes());

      StringBuffer convert = new StringBuffer();
      for (int i = 0; i < encryptedPassword.length; i++) {
        convert.append(Integer.toHexString(encryptedPassword[i]));
      }
      ret = convert.toString();

    } catch (NoSuchAlgorithmException e) {
      LOG.error("no encrypt password algorithm", e);
    }
    return ret;
  }
}
