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

package org.efaps.admin.datamodel.ui;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * This class is used as the value for a field. It can be used to get the
 * for the user interface necessary strings etc.
 *
 * @author tmo
 * @author jmox
 *
 * @version $Id$
 */
public class FieldValue implements Comparable<Object> {

  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FieldValue.class);

  /**
   * Enum used to identify for what kind of html document this Fieldvalue is
   * used.
   *
   */
  public enum HtmlType {

    /**
     * Key used to identify a html document for create.
     */
    CREATEHTML,

    /**
     * Key used to identify a html document for view.
     */
    VIEWHTML,

    /**
     * Key used to identify a html document for edit.
     */
    EDITHTML,

    /**
     * Key used to identify a html document for search.
     */
    SEARCHHTML;
  }

  /**
   * Store the HTML type for which the events must be executed.
   *
   * @see #executeEvents
   * @see #getHtmlType
   */
  private HtmlType htmlType;

  /**
   * The instance variable stores the class to represent this form value.
   *
   * @see #getClassUI
   */
  private final Attribute attribute;

  /**
   * The variable stores the field instance for this value.
   *
   * @see #getInstance
   */
  private final Instance instance;

  /**
   * The instance variable stores the field for this value.
   *
   * @see #getFieldDef
   */
  private final Field field;

  /**
   * Stores the UIInterface belonging to this fieldvalue.
   */
  private final UIInterface ui;

  /**
   * The instance variable stores the value for this form value.
   *
   * @see #getValue
   */
  private Object value;

  /**
   * Construtor used to evaluate the value from the database by using one of the
   * getter methods for html.
   *
   * @param _field      field this value belongs to
   * @param _attr       attribute the value belongs to
   * @param _value      value of the FieldValue
   * @param _instance   Instance the Value belongs to
   */
  public FieldValue(final Field _field,
                    final Attribute _attr,
                    final Object _value,
                    final Instance _instance) {
    this.field = _field;
    this.attribute = _attr;
    this.value = _value;
    this.instance = _instance;
    this.ui = (_attr == null) ? null : _attr.getAttributeType().getUI();
  }

  /**
   * Constructor used in case of comparison.
   *
   * @param _field          field this value belongs to
   * @param _ui             UIInterface this value belongs to
   * @param _compareValue   value to be compared
   */
  public FieldValue(final Field _field,
                    final UIInterface _ui,
                    final Object _compareValue) {
    this.ui = _ui;
    this.field = _field;
    this.value = _compareValue;
    this.instance = null;
    this.attribute = null;
  }
  // /////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Executes the field value events for a field.
   *
   * @param _callInstance   instance for which this field is called (not the
   *                        same as the instance of the field...)
   * @param _instance       instance of the field (needed for tabel fields)
   * @param _htmlType       which HTML output must be done (create, edit,
   *                        view...)
   * @throws EFapsException on error
   * @return string from called field value events or <code>null</code> if no
   *         field value event is defined
   *
   */
  protected String executeEvents(final Instance _callInstance,
                                 final Instance _instance,
                                 final HtmlType _htmlType)
      throws EFapsException {
    String ret = null;
    this.htmlType = _htmlType;
    if ((this.field != null)
        && this.field.hasEvents(EventType.UI_FIELD_VALUE)) {

      final List<EventDefinition> events =
                              this.field.getEvents(EventType.UI_FIELD_VALUE);

      final StringBuilder html = new StringBuilder();
      if (events != null) {
        final Parameter parameter = new Parameter();
        parameter.put(ParameterValues.UIOBJECT, this);
        parameter.put(ParameterValues.CALL_INSTANCE, _callInstance);
        parameter.put(ParameterValues.INSTANCE, _instance);
        for (final EventDefinition evenDef : events) {
          final Return retu = evenDef.execute(parameter);
          if (retu.get(ReturnValues.SNIPLETT) != null) {
            html.append(retu.get(ReturnValues.SNIPLETT));
          } else if (retu.get(ReturnValues.VALUES) != null) {
            this.value = retu.get(ReturnValues.VALUES);
          }
        }
      }
      if (html.length() > 0)  {
        ret = html.toString();
      }
    }
    return ret;
  }

  /**
   * Method to get html code for this FieldValue in case of create.
   *
   * @see #executeEvents
   *
   * @param _callInstance   Instance that called the html code
   * @param _instance       instance that is part of the html code
   * @throws EFapsException on error
   * @return html code as a String
   */
  public String getCreateHtml(final Instance _callInstance,
                              final Instance _instance)
      throws EFapsException {
    String ret = null;

    ret = executeEvents(_callInstance, _instance, HtmlType.CREATEHTML);
    if (ret == null) {
      if (this.ui != null)  {
        ret = this.ui.getCreateHtml(this);
      }
    }
    return ret;
  }

  /**
   * Method to get html code for this FieldValue in case of edit.
   *
   * @see #executeEvents
   *
   * @param _callInstance   Instance that called the html code
   * @param _instance       instance that is part of the html code
   * @throws EFapsException on error
   * @return html code as a String
   */
  public String getEditHtml(final Instance _callInstance,
                            final Instance _instance)
      throws EFapsException {
    String ret = null;

    ret = executeEvents(_callInstance, _instance, HtmlType.EDITHTML);
    if (ret == null) {
      if (this.ui != null)  {
        ret = this.ui.getEditHtml(this);
      }
    }
    return ret;
  }

  /**
   * Method to get html code for this FieldValue in case of search.
   *
   * @see #executeEvents
   *
   * @param _callInstance   Instance that called the html code
   * @param _instance       instance that is part of the html code
   * @throws EFapsException on error
   * @return html code as a String
   */
  public String getSearchHtml(final Instance _callInstance,
                              final Instance _instance)
      throws EFapsException {
    String ret = null;

    ret = executeEvents(_callInstance, _instance, HtmlType.SEARCHHTML);
    if (ret == null) {
      if (this.ui != null)  {
        ret = this.ui.getSearchHtml(this);
      }
    }
    return ret;
  }

  /**
   * Method to get html code for this FieldValue in case of view.
   *
   * @see #executeEvents
   *
   * @param _callInstance   Instance that called the html code
   * @param _instance       instance that is part of the html code
   * @throws EFapsException on error
   * @return html code as a String
   */
  public String getViewHtml(final Instance _callInstance,
                            final Instance _instance)
        throws EFapsException {
    String ret = null;

    ret = executeEvents(_callInstance, _instance, HtmlType.VIEWHTML);
    if (ret == null) {
      if (this.ui != null)  {
        ret = this.ui.getViewHtml(this);
      }
    }
    return ret;
  }

  /**
   * Method is used to retrieve the value that mut be used for comparison.
   * @return  Object
   * @throws EFapsException on error
   */
  public Object getObject4Compare() throws EFapsException {
    Object ret = null;
    if (this.ui != null) {
      ret = this.ui.getObject4Compare(this);
    }
    return ret;
  }

  /**
   * The user interface implementing this attribute is returned. If no
   * attribute for this field is defined, a <code>null</code> is returned.
   *
   * @return class implementing the user interface for given attribute instance
   *         or <code>null</code> if not attribute is defined
   * @see #attribute
   */
  public UIInterface getClassUI() {
    return this.ui;
  }

  /**
   * Method to compare this FieldValue to a target FieldValue.
   *
   * @param _target field value to compare to
   * @return 0 if smaller, else -1
   */
  public int compareTo(final Object _target) {
    final FieldValue target = (FieldValue) _target;

    int ret = 0;
    if (this.value == null || target.getValue() == null) {
      if (this.value == null && target.getValue() != null) {
        ret = -1;
      }
      if (this.value != null && target.getValue() == null) {
        ret = 1;
      }
    } else {
      if (getClassUI().equals(target.getClassUI())) {
        ret = getClassUI().compare(this, target);
      } else {
        LOG.error("can't compare this Objects because "
            + "they don't have the same ClassUI");
      }
    }
    return ret;
  }

  /**
   * This is the getter method for the instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   * @see #instance
   */
  public Instance getInstance() {
    return this.instance;
  }


  /**
   * This is the getter method for the instance variable {@link #value}.
   *
   * @return value of instance variable {@link #value}
   * @see #value
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * This is the getter method for instance variable {@link #attribute}.
   *
   * @return value of instance variable {@link #attribute}
   * @see #attribute
   */
  public Attribute getAttribute() {
    return this.attribute;
  }

  /**
   * This is the getter method for instance variable {@link #htmlType}.
   *
   * @return value of instance variable {@link #htmlType}
   * @see #htmlType
   */
  public HtmlType getHtmlType() {
    return this.htmlType;
  }

  /**
   * This is the getter method for instance variable {@link #field}.
   *
   * @return value of instance variable {@link #field}
   */
  public Field getField() {
    return this.field;
  }
}
