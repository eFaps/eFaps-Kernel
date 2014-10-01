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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.eql.EQLParser;
import org.efaps.eql.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ParserTest.class);

    private TestStatement testStatement(final String _stmtStr)
        throws ParseException
    {
        LOG.info("Validating: '{}'", _stmtStr);
        final EQLParser parser = new EQLParser(new StringReader(_stmtStr));
        final TestStatement ret = new TestStatement();
        parser.parseStatement(ret);
        return ret;
    }

    @Test
    public void oneType()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");
    }

    @Test
    public void oneTypeWithSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test
    public void oneTypeWhere()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2where(), wheremap, "No");
    }

    @Test
    public void oneTypeWhereWithSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\" select attribute[Name]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2where(), wheremap, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test
    public void oneTypeWhereWithSpace()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo de algo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo de algo");
        Assert.assertEquals(stmt.getAttr2where(), wheremap, "No");
    }

    @Test
    public void oneTypeWhereNumber()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where id = 345");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("id", "345");
        Assert.assertEquals(stmt.getAttr2where(), wheremap, "No");
    }

    @Test
    public void multipleTypes()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType, OtroType");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        Assert.assertEquals(types, stmt.getTypes(), "No");
    }

    @Test
    public void multipleTypesWhere()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType, OtroType where name = \"demo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2where(), wheremap, "No");
    }

    @Test
    public void multipleTypesWithSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType, OtroType select attribute[Name]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");

        Assert.assertEquals(types, stmt.getTypes(), "No");
        Assert.assertEquals(selects, stmt.getSelects(), "No");
    }

    @Test
    public void multipleTypesWithMultipleSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType, OtroType select attribute[Name], linkto[Otro].instance");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        selects.add("linkto[Otro].instance");

        Assert.assertEquals(types, stmt.getTypes(), "No");
        Assert.assertEquals(selects, stmt.getSelects(), "No");
    }

    @Test
    public void selectWithMapping()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] as Name");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("attribute[Name]", "Name");
        Assert.assertEquals(mapping, stmt.getSelects2alias(), "No");
    }

    @Test
    public void selectWithMultipleMapping()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] as Name, linkto[Otro].instance as instance");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("attribute[Name]", "Name");
        mapping.put("linkto[Otro].instance", "instance");

        Assert.assertEquals(mapping, stmt.getSelects2alias(), "No");
    }

    @Test
    public void selectWithMixedMapping()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name], linkto[Otro].instance as instance");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("linkto[Otro].instance", "instance");
        Assert.assertEquals(mapping, stmt.getSelects2alias(), "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        Assert.assertEquals(selects, stmt.getSelects(), "No");

    }

}
