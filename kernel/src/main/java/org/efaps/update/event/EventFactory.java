/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.update.event;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.xml.sax.Attributes;

/**
 * Factory for creating <code>org.efaps.update.event.Event</code>.<br>
 * There are two way to use the Factory:<br>
 * 1. e.g. < trigger name="TeamWork_MemberRights_DeleteOverride"
 * event="DELETE_OVERRIDE" program="org.efaps.esjp.teamwork.Member"
 * method="removeMember" index="1"/> with a call of the default Construtor<br>
 * 2. e.g. < evaluate program="org.efaps.esjp.common.uitable.QueryEvaluate"> with a
 * call of the Contructor setting the event<br>
 * If for the parameter method no value is given, it will be sett to the default
 * "execute"
 *
 * @author jmox
 * @version $Id$
 */
public class EventFactory implements ObjectCreationFactory {

  /**
   * stores the Name of the event
   */
  private String event = null;

  /**
   * default Constructor
   */
  public EventFactory() {

  }

  /**
   * Constructor setting the Event
   *
   * @param _event
   *          EventName to be set
   */
  public EventFactory(final String _event) {
    this.event = _event;
  }

  public Object createObject(final Attributes _attributes) {
    if (_attributes.getValue("event") != null) {
      this.event = _attributes.getValue("event");
    }

    String method = _attributes.getValue("method");

    if (method == null) {
      method = "execute";
    }

    final Event ret =
        new Event(_attributes.getValue("name"), this.event, _attributes
            .getValue("program"), method, _attributes.getValue("index"));
    ret.setTrigger(_attributes.getValue("event") != null);
    return ret;
  }

  public Digester getDigester() {
    return null;
  }

  public void setDigester(Digester digester) {
  }

}
