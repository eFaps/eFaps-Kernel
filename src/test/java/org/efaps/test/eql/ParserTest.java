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

    @Test(description = "Query with a esjp select")
    public void selectEsjp()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select esjp[org.efaps.demo.Test]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("esjp[org.efaps.demo.Test]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectEsjpParameter()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select esjp[org.efaps.demo.Test,12]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("esjp[org.efaps.demo.Test,12]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectEsjpParameter2()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select esjp[org.efaps.demo.Test,12,14]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("esjp[org.efaps.demo.Test,12,14]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectEsjpParameter3()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select esjp[org.efaps.demo.Test,\"das it ein text\",14]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("esjp[org.efaps.demo.Test,\"das it ein text\",14]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectMultipleEsjp()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select esjp[org.efaps.demo.Test], esjp[org.efaps.demo.TestA]");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("esjp[org.efaps.demo.Test]");
        selects.add("esjp[org.efaps.demo.TestA]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectMultipleEsjpWithMapping()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select esjp[org.efaps.demo.Test] as erster, esjp[org.efaps.demo.TestA] as zweiter");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> selects = new HashMap<>();
        selects.put("esjp[org.efaps.demo.Test]", "erster");
        selects.put("esjp[org.efaps.demo.TestA]", "zweiter");
        Assert.assertEquals(stmt.getSelects2alias(), selects, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectMultipleEsjpMixed()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select esjp[org.efaps.demo.Test], esjp[org.efaps.demo.TestA] as zweiter");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("esjp[org.efaps.demo.Test]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("esjp[org.efaps.demo.TestA]", "zweiter");
        Assert.assertEquals(stmt.getSelects2alias(), mapping, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectMixed()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name], linkto[teer].oid, esjp[org.efaps.demo.Test], esjp[org.efaps.demo.TestA] as zweiter");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        selects.add("linkto[teer].oid");
        selects.add("esjp[org.efaps.demo.Test]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("esjp[org.efaps.demo.TestA]", "zweiter");
        Assert.assertEquals(stmt.getSelects2alias(), mapping, "No");
    }

    @Test(description = "Query with a esjp select")
    public void selectMixed2()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name], linkto[teer].oid as Aliass, esjp[org.efaps.demo.Test], esjp[org.efaps.demo.TestA] as zweiter");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final List<String> selects = new ArrayList<>();
        selects.add("attribute[Name]");
        selects.add("esjp[org.efaps.demo.Test]");
        Assert.assertEquals(stmt.getSelects(), selects, "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("linkto[teer].oid", "Aliass");
        mapping.put("esjp[org.efaps.demo.TestA]", "zweiter");
        Assert.assertEquals(stmt.getSelects2alias(), mapping, "No");

    }

    @Test
    public void oneTypeWhereEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo\"");
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
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo\" select attribute[Name]");
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
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo de algo\"");
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
        final TestStatement stmt = testStatement("query type CompanyType where id == 345");
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
        final TestStatement stmt = testStatement("query type CompanyType, Accounting_Transaction2PaymentDocument");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        types.add("Accounting_Transaction2PaymentDocument");
        Assert.assertEquals(types, stmt.getTypes(), "No");
    }

    @Test
    public void multipleTypesWhereEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType, OtroType where name == \"demo\"");
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
        final TestStatement stmt = testStatement("query type CompanyType, OtroType where name == \"demo\" select attribute[Name]");
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
    public void execEsjp()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");
    }

    @Test
    public void executeEsjp()
        throws ParseException
    {
        final TestStatement stmt = testStatement("execute org.efaps.demo.Test");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");
    }

    @Test
    public void execEsjpParameter()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test 2");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");

        final List<String> parameters = new ArrayList<>();
        parameters.add("2");
        Assert.assertEquals(stmt.getParameters(), parameters, "No");
    }

    @Test
    public void execEsjpParameters()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test 2, 44");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");
        final List<String> parameters = new ArrayList<>();
        parameters.add("2");
        parameters.add("44");
        Assert.assertEquals(stmt.getParameters(), parameters, "No");
    }

    @Test
    public void execEsjpParameters2()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test \"Param1 with space\", 24");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");
        final List<String> parameters = new ArrayList<>();
        parameters.add("Param1 with space");
        parameters.add("24");
        Assert.assertEquals(stmt.getParameters(), parameters, "No");
    }

    @Test
    public void execEsjpParameters3()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test \"Param1 with space\", \"ABCDE\"");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");
        final List<String> parameters = new ArrayList<>();
        parameters.add("Param1 with space");
        parameters.add("ABCDE");
        Assert.assertEquals(stmt.getParameters(), parameters, "No");
    }

    @Test
    public void execEsjpMapping()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test select 1 as Key");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("1", "Key");
        Assert.assertEquals(mapping, stmt.getSelects2alias(), "No");
    }

    @Test
    public void execEsjpMapping1()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test select 1 as Key, 5 as Demo");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("1", "Key");
        mapping.put("5", "Demo");
        Assert.assertEquals(stmt.getSelects2alias(), mapping, "No");
    }

    @Test
    public void execEsjpParametersMapping()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test \"Param1 with space\", \"ABCDE\" select 1 as Key");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");
        final List<String> parameters = new ArrayList<>();
        parameters.add("Param1 with space");
        parameters.add("ABCDE");
        Assert.assertEquals(stmt.getParameters(), parameters, "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("1", "Key");
        Assert.assertEquals(mapping, stmt.getSelects2alias(), "No");
    }

    @Test
    public void execEsjpParametersMapping2()
        throws ParseException
    {
        final TestStatement stmt = testStatement("exec org.efaps.demo.Test \"Param1 with space\", \"ABCDE\" select 1 as Key, 2 as Demo");
        Assert.assertEquals(stmt.getStmtType(), StmtType.ESJP, "No");
        Assert.assertEquals(stmt.getEsjp(), "org.efaps.demo.Test", "No");

        final List<String> parameters = new ArrayList<>();
        parameters.add("Param1 with space");
        parameters.add("ABCDE");
        Assert.assertEquals(stmt.getParameters(), parameters, "No");

        final Map<String, String> mapping = new HashMap<>();
        mapping.put("1", "Key");
        mapping.put("2", "Demo");
        Assert.assertEquals(stmt.getSelects2alias(), mapping, "No");

    }

    @Test
    public void whereEqAndEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo\" and num == 4");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("name", "demo");
        wheremap.put("num", "4");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
    }

    @Test
    public void whereOID()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where link == 5905.636");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("link", "5905.636");
        Assert.assertEquals(stmt.getAttr2whereEq(), wheremap, "No");
    }

    @Test
    public void whereSelectEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where linkto[ContactLink].attribute[Num] == 15");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> wheremap = new HashMap<>();
        wheremap.put("linkto[ContactLink].attribute[Num]", "15");
        Assert.assertEquals(stmt.getSelect2whereEq(), wheremap, "No");
    }

    @Test
    public void whereInOID()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where link in (5905.636,1234.6464)");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Collection<String>> wheremap = new HashMap<>();
        final List<String> inner = new ArrayList<>();
        inner.add("5905.636");
        inner.add("1234.6464");
        wheremap.put("link", inner);
        Assert.assertEquals(stmt.getAttr2whereIn(), wheremap, "No");
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
    public void whereSelectInNum()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where linkto[ContactLink].attribute[Num] in (4,8)");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Collection<String>> wheremap = new HashMap<>();
        final List<String> inner = new ArrayList<>();
        inner.add("4");
        inner.add("8");
        wheremap.put("linkto[ContactLink].attribute[Num]", inner);
        Assert.assertEquals(stmt.getSelect2whereIn(), wheremap, "No");
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
    public void whereSelectInNumStr()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where linkto[ContactLink].attribute[numname] in (124,\"test2\")");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Collection<String>> wheremap = new HashMap<>();
        final List<String> inner = new ArrayList<>();
        inner.add("124");
        inner.add("test2");
        wheremap.put("linkto[ContactLink].attribute[numname]", inner);
        Assert.assertEquals(stmt.getSelect2whereIn(), wheremap, "No");
    }

    @Test
    public void whereEqAndGreater()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo\" and num > 4");
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
    public void whereSelectEqAndGreater()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where linkto[ContactLink].attribute[name] == \"demo\" and linkto[ContactLink].attribute[num] > 4");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> whereEqMap = new HashMap<>();
        whereEqMap.put("linkto[ContactLink].attribute[name]", "demo");
        Assert.assertEquals(stmt.getSelect2whereEq(), whereEqMap, "No");

        final Map<String, String> whereGreaterMap = new HashMap<>();
        whereGreaterMap.put("linkto[ContactLink].attribute[num]", "4");
        Assert.assertEquals(stmt.getSelect2whereGreater(), whereGreaterMap, "No");
    }

    @Test
    public void whereEqAndLess()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo\" and num < 4");
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
    public void whereSelectEqAndLess()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where linkto[ContactLink].attribute[name] == \"demo\" and linkto[ContactLink].attribute[num] < 4");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, String> whereEqMap = new HashMap<>();
        whereEqMap.put("linkto[ContactLink].attribute[name]", "demo");
        Assert.assertEquals(stmt.getSelect2whereEq(), whereEqMap, "No");

        final Map<String, String> whereLessMap = new HashMap<>();
        whereLessMap.put("linkto[ContactLink].attribute[num]", "4");
        Assert.assertEquals(stmt.getSelect2whereLess(), whereLessMap, "No");
    }


    @Test
    public void whereEqAndLessAndEq()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo\" and num < 4 and was == \"irgendwas\"");
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
        final TestStatement stmt = testStatement("query type CompanyType where name == \"demo\" and num < 4 and was == \"irgendwas\" select attribute[Name], linkto[Otro].instance");
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

    @Test
    public void orderByNum()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by 1");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("1", true);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByNumAsc()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by 1 asc");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("1", true);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByNumDesc()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by 1 desc");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("1", false);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByAlias()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by alias ");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("alias", true);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByAliasAsc()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by alias asc");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("alias", true);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByAliasDesc()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by alias desc");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("alias", false);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByNumMultiple()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by 1 , 3");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("1", true);
        orderMap.put("3", true);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByNumMultipleAscDesc()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by 1 asc, 3 desc");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("1", true);
        orderMap.put("3", false);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByAliasMultiple()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by blabla , wewe");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("blabla", true);
        orderMap.put("wewe", true);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

    @Test
    public void orderByAliasMultipleAscDesc()
        throws ParseException
    {
        final TestStatement stmt = testStatement("query type CompanyType select attribute[Name] order by blabla asc, wewe desc");
        final List<String> types = new ArrayList<>();
        types.add("CompanyType");
        Assert.assertEquals(stmt.getTypes(), types, "No");

        final Map<String, Boolean> orderMap = new HashMap<>();
        orderMap.put("blabla", true);
        orderMap.put("wewe", false);
        Assert.assertEquals(stmt.getOrder2ascdesc(), orderMap, "No");
    }

}
