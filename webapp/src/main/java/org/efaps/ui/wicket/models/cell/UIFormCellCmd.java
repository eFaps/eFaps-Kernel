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

package org.efaps.ui.wicket.models.cell;

import java.util.List;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.field.FieldCommand;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class UIFormCellCmd extends UIFormCell {

  /**
   * Enum is used to set for this UIFormCellCmd which status of execution
   * it is in.
   */
  public enum ExecutionStatus {
    /** Method evaluateRenderedContent is executed. */
    RENDER,

    EXECUTE;
  }

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final boolean renderButton;
  /**
   * Stores the actual execution status.
   */
  private ExecutionStatus executionStatus;

  private final boolean append;

  private final String targetField;

  /**
   * @param targetMode
   * @param field
   * @throws EFapsException
   */
  public UIFormCellCmd(final AbstractUIObject _parent,
                       final FieldCommand _field,
                       final Instance _instance,
                       final String _label)
      throws EFapsException {
    super(_parent, new FieldValue(_field, null, null, null), _instance, null, null,
          _label, null);
    this.renderButton = _field.isRenderButton();
    this.append = _field.isAppend();
    this.targetField = _field.getTargetField();
  }



  public List<Return> executeEvents(final Object _others)
      throws EFapsException {
    if (this.executionStatus == null) {
      this.executionStatus = ExecutionStatus.EXECUTE;
    }
    final List<Return> ret = executeEvents(_others, EventType.UI_FIELD_CMD);

    if (this.executionStatus == ExecutionStatus.EXECUTE) {
      this.executionStatus = null;
    }
    return ret;
  }


  /**
   * Getter method for instance variable {@link #renderButton}.
   *
   * @return value of instance variable {@link #renderButton}
   */
  public boolean isRenderButton() {
    return this.renderButton;
  }

  /**
   * Getter method for instance variable {@link #append}.
   *
   * @return value of instance variable {@link #append}
   */
  public boolean isAppend() {
    return this.append;
  }



  /**
   * @param _script
   * @throws EFapsException
   *
   */
  public String getRenderedContent(final String _script) throws EFapsException {
    this.executionStatus = ExecutionStatus.RENDER;
    final StringBuilder snip = new StringBuilder();
    final List<Return> returns = executeEvents(_script);
    for (final Return oneReturn : returns) {
      if (oneReturn.contains(ReturnValues.SNIPLETT)) {
        snip.append(oneReturn.get(ReturnValues.SNIPLETT));
      }
    }
    this.executionStatus = null;
    return snip.toString();
  }


  /**
   * Getter method for instance variable {@link #executionStatus}.
   *
   * @return value of instance variable {@link #executionStatus}
   */
  public ExecutionStatus getExecutionStatus() {
    return this.executionStatus;
  }

  public FieldCommand getFieldCommand() {
    return (FieldCommand) getField();
  }


  public boolean isTargetField() {
    return this.targetField != null;
  }

  /**
   * Getter method for instance variable {@link #targetField}.
   *
   * @return value of instance variable {@link #targetField}
   */
  public String getTargetField() {
    return this.targetField;
  }
}
