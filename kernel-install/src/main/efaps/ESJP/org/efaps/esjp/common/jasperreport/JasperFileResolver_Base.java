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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.jasperreports.engine.util.FileResolver;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class is used to load files from the eFaps Database into an JasperReport.
 * There are two different Types of files that can be loaded:
 * <ul>
 * <li>JasperReports used as subreports</li>
 * <li>Images (JasperImages)</li>
 * </ul>
 * To now if a image or a subreport is wanted a naming convention is used.
 *  <ul>
 *    <li>Images must start with "JasperImage."</li>
 *    <li>JasperReports must start with "JasperReport."</li>
 *  </ul>
 *  <b>Caution: The dot is expected!</b>
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("4733dd43-2ef3-4572-a1e9-c820567e9a36")
@EFapsRevision("$Rev$")
public abstract class JasperFileResolver_Base implements FileResolver
{

    /**
     * @see net.sf.jasperreports.engine.util.FileResolver#resolveFile(java.lang.String)
     * @param _jasperFileName name of the jasper report
     * @return File
     */
    public File resolveFile(final String _jasperFileName)
    {
        File file = null;
        try {
            final SearchQuery query = new SearchQuery();
            String name = null;
            if (_jasperFileName.startsWith("JasperImage.")) {
                name = _jasperFileName.replace("JasperImage.", "");
                query.setQueryTypes("Admin_Program_JasperImage");
            } else if (_jasperFileName.startsWith("JasperReport.")) {
                name = _jasperFileName.replace("JasperReport.", "");
                query.setQueryTypes("Admin_Program_JasperReportCompiled");
            }
            query.addWhereExprEqValue("Name", name);
            query.addSelect("OID");
            query.execute();
            if (query.next()) {
                final Checkout checkout = new Checkout((String) query.get("OID"));
                checkout.preprocess();
                file = File.createTempFile(checkout.getFileName(), "jasper");
                final FileOutputStream out = new FileOutputStream(file);
                checkout.execute(out);
                out.close();
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }
}
