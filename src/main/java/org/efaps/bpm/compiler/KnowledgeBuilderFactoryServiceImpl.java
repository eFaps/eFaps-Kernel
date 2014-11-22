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

import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.rule.builder.dialect.java.JavaDialectConfiguration;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.util.EFapsException;
import org.kie.internal.builder.conf.ClassLoaderCacheOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: KnowledgeBuilderFactoryServiceImpl.java 11598 2014-01-07
 *          00:02:12Z jan@moxter.net $
 */
public class KnowledgeBuilderFactoryServiceImpl
    extends org.drools.compiler.builder.impl.KnowledgeBuilderFactoryServiceImpl
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(KnowledgeBuilderFactoryServiceImpl.class);

    @Override
    public KnowledgeBuilder newKnowledgeBuilder()
    {
        String level = null;
        try {
            level = EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.BPM_COMPILERLEVEL);
        } catch (final EFapsException e) {
            KnowledgeBuilderFactoryServiceImpl.LOG.error("Catched error on retireving SystemConfiguration", e);
        }
        // set compiler to eclipse
        final Properties knowledgeBldrProps = new Properties();
        knowledgeBldrProps.setProperty(JavaDialectConfiguration.JAVA_COMPILER_PROPERTY, "ECLIPSE");
        knowledgeBldrProps.setProperty("drools.dialect.java.compiler.lnglevel", level == null ? "1.7" : level);
        knowledgeBldrProps.setProperty(ClassLoaderCacheOption.PROPERTY_NAME, "false");

        final KnowledgeBuilderConfigurationImpl conf = new KnowledgeBuilderConfigurationImpl(knowledgeBldrProps,
                        EFapsClassLoader.getInstance());

        return new KnowledgeBuilder(conf);
    }
}
