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

package org.efaps.admin.event;


/**
 * This enum provides the key-value Relation for the TriggerEvent.<br>
 * Therfore using a trigger can be made by using the key. An example for the use
 * of this enum is the definition of a Trigger inside the XML-Defenition.
 * 
 * @author tmo
 * @author jmo
 * @version $Id$
 * 
 */
public enum EventType  {

  ACCESSCHECK        ("Admin_Event_AccessCheck"),
  CHECKIN_OVERRIDE   ("Admin_Event_Trigger_CheckinOverride"),
  CHECKIN_POST       ("Admin_Event_Trigger_CheckinPost"),
  CHECKIN_PRE        ("Admin_Event_Trigger_CheckinPre"),
  CHECKOUT_OVERRIDE  ("Admin_Event_Trigger_CheckoutOverride"),
  CHECKOUT_POST      ("Admin_Event_Trigger_CheckoutPost"),
  CHECKOUT_PRE       ("Admin_Event_Trigger_CheckoutPre"),
  COMMAND            ("Admin_Event_Trigger_Command"),
  DELETE_OVERRIDE    ("Admin_Event_Trigger_DeleteOverride"),
  DELETE_POST        ("Admin_Event_Trigger_DeletePost"),
  DELETE_PRE         ("Admin_Event_Trigger_DeletePre"),
  INSERT_OVERRIDE    ("Admin_Event_Trigger_InsertOverride"),
  INSERT_POST        ("Admin_Event_Trigger_InsertPost"),
  INSERT_PRE         ("Admin_Event_Trigger_InsertPre"),
  RANGE_VALUE        ("Admin_Event_RangeValue"),
  UPDATE_OVERRIDE    ("Admin_Event_Trigger_UpdateOverride"),
  UPDATE_POST        ("Admin_Event_Trigger_UpdatePost"),
  UPDATE_PRE         ("Admin_Event_Trigger_UpdatePre"),
  UI_TABLE_EVALUATE  ("Admin_UI_TableEvaluateEvent");
  
  
  public final String name;

  private EventType(final String _name)  {
    this.name = _name;
  }
}
