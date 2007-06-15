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

package org.efaps.update.event;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.xml.sax.Attributes;

/**
 * Factory for creating <code>Trigger</code>
 * 
 * @author jmo
 * @version $Id$
 * 
 */
public class EventFactory implements ObjectCreationFactory {
  private String event = null;

  public EventFactory() {

  }

  public EventFactory(final String _event) {
    this.event = _event;
  }

  public Object createObject(final Attributes _attributes) {
    if (_attributes.getValue("event") != null) {
      this.event = _attributes.getValue("event");
    }

    Event ret = new Event(_attributes.getValue("name"), this.event, _attributes
        .getValue("program"), _attributes.getValue("method"), _attributes
        .getValue("index"));
    ret.setTrigger(_attributes.getValue("event") != null);
    return ret;
  }

  public Digester getDigester() {
    return null;
  }

  public void setDigester(Digester digester) {
  }

}
