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
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id:Edit.java 1563 2007-10-28 14:07:41Z tmo $
 * @todo description
 */
@EFapsUUID("d9ba2b85-8b9a-46b0-929e-8e938e7d5577")
@EFapsRevision("$Rev$")
public class Edit implements EventExecution
{

    private String classifcationName;

    /**
     * @param _parameter Parameter as provided by eFaps for a esjp
     * @throws EFapsException on error
     * @return empty Return
     */
    public Return execute(final Parameter _parameter) throws EFapsException
    {

        final Instance instance = _parameter.getInstance();
        final AbstractCommand command = (AbstractCommand) _parameter.get(ParameterValues.UIOBJECT);
        final Map<?, ?> others = (HashMap<?, ?>) _parameter.get(ParameterValues.OTHERS);

        final Context context = Context.getThreadContext();
        // update the Values for the general form
        final List<FieldSet> fieldsets = updateForm(command.getTargetForm(), instance);

        // ************************************
        // Update for FieldSets
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);

        for (final FieldSet fieldset : fieldsets) {

            final AttributeSet set = AttributeSet.find(instance.getType().getName(), fieldset.getExpression());

            boolean updateExisting = true;
            int idy = 0;
            while (updateExisting) {
                final String idfield = "hiddenId" + fieldset.getName() + nf.format(idy);
                if (context.getParameters().containsKey(idfield)) {
                    final String id = context.getParameter(idfield);

                    final Update setupdate = new Update(set, id);
                    int idx = 0;
                    for (final String attrName : fieldset.getOrder()) {
                        final Attribute child = set.getAttribute(attrName);
                        final String fieldName = fieldset.getName() + nf.format(idy) + nf.format(idx);

                        if (context.getParameters().containsKey(fieldName)) {
                            setupdate.add(child, context.getParameter(fieldName));
                        }
                        idx++;
                    }
                    setupdate.execute();
                } else {
                    updateExisting = false;
                }
                idy++;
            }
            if (others != null) {
                // add new Values
                final String[] newOnes = (String[]) others.get(fieldset.getName() + "eFapsNew");
                if (newOnes != null) {
                    for (final String newOne : newOnes) {
                        final Insert insert = new Insert(set);
                        insert.add(set.getAttribute(fieldset.getExpression()), ((Long) instance.getId()).toString());
                        int idx = 0;
                        for (final String attrName : fieldset.getOrder()) {
                            final Attribute child = set.getAttribute(attrName);
                            final String fieldName = fieldset.getName() + "eFapsNew"
                                            + nf.format(Integer.parseInt(newOne)) + nf.format(idx);
                            if (context.getParameters().containsKey(fieldName)) {
                                insert.add(child, context.getParameter(fieldName));
                            }
                            idx++;
                        }
                        insert.execute();
                    }
                }

                // remove Values
                final String[] removeOnes = (String[]) others.get(fieldset.getName() + "eFapsRemove");
                if (removeOnes != null) {
                    for (final String removeOne : removeOnes) {
                        final Delete delete = new Delete(set, removeOne);
                        delete.execute();
                    }
                }
            }
        }

