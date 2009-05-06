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

package org.efaps.admin.event;


/**
 * This enum provides the key-value Relation for the TriggerEvent.<br>
 * Therfore using a trigger can be made by using the key. An example for the use
 * of this enum is the definition of a Trigger inside the XML-Defenition.
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 *
 */
public enum EventType  {
  /** EventType for checking the access to a type. */
  ACCESSCHECK        ("Admin_DataModel_TypeAccessCheckEvent"),
  /** EventType executed instead the checkin of a type. */
  CHECKIN_OVERRIDE   ("Admin_DataModel_Type_Trigger_CheckinOverride"),
  CHECKIN_POST       ("Admin_DataModel_Type_Trigger_CheckinPost"),
  CHECKIN_PRE        ("Admin_DataModel_Type_Trigger_CheckinPre"),
  /** EventType executed instead the checkout of a type. */
  CHECKOUT_OVERRIDE  ("Admin_DataModel_Type_Trigger_CheckoutOverride"),
  CHECKOUT_POST      ("Admin_DataModel_Type_Trigger_CheckoutPost"),
  CHECKOUT_PRE       ("Admin_DataModel_Type_Trigger_CheckoutPre"),
  /** EventType executed instead the deletion of a type. */
  DELETE_OVERRIDE    ("Admin_DataModel_Type_Trigger_DeleteOverride"),
  DELETE_POST        ("Admin_DataModel_Type_Trigger_DeletePost"),
  DELETE_PRE         ("Admin_DataModel_Type_Trigger_DeletePre"),
  /** EventType executed instead the insert of a type. */
  INSERT_OVERRIDE    ("Admin_DataModel_Type_Trigger_InsertOverride"),
  INSERT_POST        ("Admin_DataModel_Type_Trigger_InsertPost"),
  INSERT_PRE         ("Admin_DataModel_Type_Trigger_InsertPre"),
  /** EventType executed instead the update of a type. */
  UPDATE_OVERRIDE    ("Admin_DataModel_Type_Trigger_UpdateOverride"),
  UPDATE_POST        ("Admin_DataModel_Type_Trigger_UpdatePost"),
  UPDATE_PRE         ("Admin_DataModel_Type_Trigger_UpdatePre"),
  RANGE_VALUE        ("Admin_DataModel_AttributeRangeEvent"),
  /**
   * EventType for generating the value for a field instead of retrieving it
   * directly from the database.
   */
  UI_FIELD_VALUE ("Admin_UI_FieldValueEvent"),
  /** EventType for a FieldCommand. */
  UI_FIELD_CMD ("Admin_UI_FieldCommandEvent"),
  /** EventType for the search esjp of a auto complete field. */
  UI_FIELD_AUTOCOMPLETE ("Admin_UI_FieldAutoCompleteEvent"),
  /** EventType for the search esjp of a auto complete field. */
  UI_PICKER("Admin_UI_PickerEvent"),
  /**
   * EventType for checking the access to a field (depending on mode: create
   * edit etc.
   */
  UI_ACCESSCHECK        ("Admin_UI_AbstractAccessCheckEvent"),
  UI_COMMAND_EXECUTE    ("Admin_UI_CommandExecuteEvent"),
  UI_VALIDATE           ("Admin_UI_ValidateEvent"),
  UI_INSTANCEMANAGER    ("Admin_UI_InstanceManagerEvent"),
  /** EventType for evaluating the values for a table. */
  UI_TABLE_EVALUATE ("Admin_UI_TableEvaluateEvent"),
  VALIDATE              ("Admin_DataModel_AttributeValidateEvent");


  public final String name;

  private EventType(final String _name)  {
    this.name = _name;
  }
}
