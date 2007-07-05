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

package org.efaps.webapp.tags;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.CommandAbstract;

public class EFapsModalDialog {

  private final CommandAbstract command;

  private final String script;

  public EFapsModalDialog(final CommandAbstract _command, final String _script) {
    this.command = _command;
    this.script = _script.replace("\"", "'");
  }

  public String getScript() {
    return this.script + ";dojo.widget.byId('" + this.getDialogId()
        + "').hide();";
  }

  public String getDialogVar() {
    return getDialogId();
  }

  public String getQuestion() {
    return DBProperties.getProperty(command.getName() + ".Question");
  }

  public String getDialogId() {
    String ret = "eFapsDialog" + ((Long) this.command.getId()).toString();
    return ret;
  }

  public String getCancelText() {
    String ret;
    if (DBProperties.hasProperty(command.getName() + ".Cancel")) {
      ret = DBProperties.getProperty(command.getName() + ".Cancel");
    } else {
      ret = DBProperties.getProperty("webapp.EFapsModalDialog.Cancel");
    }
    return ret;
  }

  public String getSubmitText() {
    String ret;
    if (DBProperties.hasProperty(command.getName() + ".Submit")) {
      ret = DBProperties.getProperty(command.getName() + ".Submit");
    } else {
      ret = DBProperties.getProperty("webapp.EFapsModalDialog.Submit");
    }
    return ret;
  }

}
