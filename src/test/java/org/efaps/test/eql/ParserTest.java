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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.eql.EQLParser;
import org.efaps.eql.IStatement.StmtType;
import org.efaps.eql.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Generic basic tests fro the eql language.
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

    protected TestStatement testStatement(final String _stmtStr)
        throws ParseException
    {
        LOG.info("Validating: '{}'", _stmtStr);
        final EQLParser parser = new EQLParser(new StringReader(_stmtStr));
        final TestStatement ret = new TestStatement();
        parser.parseStatement(ret);
        return ret;
    }

    @Test(description = "Query with a Type")
    public void oneType()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType");
        Assert.assertEquals(stmt.getStmtType(), StmtType.QUERY, "No");

        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");
    }

    @Test(description = "Query with a Type and one attribute")
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
    public void oneTypeWhereEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
    }

    @Test
    public void oneTypeWhereGreater()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name > \"demo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereGreater(), wheremap, "No");
    }

    @Test
    public void oneTypeWhereLess()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name < \"demo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereLess(), wheremap, "No");
    }

    @Test
    public void oneTypeWhereEqWithSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\" select attribute[Name]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test
    public void oneTypeWhereEqWithSpace()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo de algo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo de algo");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
    }

    @Test
    public void oneTypeWhereEqNumber()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where id = 345");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("id", "345");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
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
    public void multipleTypesWhereEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType, OtroType where name = \"demo\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        Assert.assertEquals(types, stmt.getTypes(), "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
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
    public void multipleTypesWhereEqWithSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType, OtroType where name = \"demo\" select attribute[Name]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("OtroType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
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

    @Test
    public void print()
        throws ParseException
    {
        final TestStatement stmt = testStatement("print 123.456");
        Assert.assertEquals(stmt.getStmtType(), StmtType.PRINT, "No");
        Assert.assertEquals(stmt.getObject(), "123.456", "No");
    }

    @Test
    public void printWithSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("print 123.456 select attribute[Name]");
        Assert.assertEquals(stmt.getStmtType(), StmtType.PRINT, "No");
        Assert.assertEquals(stmt.getObject(), "123.456", "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test
    public void printWithMultipleSelects()
        throws ParseException
    {
        final TestStatement stmt = testStatement("print 123.456 select attribute[Name], linkto[Otro].instance");
        Assert.assertEquals(stmt.getStmtType(), StmtType.PRINT, "No");
        Assert.assertEquals(stmt.getObject(), "123.456", "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        selects.add("linkto[Otro].instance");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test
    public void queryEsjp()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query esjp org.efaps.demo.Test");
        Assert.assertEquals(stmt.getStmtType(), StmtType.QUERY, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");
    }

    @Test
    public void whereEqAndEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\" and num = 4");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        wheremap.put("num", "4");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
    }

    @Test
    public void whereInNum()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where num in (4,8)");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Collection<String>> wheremap = new HashMap<>();
        final List<String> inner = new ArrayList<>();
        inner.add("4");
        inner.add("8");
        wheremap.put("num", inner);
        Assert.assertEquals(stmt.getAttr2whereIn(), wheremap, "No");
    }

    @Test
    public void whereInStr()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name in (\"test\",\"test2\")");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Collection<String>> wheremap = new HashMap<>();
        final List<String> inner = new ArrayList<>();
        inner.add("test");
        inner.add("test2");
        wheremap.put("name", inner);
        Assert.assertEquals(stmt.getAttr2whereIn(), wheremap, "No");
    }

    @Test
    public void whereInNumStr()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where numname in (124,\"test2\")");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Collection<String>> wheremap = new HashMap<>();
        final List<String> inner = new ArrayList<>();
        inner.add("124");
        inner.add("test2");
        wheremap.put("numname", inner);
        Assert.assertEquals(stmt.getAttr2whereIn(), wheremap, "No");
    }


    @Test
    public void whereEqAndGreater()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\" and num > 4");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> whereEqMap = new HashMap<>();
        whereEqMap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereEq(), whereEqMap, "No");

        final Map<String, String> whereGreaterMap = new HashMap<>();
        whereGreaterMap.put("num", "4");
        Assert.assertEquals(stmt.getAttr2whereGreater(), whereGreaterMap, "No");
    }

    @Test
    public void whereEqAndLess()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\" and num < 4");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> whereEqMap = new HashMap<>();
        whereEqMap.put("name", "demo");
        Assert.assertEquals(stmt.getAttr2whereEq(), whereEqMap, "No");

        final Map<String, String> whereLessMap = new HashMap<>();
        whereLessMap.put("num", "4");
        Assert.assertEquals(stmt.getAttr2whereLess(), whereLessMap, "No");
    }

    @Test
    public void whereEqAndLessAndEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\" and num < 4 and was = \"irgendwas\"");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> whereEqMap = new HashMap<>();
        whereEqMap.put("name", "demo");
        whereEqMap.put("was", "irgendwas");
        Assert.assertEquals(stmt.getAttr2whereEq(), whereEqMap, "No");

        final Map<String, String> whereLessMap = new HashMap<>();
        whereLessMap.put("num", "4");
        Assert.assertEquals(stmt.getAttr2whereLess(), whereLessMap, "No");
    }

    @Test
    public void whereMultiWithSelect()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name = \"demo\" and num < 4 and was = \"irgendwas\" select attribute[Name], linkto[Otro].instance");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> whereEqMap = new HashMap<>();
        whereEqMap.put("name", "demo");
        whereEqMap.put("was", "irgendwas");
        Assert.assertEquals(stmt.getAttr2whereEq(), whereEqMap, "No");

        final Map<String, String> whereLessMap = new HashMap<>();
        whereLessMap.put("num", "4");
        Assert.assertEquals(stmt.getAttr2whereLess(), whereLessMap, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        selects.add("linkto[Otro].instance");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }
}
