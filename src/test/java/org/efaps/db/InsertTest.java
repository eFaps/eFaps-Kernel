package org.efaps.db;

import java.time.LocalDate;

import org.efaps.mock.datamodel.CI;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;

public class InsertTest
    extends AbstractTest
{

    @Test
    public void testLocalDateInsert()
        throws EFapsException
    {
        final Insert insert = new Insert(CI.AllAttrType);
        insert.add(CI.AllAttrType.DateAttribute, LocalDate.of(2019,8,8));
        insert.execute();
    }

}
