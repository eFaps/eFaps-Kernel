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

package org.efaps.earchive.svn;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.efaps.db.Context;
import org.efaps.jaas.ServerLoginHandler;
import org.efaps.util.EFapsException;

/**
 * Extends the ServerLoginHandler from the kernel, to start and stop the
 * Context for eFaps.
 *
 * @author Jan Moxter
 * @version $Id$
 */
public class LoginHandler extends ServerLoginHandler {

  /**
   * @see org.efaps.jaas.ServerLoginHandler#handle(javax.security.auth.callback.Callback[])
   * @param _callbacks
   * @throws IOException
   * @throws UnsupportedCallbackException
   */
  @Override
  public void handle(final Callback[] _callbacks) throws IOException,
      UnsupportedCallbackException {
    try {
      if (!Context.isTMActive()) {
        Context.begin();
      }

      super.handle(_callbacks);

      if (!Context.isTMNoTransaction()) {
        if (Context.isTMActive()) {
          Context.commit();
        } else {
          Context.rollback();
        }
      }
    } catch (final SecurityException e) {
      //TODO logger?
      e.printStackTrace();
    } catch (final IllegalStateException e) {
      //TODO logger?
      e.printStackTrace();
    } catch (final EFapsException e) {
      //TODO logger?
      e.printStackTrace();
    }
  }
}
