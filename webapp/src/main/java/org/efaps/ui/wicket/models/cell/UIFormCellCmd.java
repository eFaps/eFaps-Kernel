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

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.FieldCommand;
import org.efaps.db.Context;
import org.efaps.db.Instance;
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
  private final long fieldCmdId;
  private final boolean renderButton;
  /**
   * Stores the actual execution status.
   */
  private ExecutionStatus executionStatus;

  /**
   * @param targetMode
   * @param field
   * @throws EFapsException
   */
  public UIFormCellCmd(final FieldCommand _field,
                       final String _oid,
                       final TargetMode _targetmode,
                       final String _label)
      throws EFapsException {
    super(new FieldValue(_field, null, null, null),
          _oid,
          null,
          null,
          _targetmode,
          _label,
          null);
    this.fieldCmdId = _field.getId();
    this.renderButton = _field.isRenderButton();
  }


  public List<Return> executeEvents(final Object _others) throws EFapsException {
    if (this.executionStatus == null) {
      this.executionStatus = ExecutionStatus.EXECUTE;
    }
    List<Return> ret = new ArrayList<Return>();
    final FieldCommand fieldCmd = getFieldCommand();
    if (fieldCmd.hasEvents(EventType.UI_FIELD_CMD)) {
        final Context context = Context.getThreadContext();
        final String[] contextoid = { getOid() };
        context.getParameters().put("oid", contextoid);
        ret = fieldCmd.executeEvents(EventType.UI_FIELD_CMD,
                            ParameterValues.INSTANCE, new Instance(getOid()),
                            ParameterValues.OTHERS, _others,
                            ParameterValues.PARAMETERS, context.getParameters(),
                            ParameterValues.CLASS, this);
    }
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
    return FieldCommand.get(this.fieldCmdId);
  }
}
