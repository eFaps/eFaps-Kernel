/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.eql;

import org.eclipse.xtext.diagnostics.Severity;
import org.efaps.admin.dbproperty.DBProperties;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class EFapsDiagnostic
    extends org.efaps.eql.validation.EFapsDiagnostic
{

    /**
     * Instantiates a new eFaps diagnostic.
     *
     * @param _severity the severity
     * @param _message the message
     * @param _code the code
     * @param _issueData the issue data
     */
    public EFapsDiagnostic(final Severity _severity,
                           final String _message,
                           final String _code,
                           final String... _issueData)
    {
        super(_severity, _message, _code, _issueData);
    }

    @Override
    public String getMessage()
    {
        DBProperties.getFormatedDBProperty(super.getMessage(), getData().toArray());
        return super.getMessage();
    }
}
