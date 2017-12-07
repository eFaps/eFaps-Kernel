/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.db.stmt.runner;

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PrintRunner.
 */
public final class StmtRunner
{
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(StmtRunner.class);

    /** The eqlrunners. */
    private static List<Class<? extends IEQLRunner>> EQLRUNNERS = new ArrayList<>();

    /** The instance. */
    private static StmtRunner RUNNER;

    /**
     * Instantiates a new stmt runner.
     */
    private StmtRunner()
    {
    }

    /**
     * Gets the.
     *
     * @return the stmt runner
     */
    public static StmtRunner get()
    {
        if (StmtRunner.RUNNER == null) {
            StmtRunner.RUNNER = new StmtRunner();
            EQLRUNNERS.add(SQLRunner.class);
        }
        return StmtRunner.RUNNER;
    }

    /**
     * Execute.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    public void execute(final ObjectPrint _print)
        throws EFapsException
    {
        try {
            final List<IEQLRunner> instances = new ArrayList<>();
            for (final Class<? extends IEQLRunner> clazz : EQLRUNNERS) {
                final IEQLRunner runner = clazz.newInstance();
                instances.add(runner);
                runner.prepare(_print);
            }
            for (final IEQLRunner runner : instances) {
                runner.execute();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Problems while instantiating", e);
        }
    }
}
