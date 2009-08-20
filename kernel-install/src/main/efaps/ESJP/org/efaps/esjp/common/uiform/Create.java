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

package org.efaps.esjp.common.uiform;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * This esjp is used from the UI_COMMAND_EXECUTE from the Form on Create.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d74132b7-caf1-4f83-866d-8bc83bb26cdf")
@EFapsRevision("$Rev$")
public class Create implements EventExecution
{

    /**
     * Execute the esjp.
     *
     * @see org.efaps.admin.event.EventExecution#execute(org.efaps.admin.event.Parameter)
     * @param _parameter Parameter as defined for an esjp
     * @return new empty Return
     * @throws EFapsException on error
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {

        // create the basic object
        final Instance instance = basicInsert(_parameter);
        // connect the basic object to a middle object
        connect(_parameter, instance);
        // check if we have a fileupload field
        fileUpload(_parameter, instance);
        // create classifications
        insertClassification(_parameter, instance);

        return new Return();
    }

    /**
     * Method that insert the basic object.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @return Instance on the insert
     * @throws EFapsException on error
     */
    protected Instance basicInsert(final Parameter _parameter) throws EFapsException
    {
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final Instance parent = _parameter.getInstance();
        final List<FieldSet> fieldsets = new ArrayList<FieldSet>();

        final Insert insert = new Insert(command.getTargetCreateType());
        for (final Field field : command.getTargetForm().getFields()) {
            final String attrName = field.getExpression() == null
                                        ? field.getAttribute()
                                        : field.getExpression();
            if (attrName != null
                          && (field.isEditableDisplay(TargetMode.CREATE) || field.isHiddenDisplay(TargetMode.CREATE))) {
                if (field instanceof FieldSet) {
                    fieldsets.add((FieldSet) field);
                } else {

                    final Attribute attr = command.getTargetCreateType().getAttribute(attrName);
                    // check if not a fileupload field
                    if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                        if (_parameter.getParameters().containsKey(field.getName())) {
                            final String value = _parameter.getParameterValue(field.getName());
                            if (attr.hasUoM()) {
                                final String uom = _parameter.getParameterValue(field.getName() + "UoM");
                                insert.add(attr, new Object[] { value, uom });
                            } else {
                                insert.add(attr, value);
                            }
                        }
                    }
                }
            }
        }
        if (command.getTargetConnectAttribute() != null) {
            insert.add(command.getTargetConnectAttribute(), "" + parent.getId());
        }
        insert.execute();

        final Instance instance = insert.getInstance();
        insertFieldSets(_parameter, instance, fieldsets);

