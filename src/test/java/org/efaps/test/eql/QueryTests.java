/*
 * Copyright 2003 - 2014 The eFaps Team
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


package org.efaps.test.eql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.eql.IStatement.StmtType;
import org.efaps.eql.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Group of tests for real life statements
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QueryTests
{
    @Test(description = "Statement from the Data_ExportAttributeDefinitions.jrxml jasperreport")
    public void query1()
        throws ParseException
    {
        final String stmtStr = "query type ERP_AttributeDefinitionAbstract select attribute[Value] as Value, type.name as Type,"
                        + "attribute[Description] as Description, attribute[MappingKey] as Key,"
                        +" status.key as StatusKey, status.type.name as StatusGroup";
        final TestStatement stmt = new ParserTest().testStatement(stmtStr);

        Assert.assertEquals(stmt.getStmtType(), StmtType.QUERY, "No");

        final List<String> types = new ArrayList<>();
        types.add("ERP_AttributeDefinitionAbstract");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> selects2alias = new HashMap<>();
        selects2alias.put("attribute[Value]", "Value");
        selects2alias.put("type.name", "Type");
        selects2alias.put("attribute[Description]", "Description");
        selects2alias.put("attribute[MappingKey]", "Key");
        selects2alias.put("status.key", "StatusKey");
        selects2alias.put("status.type.name", "StatusGroup");
        Assert.assertEquals(stmt.getSelects2alias(), selects2alias, "No");
    }
}
