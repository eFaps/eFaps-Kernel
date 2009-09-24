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

package org.efaps.esjp.common.jasperreport;

import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.FormatedStringType;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("4675dffe-6551-477b-b069-8968901aeff4")
@EFapsRevision("$Rev$")
abstract class EFapsDataSource_Base implements JRDataSource
{

    /**
     * PrintQuery for this datasource.
     */
    private MultiPrintQuery print;

    /**
     * has this report a subreport? Used to return on the first call of
     * {@link #next()} true in all cases.
     */
    private boolean subReport;

    /**
     * Method to initialize this datasource.
     * @param _jasperReport jasperreport this datasource belongs to
     * @throws EFapsException on error
     */
    public void init(final JasperReport _jasperReport) throws EFapsException
    {
        String typeName = null;
        boolean expand = false;
        for (final JRParameter para : _jasperReport.getMainDataset().getParameters()) {
            if ("EFAPS_DEFINITION".equals(para.getName())) {
                if (para.hasProperties()) {
                    typeName  = para.getPropertiesMap().getProperty("Type");
                    this.subReport = "true".equalsIgnoreCase(para.getPropertiesMap().getProperty("hasSubReport"));
                    expand = "true".equalsIgnoreCase(para.getPropertiesMap().getProperty("expandChildTypes"));
                }
            }
        }

        if (typeName != null) {
            final List<Instance> instances = new ArrayList<Instance>();
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(typeName);
            query.setExpandChildTypes(expand);
            query.addSelect("OID");
            query.execute();
            while (query.next()) {
                instances.add(Instance.get((String) query.get("OID")));
            }
            query.close();
            if (instances.size() > 0) {
                this.print = new MultiPrintQuery(instances);
                for (final JRField field : _jasperReport.getMainDataset().getFields()) {
                    final String select = field.getPropertiesMap().getProperty("Select");
                    if (select != null) {
                        this.print.addSelect(select);
                    }
                }
                this.print.execute();
            }
        }
    }

    /**
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     * @param _field JRField
     * @return value for the given field
     * @throws JRException on error
     */
    public Object getFieldValue(final JRField _field) throws JRException
    {
        Object ret = null;
        final String select = _field.getPropertiesMap().getProperty("Select");
        if (select != null) {
            try {
                ret = this.print.getSelect(select);
                final Attribute attr = this.print.getAttribute4Select(select);
                if (attr != null && attr.getAttributeType().getClassRepr().equals(FormatedStringType.class)) {
                    ret = HtmlMarkupConverter.getConvertedString((String) ret);
                }
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     * @return true if a next value exist, else false
     * @throws JRException on error
     */
    public boolean next() throws JRException
    {
        final boolean tmp = this.subReport;
        if (this.subReport) {
            this.subReport = false;
        }
        return this.print == null ? tmp : this.print.next();
    }
}