        return instance;
    }

    /**
     * Method to create the related fieldsets if parameters are given for them.
     *
     * @param _parameter    Parameter as passed from the efaps API.
     * @param _instance     Instance of the new object
     * @param _fieldsets    fieldsets to insert
     * @throws EFapsException on error
     */
    protected void insertFieldSets(final Parameter _parameter, final Instance _instance,
                                   final List<FieldSet> _fieldsets)
        throws EFapsException
    {
        final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);
        // to find out if new values where added for a field set, first it is checked
        // if it exists in this map
        if (others != null) {

            final NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            nf.setMaximumIntegerDigits(2);

            for (final FieldSet fieldset : _fieldsets) {
                final String[] yCoords = (String[]) others.get(fieldset.getName() + "_eFapsNew");
                if (yCoords != null) {
                    final String setName = fieldset.getExpression() == null
                                                ? fieldset.getAttribute()
                                                : fieldset.getExpression();
                    final AttributeSet set = AttributeSet.find(_instance.getType().getName(), setName);

                    for (final String yCoord : yCoords) {
                        final Insert insert = new Insert(set);
                        insert.add(set.getAttribute(setName), ((Long) _instance.getId()).toString());
                        int xCoord = 0;
                        for (final String attrName : fieldset.getOrder()) {
                            final Attribute child = set.getAttribute(attrName);
                            final String fieldName = fieldset.getName() + "_eFapsNew_"
                                            + nf.format(Integer.parseInt(yCoord)) + nf.format(xCoord);
                            if (_parameter.getParameters().containsKey(fieldName)) {
                                if (child.hasUoM()) {
                                    insert.add(child, new Object[] { _parameter.getParameterValue(fieldName),
                                                    _parameter.getParameterValue(fieldName + "UoM") });
                                } else {
                                    insert.add(child, _parameter.getParameterValue(fieldName));
                                }
                            }
                            xCoord++;
                        }
                        insert.execute();
                    }
                }
            }
        }
    }


    /**
     * Method to connect the new instance to parent via middle object.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @throws EFapsException on error
     */
    protected void connect(final Parameter _parameter, final Instance _instance) throws EFapsException
    {
        final Instance parent = _parameter.getInstance();
        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);

        if (properties.containsKey("ConnectType")) {
            final String type = (String) properties.get("ConnectType");
            final String childAttr = (String) properties.get("ConnectChildAttribute");
            final String parentAttr = (String) properties.get("ConnectParentAttribute");

            final Insert insert = new Insert(type);
            insert.add(parentAttr, ((Long) parent.getId()).toString());
            insert.add(childAttr, ((Long) _instance.getId()).toString());
            insert.execute();
        }
    }

    /**
     * Method to upload the file.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @throws EFapsException on error
     */
    protected void fileUpload(final Parameter _parameter, final Instance _instance) throws EFapsException
    {
        final Context context = Context.getThreadContext();

        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);

        for (final Field field : command.getTargetForm().getFields()) {
            final String attrName = field.getExpression() == null
                                        ? field.getAttribute()
                                        : field.getExpression();
            if (attrName == null && field.isEditableDisplay(TargetMode.CREATE)) {
                final Context.FileParameter fileItem = context.getFileParameters().get(field.getName());
                if (fileItem != null) {
                    final Checkin checkin = new Checkin(_instance);
                    try {
                        checkin.execute(fileItem.getName(), fileItem.getInputStream(), (int) fileItem.getSize());
                    } catch (final IOException e) {
                        throw new EFapsException(this.getClass(), "execute", e, _parameter);
                    }
                }
            }
        }
    }

    /**
     * Method to insert the classifications.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @throws EFapsException on error
     */
    protected void insertClassification(final Parameter _parameter, final Instance _instance) throws EFapsException
    {
        if (_parameter.get(ParameterValues.CLASSIFICATIONS) != null) {

            final List<?> classifications = (List<?>) _parameter.get(ParameterValues.CLASSIFICATIONS);

            for (final Object object : classifications) {
                final Classification classification = (Classification) object;
                final List<FieldSet> fieldsets = new ArrayList<FieldSet>();

                final Insert relInsert = new Insert(classification.getClassifyRelationType());
                relInsert.add(classification.getRelLinkAttributeName(), ((Long) _instance.getId()).toString());
                relInsert.add(classification.getRelTypeAttributeName(), ((Long) classification.getId()).toString());
                relInsert.execute();

                final Form form = Form.getTypeForm(classification);
                final Insert classInsert = new Insert(classification);
                classInsert.add(classification.getLinkAttributeName(), ((Long) _instance.getId()).toString());
                for (final Field field : form.getFields()) {
                    final String attrName = field.getExpression() == null
                                                ? field.getAttribute()
                                                : field.getExpression();
                    if (attrName != null
                          && (field.isEditableDisplay(TargetMode.CREATE) || field.isHiddenDisplay(TargetMode.CREATE))) {
                        if (field instanceof FieldSet) {
                            fieldsets.add((FieldSet) field);
                        } else {

                            final Attribute attr = classification.getAttribute(attrName);
                            if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                                if (_parameter.getParameters().containsKey(field.getName())) {
                                    final String value = _parameter.getParameterValue(field.getName());
                                    if (attr.hasUoM()) {
                                        final String uom = _parameter.getParameterValue(field.getName() + "UoM");
                                        classInsert.add(attr, new Object[] { value, uom });
                                    } else {
                                        classInsert.add(attr, value);
                                    }
                                }
                            }
                        }
                    }
                }
                classInsert.execute();
                insertFieldSets(_parameter, classInsert.getInstance(), fieldsets);
            }
        }
    }
}
