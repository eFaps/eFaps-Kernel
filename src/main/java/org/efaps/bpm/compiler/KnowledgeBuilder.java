/*
 * Copyright 2003 - 2013 The eFaps Team
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


package org.efaps.bpm.compiler;

import java.util.Properties;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.PackageBuilder;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.kie.api.KieBaseConfiguration;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class KnowledgeBuilder
    extends KnowledgeBuilderImpl
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(KnowledgeBuilder.class);

    /**
     * @param _pkgBuilder package builder
     */
    public KnowledgeBuilder(final PackageBuilder _pkgBuilder)
    {
        super(_pkgBuilder);
    }

    @SuppressWarnings("deprecation")
    @Override
    public KnowledgeBase newKnowledgeBase()
    {
        final KnowledgeBuilderErrors errors = getErrors();
        if (errors.size() > 0) {
            for (final KnowledgeBuilderError error : errors) {
                KnowledgeBuilder.LOG.error(error.toString());
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        final KieBaseConfiguration conf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(new Properties(),
                        EFapsClassLoader.getInstance());
        final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(conf);
        kbase.addKnowledgePackages(getKnowledgePackages());
        return kbase;
    }
}
