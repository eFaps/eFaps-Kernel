/*
 * Copyright 2003 - 2019 The eFaps Team
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
package org.efaps.eql.builder;

import static org.testng.Assert.assertEquals;

import org.efaps.db.Instance;
import org.efaps.eql.EQL;
import org.efaps.mock.datamodel.CI;
import org.efaps.test.AbstractTest;
import org.testng.annotations.Test;

public class BuilderTest
    extends AbstractTest
{

    @Test
    public void testPrintQueryString()
    {
        final String stmt = EQL.print()
            .query(CI.SimpleType)
            .where()
            .attribute(CI.SimpleType.TestAttr).eq("Hallo World")
            .select()
            .attribute(CI.SimpleType.TestAttr)
            .stmt()
            .asString();
        assertEquals(stmt, "print query type " + CI.SimpleType.uuid
                        + " where attribute[TestAttribute] == \"Hallo World\""
                        + " select attribute[TestAttribute] as \"CIALIASTestAttribute\"");
    }

    @Test
    public void testPrintQueryInstanc()
    {
        final String stmt = EQL.print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.LongAttribute).eq(Instance.get(CI.SimpleType.uuid, 56))
            .select()
            .attribute(CI.AllAttrType.StringAttribute)
            .stmt()
            .asString();
        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrLongAttribute] == 56"
                        + " select attribute[AllAttrStringAttribute] as \"CIALIASAllAttrStringAttribute\"");
    }

    @Test
    public void testPrintQueryNumber()
    {
        final String stmt = EQL.print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIASAllAttrIntegerAttribute\"");
    }

    @Test
    public void testPrintQueryVarious()
    {
        final String stmt = EQL.print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .and()
            .attribute(CI.AllAttrType.StringAttribute).eq("Hello world")
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " and attribute[AllAttrStringAttribute] == \"Hello world\""
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIASAllAttrIntegerAttribute\"");
    }

    @Test
    public void testPrintQueryVariousWithSelect()
    {
        final String stmt = EQL.print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .and()
            .attribute(CI.AllAttrType.StringAttribute).eq("Hello world")
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .attribute(CI.AllAttrType.StringAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " and attribute[AllAttrStringAttribute] == \"Hello world\""
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIASAllAttrIntegerAttribute\","
                        + " attribute[AllAttrStringAttribute] as \"CIALIASAllAttrStringAttribute\"");
    }

    @Test
    public void testPrintQueryWithAlias()
    {
        final String stmt = EQL.print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute).as("This is the Alias")
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select attribute[AllAttrIntegerAttribute] as \"This is the Alias\"");
    }

    @Test
    public void testPrintQueryWithLinkto()
    {
        final String stmt = EQL.print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .linkto(CI.AllAttrType.LinkAttribute).attribute(CI.SimpleType.TestAttr).as("This is the Alias")
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select linkto[AllAttrLinkAttribute].attribute[TestAttribute] as \"This is the Alias\"");
    }

    @Test
    public void testPrintQueryOrder()
    {
        final String stmt = EQL.print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .orderBy(CI.AllAttrType.IntegerAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIASAllAttrIntegerAttribute\""
                        + " order by AllAttrIntegerAttribute asc");
    }

}
