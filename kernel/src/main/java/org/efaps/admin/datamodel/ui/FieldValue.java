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

package org.efaps.admin.datamodel.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @todo description
 * @version $Id$
 */
public class FieldValue implements Comparable {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(FieldValue.class);

  // /////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the class to represent this form value.
   * 
   * @see #getClassUI
   */
  private final Attribute attribute;

  /**
   * The variable stores the instance for this value.
   * 
   * @see #getInstance
   */
  private final Instance instance;

  /**
   * The instance variable stores the field for this value.
   * 
   * @see #getFieldDef
   */
  private final FieldDefinition fieldDef;

  /**
   * The instance variable stores the value for this form value.
   * 
   * @see #getValue
   */
  private final Object value;

  // /////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   *
   */
  public FieldValue(final FieldDefinition _fieldDef, final Attribute _attr,
                    final Object _value, final Instance _instance) {
    this.fieldDef = _fieldDef;
    this.attribute = _attr;
    this.value = _value;
    this.instance = _instance;
  }

  // /////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
   */
  public String getCreateHtml() throws EFapsException {
    return getClassUI().getCreateHtml(this);
  }

  /**
   *
   */
  public String getViewHtml() throws EFapsException {
    return getClassUI().getViewHtml(this);
  }

  /**
   *
   */
  public String getEditHtml() throws EFapsException {
    return getClassUI().getEditHtml(this);
  }

  /**
   *
   */
  public String getSearchHtml() throws EFapsException {
    return getClassUI().getSearchHtml(this);
  }

  // /////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * This is the getter method for the instance variable {@link #classUI}.
   * 
   * @return value of instance variable {@link #classUI}
   * @see #classUI
   */
  public UIInterface getClassUI() {
    return this.attribute.getAttributeType().getUI();
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
   * This is the getter method for the field variable {@link #fieldDef}.
   * 
   * @return value of field variable {@link #fieldDef}
   * @see #field
   */
  public FieldDefinition getFieldDef() {
    return this.fieldDef;
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

  public Attribute getAttribute() {
    return this.attribute;
  }

  public int compareTo(Object _target) {
    FieldValue target = (FieldValue) _target;

    int ret = 0;
    if (this.value == null || target.getValue() == null) {
      if (this.value == null && target.getValue() != null) {
        ret = -1;
      }
      if (this.value != null && target.getValue() == null) {
        ret = 1;
      }
    } else {
      if (this.getClassUI().equals(target.getClassUI())) {
        ret = this.getClassUI().compare(this, target);
      } else {
        LOG.error("can't compare this Objects because "
            + "they don't have the same ClassUI");
      }
    }
    return ret;
  }
}
