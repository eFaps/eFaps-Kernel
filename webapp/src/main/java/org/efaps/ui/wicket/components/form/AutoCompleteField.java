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

package org.efaps.ui.wicket.components.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.form.command.AjaxCmdBehavior;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class AutoCompleteField extends AutoCompleteTextField<String> {

  /**
   * Reference to the stylesheet.
   */
  public static final EFapsContentReference CSS
                            = new EFapsContentReference(AutoCompleteField.class,
                                                       "AutoCompleteField.css");

  /** Needed for serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * Model of this Component.
   */
  private final IModel<?> model;

  /**
   * Behavior that will be executed onchange, if this AutoCompleteField is used
   * in CommandCellPanel.
   */
  private AjaxCmdBehavior cmdBehavior;

  /**
   * The name of this field.
   */
  private final String fieldName;

  private Map<String, String> valuesMap;

  /**
   * @param _wicketId  wicket if for this component
   * @param _model     model for this component
   */
  public AutoCompleteField(final String _wicketId, final IModel<?> _model) {
    super(_wicketId);
    super.setModel(new Model<String>(""));
    this.model = _model;
    this.fieldName = ((UITableCell) _model.getObject()).getName();
    add(StaticHeaderContributor.forCss(CSS));

    add(new AjaxFormSubmitBehavior("onKeyDown") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(final AjaxRequestTarget _target) {
        try {
          String fieldvalue = Context.getThreadContext().getParameter(
              AutoCompleteField.this.fieldName);
          if (AutoCompleteField.this.valuesMap != null) {
            for (final Entry<String, String> entry
                              : AutoCompleteField.this.valuesMap.entrySet()) {
              if (entry.getValue().equals(fieldvalue)) {
                fieldvalue = entry.getKey();
                break;
              }
            }
            AutoCompleteField.this.setModel(new Model<String>(fieldvalue));
            _target.addComponent(AutoCompleteField.this);
          }
          if (AutoCompleteField.this.cmdBehavior != null) {
            AutoCompleteField.this.cmdBehavior.onSubmit4AutoComplete(_target,
                                                                    fieldvalue);
          }
        } catch (final EFapsException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      @Override
      protected void onError(final AjaxRequestTarget _target) {
      }
    });
  }

  /**
   * The Name must be set too use the name from eFaps.
   *
   * @param _tag tag to modify
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    _tag.setName("input");
    super.onComponentTag(_tag);
    _tag.put("name", this.fieldName);
  }


  @SuppressWarnings("unchecked")
  @Override
  protected Iterator<String> getChoices(final String _input) {
    final UIFormCell uiObject = (UIFormCell) this.model.getObject();
    final List<String> retList = new ArrayList<String>();
    try {
      final List<Return> returns = uiObject.getAutoCompletion(_input);
      for (final Return aReturn : returns) {
        final Object ob = aReturn.get(ReturnValues.VALUES);
        if (ob instanceof List) {
          final List<String> list = (List<String>) ob;
          retList.addAll(list);
        } else if (ob instanceof Map) {
          retList.addAll(((Map) ob).values());
          this.valuesMap = (Map) ob;
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return retList.iterator();
  }


  /**
   * Add a AjaxCmdBehavior to this .
   *
   * @param _cmdBehavior behavior to add
   */
  public void addCmdBehavior(final AjaxCmdBehavior _cmdBehavior) {
    this.cmdBehavior = _cmdBehavior;
  }
}
