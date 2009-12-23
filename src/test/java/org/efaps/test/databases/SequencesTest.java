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

package org.efaps.test.databases;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.efaps.db.Context;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Database connector tests for sequences.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SequencesTest
    extends AbstractTest
{
    /**
     * Name of the sequence which is used for the test.
     */
    private static final String TEST_SEQUENCE = "TEST_SEQ";

    /**
     * Set is used to store fetched number from the sequences to be sure that
     * no double numbers are returned.
     *
     * @see #testMultiThreadingFetching()
     */
    private final Set<Long> numbers = new HashSet<Long>();

    /**
     * Creates the sequence {@link #TEST_SEQUENCE} in eFaps database. To be
     * sure that the sequence does not already exists, a check is done and then
     * first deleted if sequence already defined.
     *
     * @throws SQLException     if sequence could not be created
     * @throws EFapsException   if eFaps context could not be opened / closed
     */
    @BeforeTest(description = "related sequence is created (and in front deleted if already exists)",
                dependsOnGroups = "connect")
    public void createSequence()
        throws SQLException, EFapsException
    {
        final Context context = Context.begin();
        try {
            if (Context.getDbType().existsSequence(context.getConnection(), SequencesTest.TEST_SEQUENCE))  {
                Context.getDbType().deleteSequence(context.getConnection(), SequencesTest.TEST_SEQUENCE);
            }
            Context.getDbType().createSequence(context.getConnection(),
                                               SequencesTest.TEST_SEQUENCE,
                                               100);
            final long number = Context.getDbType().nextSequence(Context.getThreadContext().getConnection(),
                                                                 SequencesTest.TEST_SEQUENCE);
            Assert.assertEquals(number, 100, "check that first number is 100");
        } finally  {
            Context.commit();
        }
    }

    /**
     * Deletes the test sequence {@link #TEST_SEQUENCE} and checks if the
     * sequence is really deleted.
     *
     * @throws SQLException     if sequence could not be deleted
     * @throws EFapsException   if eFaps context could not be opened / closed
     */
    @AfterTest(groups = "cleanup")
    public void deleteSequence()
        throws SQLException, EFapsException
    {
        final Context context = Context.begin();
        try {
            if (Context.getDbType().existsSequence(context.getConnection(), SequencesTest.TEST_SEQUENCE))  {
                Context.getDbType().deleteSequence(context.getConnection(), SequencesTest.TEST_SEQUENCE);
            }
            Assert.assertFalse(Context.getDbType().existsSequence(context.getConnection(),
                                                                  SequencesTest.TEST_SEQUENCE),
                               "check that sequence is deleted");
        } finally  {
            Context.commit();
        }
    }

    /**
     * Tests with multiple threads that the sequences could be fetched without
     * problems and without double entries.
     *
     * @throws SQLException         if sequence could not be fetched from
     *                              database
     * @throws EFapsException       if eFaps context could not be opened /
     *                              closed
     * @throws InterruptedException if thread could not be slept for 1s
     */
    @Test(description = "multi threading fetching numbers from database sequences",
          threadPoolSize = 50,
          invocationCount = 2000,
          timeOut = 100000,
          sequential = false)
    public void testMultiThreadingFetching()
        throws SQLException, EFapsException, InterruptedException
    {
        Context.begin();
        try {
            final long number = Context.getDbType().nextSequence(Context.getThreadContext().getConnection(),
                                                                 SequencesTest.TEST_SEQUENCE);
            synchronized (this.numbers)  {
                Assert.assertFalse(this.numbers.contains(number), "number is not fetched before");
                this.numbers.add(number);
            }
            Thread.sleep(1000);
        } finally  {
            Context.commit();
        }
    }

    /**
     * Defines new number 20000 for the sequence and then fetches the first
     * number for a check.
     *
     * @throws SQLException         if new sequence number could not be defined
     * @throws EFapsException       if eFaps context could not be opened /
     *                              closed
     */
    @Test(description = "sets the sequence to new number '20000' and tests the first result")
    public void testDefiningNewNumber()
        throws SQLException, EFapsException
    {
        Context.begin();
        try {
            Context.getDbType().setSequence(Context.getThreadContext().getConnection(),
                                            SequencesTest.TEST_SEQUENCE,
                                            20000);
            final long number = Context.getDbType().nextSequence(Context.getThreadContext().getConnection(),
                    SequencesTest.TEST_SEQUENCE);

            Assert.assertEquals(number, 20000, "check that returned number is correct");
        } finally  {
            Context.commit();
        }
    }
}