        // ************************************
        // check if we have a fileupload field
        if (context.getFileParameters().size() > 0) {
            for (final Field field : command.getTargetForm().getFields()) {
                if (field.getExpression() == null && field.isEditable()) {
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

        if (this.classifcationName != null) {
            updateClassifcation(_parameter, instance);
        }




        return new Return();
    }

    private List<FieldSet> updateForm(final Form _form, final Instance _instance) throws EFapsException {

        // ************************************
        // Update for Fields
        final List<FieldSet> fieldsets = new ArrayList<FieldSet>();
        final List<Field> fields = new ArrayList<Field>();

        final SearchQuery query = new SearchQuery();
        query.setObject(_instance);
        for (final Field field : _form.getFields()) {
            if (field instanceof FieldSet) {
                fieldsets.add((FieldSet) field);
            } else if (field instanceof FieldClassification) {
               this.classifcationName = ((FieldClassification) field).getClassificationName();
            } else {
                if (field.getExpression() != null && field.isEditable()) {
                    final Attribute attr = _instance.getType().getAttribute(field.getExpression());
                    // check if not a fileupload
                    if (attr != null
                                    && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                        query.addSelect(field.getExpression());
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
                    final Object object = query.get(field.getExpression());
                    final String oldValue = object != null ? object.toString() : null;
                    if (!newValue.equals(oldValue)) {
                        update.add(field.getExpression(), newValue);
                    }
                }
            }
            update.execute();
        }
        return fieldsets;
    }


    private void updateClassifcation(final Parameter _parameter, final Instance _instance) throws EFapsException {
        final Context context = Context.getThreadContext();
        final List<?> classifications = (List<?>) _parameter.get(ParameterValues.CLASSIFICATIONS);
        final Classification classType = (Classification) Type.get(this.classifcationName);
        final SearchQuery relQuery = new SearchQuery();
        relQuery.setExpand(_instance, classType.getClassifyRelationType().getName()
                                        + "\\" + classType.getRelLinkAttributeName());
        relQuery.addSelect(classType.getRelTypeAttributeName());
        relQuery.execute();
        final Map<Classification, Map<String, Object>> class2values = new HashMap<Classification, Map<String, Object>>();
        while (relQuery.next()) {
            final Long typeid = (Long) relQuery.get(classType.getRelTypeAttributeName());
            final Classification subClassType = (Classification) Type.get(typeid);
            final Map<String, Object> values = new HashMap<String, Object>();
            class2values.put(subClassType, values);

            final SearchQuery subquery = new SearchQuery();
            subquery.setExpand(_instance,
                               subClassType.getName() + "\\" + subClassType.getLinkAttributeName());
            subquery.addSelect("OID");

            final List<Field> fields = new ArrayList<Field>();
            final Form form = Form.getTypeForm(subClassType);
            for (final Field field : form.getFields()) {
                if (field instanceof FieldSet) {
                    //fieldsets.add((FieldSet) field);
                } else {
                    if (field.getExpression() != null && field.isEditable()) {
                        final Attribute attr = subClassType.getAttribute(field.getExpression());
                        // check if not a fileupload
                        if (attr != null
                                        && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr())) {
                            subquery.addSelect(field.getExpression());
                            fields.add(field);
                        }
                    }
                }
            }
            subquery.execute();
            if (subquery.next()) {
                for (final Field field : fields) {
                    values.put(field.getName(), subquery.get(field.getExpression()));
                }
                values.put("OID", subquery.get("OID"));
            }
            subquery.close();
        }
        relQuery.close();

        for (final Object object : classifications) {
            final Classification classification = (Classification) object;
            // if the classification does not exist yet the relation must be created,
            // and the new instance of the classification inserted
            final Form form = Form.getTypeForm(classification);
            if (!class2values.containsKey(classification)) {
                final Insert relInsert = new Insert(classification.getClassifyRelationType());
                relInsert.add(classification.getRelLinkAttributeName(), ((Long) _instance.getId()).toString());
                relInsert.add(classification.getRelTypeAttributeName(), ((Long) classification.getId()).toString());
                relInsert.execute();

                final Insert classInsert = new Insert(classification);
                classInsert.add(classification.getLinkAttributeName(), ((Long) _instance.getId()).toString());
                for (final Field field : form.getFields()) {
                    if (field.getExpression() != null && (field.isCreatable() || field.isHidden())) {
                        final Attribute attr = classification.getAttribute(field.getExpression());
                        if (attr != null
                                        && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                                                        .getClassRepr())) {
                            if (context.getParameters().containsKey(field.getName())) {
                                final String value = context.getParameter(field.getName());
                                classInsert.add(attr, value);
                            }
                        }
                    }
                }
                classInsert.execute();
            } else {
                final Map<String, Object> values = class2values.get(classification);
                final Update update = new Update((String) values.get("OID"));
                boolean execUpdate = false;
                for (final Field field : form.getFields()) {
                    if (context.getParameters().containsKey(field.getName())) {
                        final String newValue = context.getParameter(field.getName());
                        final Object value = values.get(field.getName());
                        final String oldValue = value != null ? value.toString() : null;
                        if (!newValue.equals(oldValue)) {
                            update.add(field.getExpression(), newValue);
                            execUpdate = true;
                        }
                    }
                }
                if (execUpdate) {
                    update.execute();
                }
            }
        }
    }
}
