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
 * Revision:        $Rev:1563 $
 * Last Changed:    $Date:2007-10-28 15:07:41 +0100 (So, 28 Okt 2007) $
 * Last Changed By: $Author:tmo $
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
import org.efaps.admin.datamodel.Type;
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
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d9ba2b85-8b9a-46b0-929e-8e938e7d5577")
@EFapsRevision("$Rev$")
public class Edit implements EventExecution
{
    /**
     * @param _parameter Parameter as provided by eFaps for a esjp
     * @throws EFapsException on error
     * @return empty Return
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {

        final Instance instance = _parameter.getInstance();
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);

        final Context context = Context.getThreadContext();
        // update the Values for the general form
        final String classifcationName = updateMainElements(_parameter, command.getTargetForm(), instance);

        // ************************************
        // check if we have a fileupload field
        if (context.getFileParameters().size() > 0) {
            for (final Field field : command.getTargetForm().getFields()) {
                final String attrName = field.getExpression() == null
                                            ? field.getAttribute()
                                            : field.getExpression();
                if (attrName == null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Context.FileParameter fileItem = context.getFileParameters().get(field.getName());
                    if (fileItem != null) {
                        final Checkin checkin = new Checkin(instance);
                        try {
                            checkin.execute(fileItem.getName(), fileItem.getInputStream(), (int) fileItem.getSize());
                        } catch (final IOException e) {
                            throw new EFapsException(this.getClass(), "execute", e, _parameter);
                        }
                    }
                }
            }
        }

        if (classifcationName != null) {
            updateClassifcation(_parameter, instance, classifcationName);
        }
        return new Return();
    }

    /**
     * Method updates the main elements from the form.
     *
     * @param _parameter    _parameter Parameter as provided by eFaps for a esjp
     * @param _form         from used for the update
     * @param _instance     instance that must be updated
     * @return  the name of a classification if found in the form, else null
     * @throws EFapsException on error
     */
    protected String updateMainElements(final Parameter _parameter, final Form _form, final Instance _instance)
        throws EFapsException
    {
        String ret = null;
        final List<FieldSet> fieldsets = new ArrayList<FieldSet>();
        final List<Field> fields = new ArrayList<Field>();

        final SearchQuery query = new SearchQuery();
        query.setObject(_instance);
        for (final Field field : _form.getFields()) {
            if (field instanceof FieldSet) {
                fieldsets.add((FieldSet) field);
            } else if (field instanceof FieldClassification) {
                ret = ((FieldClassification) field).getClassificationName();
            } else {
                final String attrName = field.getExpression() == null
                                            ? field.getAttribute()
                                            : field.getExpression();
                if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Attribute attr = _instance.getType().getAttribute(attrName);
                    // check if not a fileupload
                    if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                        query.addSelect(attrName);
                        fields.add(field);
                    }
                }
            }
        }
        query.execute();
        final Context context = Context.getThreadContext();
        if (query.next()) {
            final Update update = new Update(_instance);
            for (final Field field : fields) {
                if (context.getParameters().containsKey(field.getName())) {
                    final String newValue = context.getParameter(field.getName());
                    final String attrName = field.getExpression() == null
                                                ? field.getAttribute()
                                                : field.getExpression();
                    final Object object = query.get(attrName);
                    final String oldValue = object != null ? object.toString() : null;
                    if (!newValue.equals(oldValue)) {
                        final Attribute attr = _instance.getType().getAttribute(attrName);
                        if (attr.hasUoM()) {
                            update.add(attr, new Object[] { context.getParameter(field.getName()),
                                            context.getParameter(field.getName() + "UoM") });
                        } else {
                            update.add(attr, newValue);
                        }
                    }
                }
            }
            update.execute();
        }
        updateFieldSets(_parameter, _instance, fieldsets);
        return ret;
    }

    /**
     * Method to update the related fieldsets if parameters are given for them.
     *
     * @param _parameter    Parameter as passed from the efaps API.
     * @param _instance     Instance of the new object
     * @param _fieldsets    fieldsets to insert
     * @throws EFapsException on error
     */
    protected void updateFieldSets(final Parameter _parameter, final Instance _instance,
                                   final List<FieldSet> _fieldsets)
        throws EFapsException
    {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);

        for (final FieldSet fieldset : _fieldsets) {
            final String setName = fieldset.getExpression() == null
                                        ? fieldset.getAttribute()
                                        : fieldset.getExpression();
            final AttributeSet set = AttributeSet.find(_instance.getType().getName(), setName);

            // first already existing values must be updated, if they were altered
            boolean updateExisting = true;
            int yCoord = 0;
            while (updateExisting) {
                //check in the context if already existing values might have been altered, by
                //using the hidden field that is added when existing values for a fieldset are shown
                final String idfield = "hiddenId_" + fieldset.getName() + "_" + nf.format(yCoord);
                if (_parameter.getParameters().containsKey(idfield)) {
                    final String id = _parameter.getParameterValue(idfield);
                    //check the values in the database
                    final PrintQuery printQuery = new PrintQuery(set, id);
                    for (final Attribute attr : set.getAttributes().values()) {
                        printQuery.addAttribute(attr);
                    }
                    printQuery.execute();

                    final Update setupdate = new Update(set, id);
                    int xCoord = 0;
                    boolean update = false;
                    for (final String attrName : fieldset.getOrder()) {
                        final Attribute child = set.getAttribute(attrName);
                        final String fieldName = fieldset.getName() + "_" + nf.format(yCoord) + nf.format(xCoord);
                        if (_parameter.getParameters().containsKey(fieldName)) {
                            final Object object = printQuery.getAttribute(attrName);
                            final String oldValue = object != null ? object.toString() : null;
                            final String newValue = _parameter.getParameterValue(fieldName);
                            if (!newValue.equals(oldValue)) {
                                if (child.hasUoM()) {
                                    setupdate.add(child, new Object[] { newValue,
                                                                     _parameter.getParameterValue(fieldName + "UoM") });
                                } else {
                                    setupdate.add(child, newValue);
                                }
                                update = true;
                            }
                        }
                        xCoord++;
                    }
                    if (update) {
                        setupdate.execute();
                    }
                } else {
                    updateExisting = false;
                }
                yCoord++;
            }

            // add new values
            final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);
            if (others != null) {
                // add new Values
                final String[] yCoords = (String[]) others.get(fieldset.getName() + "_eFapsNew");
                if (yCoords != null) {

                    for (final String ayCoord : yCoords) {
                        final Insert insert = new Insert(set);
                        insert.add(set.getAttribute(setName), ((Long) _instance.getId()).toString());
                        int xCoord = 0;
                        for (final String attrName : fieldset.getOrder()) {
                            final Attribute child = set.getAttribute(attrName);
                            final String fieldName = fieldset.getName() + "_eFapsNew_"
                                            + nf.format(Integer.parseInt(ayCoord)) + nf.format(xCoord);
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

                // remove deleted Values
                final String[] removeOnes = (String[]) others.get(fieldset.getName() + "eFapsRemove");
                if (removeOnes != null) {
                    for (final String removeOne : removeOnes) {
                        final Delete delete = new Delete(set, removeOne);
                        delete.execute();
                    }
                }
            }
        }
    }

    /**
     * Method to update the classifications.
     *
     * @param _parameter Parameter as passed from the efaps API.
     * @param _instance Instance of the new object
     * @param _classifcationName name of the classificationto be updated
     * @throws EFapsException on error
     */
    protected void updateClassifcation(final Parameter _parameter, final Instance _instance,
                                       final String _classifcationName)
        throws EFapsException
    {

        final List<?> classifications = (List<?>) _parameter.get(ParameterValues.CLASSIFICATIONS);
        final Classification classType = (Classification) Type.get(_classifcationName);

        final Map<Classification, Map<String, Object>> clas2values = new HashMap<Classification, Map<String, Object>>();
        // get the already existing classifications
        final SearchQuery relQuery = new SearchQuery();
        relQuery.setExpand(_instance, classType.getClassifyRelationType().getName() + "\\"
                                                                                + classType.getRelLinkAttributeName());
        relQuery.addSelect(classType.getRelTypeAttributeName());
        relQuery.addSelect("OID");
        relQuery.execute();

        while (relQuery.next()) {
            final Long typeid = (Long) relQuery.get(classType.getRelTypeAttributeName());
            final Classification subClassType = (Classification) Type.get(typeid);
            final Map<String, Object> values = new HashMap<String, Object>();
            clas2values.put(subClassType, values);

            final SearchQuery subquery = new SearchQuery();
            subquery.setExpand(_instance, subClassType.getName() + "\\" + subClassType.getLinkAttributeName());
            subquery.addSelect("OID");

            final List<Field> fields = new ArrayList<Field>();
            final Form form = Form.getTypeForm(subClassType);
            for (final Field field : form.getFields()) {
                final String attrName = field.getExpression() == null
                                                ? field.getAttribute()
                                                : field.getExpression();
                if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                    final Attribute attr = subClassType.getAttribute(attrName);
                    // check if not a fileupload
                    if (attr != null
                                  && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                        subquery.addSelect(attrName);
                        fields.add(field);
                    }
                }
            }
            subquery.execute();
            if (subquery.next()) {
                for (final Field field : fields) {
                    final String attrName = field.getExpression() == null
                                                    ? field.getAttribute()
                                                    : field.getExpression();
                    values.put(field.getName(), subquery.get(attrName));
                }
                values.put("OID", subquery.get("OID"));
            }
            subquery.close();
            values.put("relOID", relQuery.get("OID"));
        }
        relQuery.close();

        if (classifications != null) {
            for (final Object object : classifications) {
                final Classification classification = (Classification) object;
                // if the classification does not exist yet the relation must be
                // created, and the new instance of the classification inserted
                final Form form = Form.getTypeForm(classification);
                if (!clas2values.containsKey(classification)) {
                    final Insert relInsert = new Insert(classification.getClassifyRelationType());
                    relInsert.add(classification.getRelLinkAttributeName(), ((Long) _instance.getId()).toString());
                    relInsert.add(classification.getRelTypeAttributeName(), ((Long) classification.getId()).toString());
                    relInsert.execute();

                    final Insert classInsert = new Insert(classification);
                    classInsert.add(classification.getLinkAttributeName(), ((Long) _instance.getId()).toString());
                    final List<FieldSet> fieldsets = new ArrayList<FieldSet>();
                    for (final Field field : form.getFields()) {
                        if (field instanceof FieldSet) {
                            fieldsets.add((FieldSet) field);
                        } else {
                            final String attrName = field.getExpression() == null
                                                        ? field.getAttribute()
                                                        : field.getExpression();
                            if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                                final Attribute attr = classification.getAttribute(attrName);
                                if (attr != null
                                        && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                        .getClassRepr())) {
                                    if (_parameter.getParameters().containsKey(field.getName())) {
                                        final String value = _parameter.getParameterValue(field.getName());
                                        if (attr.hasUoM()) {
                                            classInsert.add(attr, new Object[] { value,
                                                              _parameter.getParameterValue(field.getName() + "UoM") });
                                        } else {
                                            classInsert.add(attr, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    classInsert.execute();
                    updateFieldSets(_parameter, classInsert.getInstance(), fieldsets);
                } else {
                    final Map<String, Object> values = clas2values.get(classification);
                    final List<FieldSet> fieldsets = new ArrayList<FieldSet>();
                    final Update update = new Update((String) values.get("OID"));
                    boolean execUpdate = false;
                    for (final Field field : form.getFields()) {
                        if (field instanceof FieldSet) {
                            fieldsets.add((FieldSet) field);
                        } else {
                            final String attrName = field.getExpression() == null
                                                        ? field.getAttribute()
                                                        : field.getExpression();
                            if (attrName != null && field.isEditableDisplay(TargetMode.EDIT)) {
                                final Attribute attr = classification.getAttribute(attrName);
                                if (attr != null
                                                && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                                .getClassRepr())) {
                                    if (_parameter.getParameters().containsKey(field.getName())) {
                                        final String newValue = _parameter.getParameterValue(field.getName());
                                        final Object value = values.get(field.getName());
                                        final String oldValue = value != null ? value.toString() : null;
                                        if (!newValue.equals(oldValue)) {
                                            execUpdate = true;
                                            if (attr.hasUoM()) {
                                                update.add(attr, new Object[] { newValue,
                                                               _parameter.getParameterValue(field.getName() + "UoM") });
                                            } else {
                                                update.add(attr, newValue);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (execUpdate) {
                        update.execute();
                    }
                    updateFieldSets(_parameter, update.getInstance(), fieldsets);
                }
            }
        }
        // remove the classifications that are not any more wanted
        for (final Classification clas : clas2values.keySet()) {
            if (classifications == null || !classifications.contains(clas)) {
                Delete del = new Delete((String) clas2values.get(clas).get("OID"));
                del.execute();
                del = new Delete((String) clas2values.get(clas).get("relOID"));
                del.execute();
            }
        }
    }
}
