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

package org.efaps.ui.wicket.components.form.valuepicker;

import java.util.Iterator;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class ValuePickerPanel extends Panel {

  /**
   *
   */
  private static final long serialVersionUID = 1L;


  private final Value4Picker valueComponent;


  private final SearchField searchField;

  /**
   * @param _wicketId
   * @param _model
   * @param value4Picker
   */
  public ValuePickerPanel(final String _wicketId, final IModel<?> _model,
                          final Value4Picker _value4Picker) {
    super(_wicketId, _model);

    this.valueComponent = _value4Picker;
    final UIFormCell uiObject = (UIFormCell) getDefaultModelObject();

    add(new Label("title", uiObject.getPicker().getTitle()));


    final NoSubmitForm form = new NoSubmitForm("form");
    add(form);
    form.add(new Label("label",
                       DBProperties.getProperty("default.Button.Search")));


    this.searchField = new SearchField("search", form, _model);
    form.add(this.searchField);

    final SubmitCloseBehavior submitclose = new SubmitCloseBehavior(form);
    this.add(submitclose);
    final WebMarkupContainer updater = new WebMarkupContainer("update");
    updater.setOutputMarkupId(true);
    form.add(updater);
    try {
      uiObject.getPicker().execute(null);
      final RepeatingView repeater = new RepeatingView("repeater");
      repeater.add(new Item(repeater.newChildId(),
                            uiObject.getPicker().getHeadings(),
                            submitclose));
      updater.add(repeater);
      for (final Object obj :  uiObject.getPicker().getValueList()) {
        final String[] arr = (String[]) obj;
        repeater.add(new Item(repeater.newChildId(), arr , null));
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Getter method for instance variable {@link #searchField}.
   *
   * @return value of instance variable {@link #searchField}
   */
  public SearchField getSearchField() {
    return this.searchField;
  }

  public class Item extends WebComponent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final SubmitCloseBehavior submitCloseBehavior;
    private final String[] values;


    /**
     * @param newChildId
     * @param obj
     * @param submitCloseBehavior2
     */
    public Item(final String wicketId, final String[] _strArr,
        final SubmitCloseBehavior _submitCloseBehavior) {
      super(wicketId);
      this.values = _strArr;
      this.submitCloseBehavior = _submitCloseBehavior;
    }

    /**
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     *      org.apache.wicket.markup.ComponentTag)
     * @param _markupStream
     * @param _tag
     */
    @Override
    protected void onComponentTagBody(final MarkupStream _markupStream,
        final ComponentTag _tag) {
      super.onComponentTagBody(_markupStream, _tag);

      final StringBuilder valueStr = new StringBuilder();

      if (this.submitCloseBehavior != null) {
        valueStr.append("<script type=\"text/javascript\">")
          .append("  function submitClose() {")
          .append(this.submitCloseBehavior.getEventHandler())
          .append("}")
          .append("</script>");
      }
      final String columnTag = "".equals(this.values[0]) ? "th" : "td";
      valueStr.append("<").append(columnTag).append(">");
      if (!"".equals(this.values[0])) {
        valueStr.append("<input type=\"radio\" ")
          .append("value=\"").append(this.values[0]).append("\" ")
          .append("name=\"picker\" onClick=\"submitClose()\" />");
      }
      valueStr.append("</").append(columnTag).append("><").append(columnTag).append(">")
        .append("<span>").append(this.values[1]).append("</span>")
        .append("</").append(columnTag).append(">");

      for (int i = 2; i < this.values.length; i++) {
        valueStr.append("<").append(columnTag).append(">")
          .append("<span>").append(this.values[i]).append("</span>")
          .append("</").append(columnTag).append(">");
      }

        replaceComponentTagBody(_markupStream, _tag, valueStr);

    }
  }

  public class SubmitCloseBehavior extends AjaxFormSubmitBehavior {

    /**
     * @param form
     * @param event
     */
    public SubmitCloseBehavior(final Form<?> form) {
      super(form, "onClick");
    }

    /**
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target
     */
    @Override
    protected void onError(final AjaxRequestTarget _target) {
    }

    /**
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target
     */
    @Override
    protected void onSubmit(final AjaxRequestTarget _target) {
      try {
        final String key = Context.getThreadContext().getParameter("picker");
        ValuePickerPanel.this.valueComponent.setValue(key,
           ((UIFormCell) getDefaultModelObject()).getPicker().getValue(key));
      } catch (final EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      ModalWindowContainer modal;
      if (getPage() instanceof MainPage) {
        modal = ((MainPage) getPage()).getModal();
      } else {
        modal =
            ((AbstractContentPage) getPage()).getModal();
      }
      modal.close(_target);
      _target.addComponent(ValuePickerPanel.this.valueComponent);

    }

    /**
     * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getEventHandler()
     * @return
     */
    @Override
    protected CharSequence getEventHandler() {
      return super.getEventHandler();
    }

    /**
     * @see org.apache.wicket.ajax.AjaxEventBehavior#onComponentTag(org.apache.wicket.markup.ComponentTag)
     * @param tag
     */
    @Override
    protected void onComponentTag(final ComponentTag tag) {
     //nothing must be done
    }

    /**
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#getPreconditionScript()
     * @return
     */
    @Override
    protected CharSequence getPreconditionScript() {
      // must be overwritten
      return null;
    }
  }
  public class SearchField extends WebComponent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId
     */
    public SearchField(final String _wicketId, final Form<?> form, final IModel <?> _model) {
      super(_wicketId);
      this.add(new SearchBehavior(form));
    }

    /**
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     * @param tag
     */
    @Override
    protected void onComponentTag(final ComponentTag tag) {
      // TODO Auto-generated method stub
      super.onComponentTag(tag);
      tag.put("autocomplete", "off");
      tag.put("name", "pickerSearch");
    }

  }
  public class SearchBehavior extends AjaxFormSubmitBehavior {

    /**
     * @param form
     * @param event
     */
    public SearchBehavior(final Form<?> form) {
      super(form, "onkeyup");
    }

    /**
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param ajaxrequesttarget
     */
    @Override
    protected void onError(final AjaxRequestTarget ajaxrequesttarget) {
    }

    /**
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target
     */
    @Override
    protected void onSubmit(final AjaxRequestTarget _target) {
      final UIFormCell uiObject = (UIFormCell) getDefaultModelObject();
      try {
        final String search
                      = Context.getThreadContext().getParameter("pickerSearch");

        uiObject.getPicker().execute(search);
        final MarkupContainer panel = getComponent().getParent();
        final Iterator<?> iter = panel.iterator();
        MarkupContainer parent = null;
        while (iter.hasNext()) {
          final Object comp = iter.next();
          if (comp instanceof WebMarkupContainer) {
            parent = (MarkupContainer) comp;
          }
        }

        _target.addComponent(parent);
        final RepeatingView repeater = new RepeatingView("repeater");
        parent.addOrReplace(repeater);
        repeater.add(new Item(repeater.newChildId(),
                              uiObject.getPicker().getHeadings(),
                              null));

        for (final Object obj :  uiObject.getPicker().getValueList()) {
          final String[] arr = (String[]) obj;
          repeater.add(new Item(repeater.newChildId(), arr , null));
        }
      } catch (final EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public class NoSubmitForm extends Form<Object> {

    private static final long serialVersionUID = 1L;

   /**
     * @param _wicketId
     */
    public NoSubmitForm(final String _wicketId) {
      super(_wicketId);
    }

    /**
     * @see org.apache.wicket.markup.html.form.Form#onComponentTag(org.apache.wicket.markup.ComponentTag)
     * @param _tag
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag) {
      super.onComponentTag(_tag);
      _tag.put("action", "");
      _tag.put("method", "");
      _tag.put("onsubmit", "return false;");
    }
  }
}
