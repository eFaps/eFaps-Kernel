/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.esjp.common.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 * @todo description
 */
public class PwdChgExecute implements EventExecution {

  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(PwdChgExecute.class);

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException {

    Context context = Context.getThreadContext();
    String passwordold = context.getParameter("passwordold");
    String passwordnew = context.getParameter("passwordnew");
    String passwordnew2 = context.getParameter("passwordnew2");

    if (context.getPerson().checkPassword(passwordold)
        && passwordnew.equals(passwordnew2)) {
      try {
        context.getPerson().setPassword(passwordnew);
      } catch (Exception e) {
        LOG.error("execute(Parameter)", e);
      }
    } else {
      throw new EFapsException(this.getClass(), "wrongPassword", new Object());
    }

    return null;
  }
}
