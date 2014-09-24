/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * This enumeration provides the key-value Relation for the TriggerEvent.<br>
 * Therefore using a trigger can be made by using the key. An example for the
 * use of this enumeration is the definition of a Trigger inside the XML
 * definition.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public enum EventType
{
    /** EventType for checking the access to a type. */
    ACCESSCHECK          ("Admin_DataModel_TypeAccessCheckEvent"),
    /** EventType executed instead the checkin of a type. */
    CHECKIN_OVERRIDE     ("Admin_DataModel_Type_Trigger_CheckinOverride"),
    /** EventType executed after the checkin of a type. */
    CHECKIN_POST         ("Admin_DataModel_Type_Trigger_CheckinPost"),
    /** EventType executed before the checkin of a type. */
    CHECKIN_PRE          ("Admin_DataModel_Type_Trigger_CheckinPre"),
    /** EventType executed instead the checkout of a type. */
    CHECKOUT_OVERRIDE    ("Admin_DataModel_Type_Trigger_CheckoutOverride"),
    /** EventType executed after the checkout of a type. */
    CHECKOUT_POST        ("Admin_DataModel_Type_Trigger_CheckoutPost"),
    /** EventType executed before the checkout of a type. */
    CHECKOUT_PRE         ("Admin_DataModel_Type_Trigger_CheckoutPre"),
    /** EventType executed instead the deletion of a type. */
    DELETE_OVERRIDE      ("Admin_DataModel_Type_Trigger_DeleteOverride"),
    /** EventType executed after the deletion of a type. */
    DELETE_POST          ("Admin_DataModel_Type_Trigger_DeletePost"),
    /** EventType executed before the deletion of a type. */
    DELETE_PRE           ("Admin_DataModel_Type_Trigger_DeletePre"),
    /** EventType executed instead the insert of a type. */
    INSERT_OVERRIDE      ("Admin_DataModel_Type_Trigger_InsertOverride"),
    /** EventType executed after the insert of a type. */
    INSERT_POST          ("Admin_DataModel_Type_Trigger_InsertPost"),
    /** EventType executed before the insert of a type. */
    INSERT_PRE           ("Admin_DataModel_Type_Trigger_InsertPre"),
    /** EventType executed instead the update of a type. */
    UPDATE_OVERRIDE      ("Admin_DataModel_Type_Trigger_UpdateOverride"),
    /** EventType executed after the update of a type. */
    UPDATE_POST          ("Admin_DataModel_Type_Trigger_UpdatePost"),
    /** EventType executed before the update of a type. */
    UPDATE_PRE           ("Admin_DataModel_Type_Trigger_UpdatePre"),
    /**
     * EventType executed immediately after reading the value from
     * the database to be possible to alter the value.
     */
    READ_VALUE           ("Admin_DataModel_AttributeReadEvent"),
    /**
     *  EventType executed just before the prepare update the value
     *  into the database to be possible to alter the value.
     */
    UPDATE_VALUE         ("Admin_DataModel_AttributeUpdateEvent"),
    /**
     *  EventType executed just before prepare insert of the value
     *  into the database to be possible to alter the value.
     */
    INSERT_VALUE         ("Admin_DataModel_AttributeInsertEvent"),
    /**
     * EventType to get the BigDecimal value for a rate.
     */
    RATE_VALUE           ("Admin_DataModel_AttributeRateEvent"),
    /**
     * Event Ttpe to get the values as a map for a range link.
     */
    RANGE_VALUE          ("Admin_DataModel_AttributeRangeEvent"),
    /**
     * EventType to get a formated Value for a Field. It is executed after
     * retrieving the value. The value is passed to the ESJP for formatting it.
     */
    UI_FIELD_FORMAT      ("Admin_UI_FieldFormatEvent"),
    /**
     * EventType for a field that updates other fields. The event will be
     * executed, if the value for the field is changed.
     */
    UI_FIELD_UPDATE      ("Admin_UI_FieldUpdateEvent"),
    /**
     * EventType for generating the value for a field instead of retrieving it
     * directly from the database.
     */
    UI_FIELD_VALUE        ("Admin_UI_FieldValueEvent"),
    /**
     * EventType for getting the instance behind a field.
     */
    UI_FIELD_ALTINST      ("Admin_UI_FieldAlternateInstanceEvent"),
    /** EventType for a FieldCommand. */
    UI_FIELD_CMD          ("Admin_UI_FieldCommandEvent"),
    /** EventType for the search ESJP of a auto complete field. */
    UI_FIELD_AUTOCOMPLETE ("Admin_UI_FieldAutoCompleteEvent"),
    /** EventType for the picker event of a field. */
    UI_PICKER             ("Admin_UI_PickerEvent"),
    /**
     * EventType for checking the access to a field (depending on mode: create
     * edit etc.
     */
    UI_ACCESSCHECK        ("Admin_UI_AbstractAccessCheckEvent"),
    /**
     * EventType executed from an command.
     */
    UI_COMMAND_EXECUTE    ("Admin_UI_CommandExecuteEvent"),
    /**
     * EventType executed to validate a form.
     */
    UI_VALIDATE           ("Admin_UI_ValidateEvent"),
    /**
     * EventType for checking the access to a field (depending on mode: create
     * edit etc.
     */
    UI_INSTANCEMANAGER    ("Admin_UI_InstanceManagerEvent"),
    /** EventType for evaluating the values for a table. */
    UI_TABLE_EVALUATE     ("Admin_UI_TableEvaluateEvent"),
    /**
     * EventType used to validate the values for an attribute.
     */
    VALIDATE              ("Admin_DataModel_AttributeValidateEvent");

    /**
     * Name of the Event.
     */
    private final String name;

    /**
     * @param _name name for the event
     */
    private EventType(final String _name)
    {
        this.name = _name;
    }

    /**
     * Getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }
}
