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

package org.efaps.db;

import java.util.List;

import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.util.EFapsException;

/**
 * Abstract class where all eFaps database actions are derived.
 *
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractAction {

  /**
   * Instance holding the oid of the object which is checked in/out.
   *
   * @see #getInstance()
   * @see #setInstance(Instance)
   */
  private Instance instance;

  /**
   * The method gets all events for the given EventType and executes them in the
   * given order. If no events are defined, nothing is done. The method return
   * TRUE if a event was found, otherwise FALSE.
   *
   * @param _eventtype  trigger events to execute
   * @return true if a trigger was found and executed, otherwise false
   * @throws EFapsException on error
   */
  protected boolean executeEvents(final EventType _eventtype)
      throws EFapsException {
    final List<EventDefinition> triggers =
        getInstance().getType().getEvents(_eventtype);
    if (triggers != null) {
      final Parameter parameter = new Parameter();
      parameter.put(ParameterValues.INSTANCE, getInstance());
      for (final EventDefinition evenDef : triggers) {
        evenDef.execute(parameter);
      }
      return true;
    }
    return false;
  }

  /**
   * this is the getter method for instance variable {@link #instance}.
   *
   * @return the Instance of the Checkin/Checkout
   * @see #setInstance(Instance)
   */
  protected Instance getInstance() {
    return this.instance;
  }

  /**
   * this is the setter method for instance variable {@link #instance}.
   *
   * @param _instance
   *          Instance to set
   * @see #getInstance()
   */
  protected void setInstance(final Instance _instance) {
    this.instance = _instance;
  }

}
