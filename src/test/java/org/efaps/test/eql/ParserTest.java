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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.efaps.eql.EQLParser;
import org.efaps.eql.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */

public class ParserTest
{

    @Test
    public void oneType()
        throws ParseException
    {
        final String stmtStr = "query type CompanyType";
        System.out.println(stmtStr);
        final EQLParser parser = new EQLParser(new StringReader(stmtStr));
        final TestStatement stmt = new TestStatement();
        parser.createStatement(stmt);
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");
    }

    @Test
    public void multipleTypes()
        throws ParseException
    {
        final String stmtStr = "query type CompanyType, OtroType";
        System.out.println(stmtStr);
        final EQLParser parser = new EQLParser(new StringReader(stmtStr));
        final TestStatement stmt = new TestStatement();
        parser.createStatement(stmt);
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        Assert.assertEquals(types, stmt.getTypes(), "No");
    }

    @Test
    public void multipleTypesWithSelect()
        throws ParseException
    {
        final String stmtStr = "query type CompanyType, OtroType select attribute[Name]";
        System.out.println(stmtStr);
        final EQLParser parser = new EQLParser(new StringReader(stmtStr));
        final TestStatement stmt = new TestStatement();
        parser.createStatement(stmt);
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        final List<String> selects =new ArrayList<>();
        selects.add("attribute[Name]");

        Assert.assertEquals(types, stmt.getTypes(), "No");
        Assert.assertEquals(selects, stmt.getSelects(), "No");
    }

    @Test
    public void multipleTypesWithMultipleSelect()
        throws ParseException
    {
        final String stmtStr = "query type CompanyType, OtroType select attribute[Name], linkto[Otro].instance";
        System.out.println(stmtStr);
        final EQLParser parser = new EQLParser(new StringReader(stmtStr));
        final TestStatement stmt = new TestStatement();
        parser.createStatement(stmt);
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        final List<String> selects =new ArrayList<>();
        selects.add("attribute[Name]");
        selects.add("linkto[Otro].instance");

        Assert.assertEquals(types, stmt.getTypes(), "No");
        Assert.assertEquals(selects, stmt.getSelects(), "No");
    }
}
