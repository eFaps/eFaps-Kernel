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

package org.efaps.update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.efaps.update.schema.program.BPMUpdate;
import org.efaps.update.schema.program.CSSUpdate;
import org.efaps.update.schema.program.JasperReportUpdate;
import org.efaps.update.schema.program.JavaScriptUpdate;
import org.efaps.update.schema.program.JavaUpdate;
import org.efaps.update.schema.program.WikiUpdate;
import org.efaps.update.schema.program.XSLUpdate;

/**
 * Main File type definition for the import and update of files.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public enum FileType {

    /** Java Source file. */
    BPM("bpmn", "bpmn2"),
    /** Java Source file. */
    JAVA("source-java", "java"),
    /** Java Script file. */
    JS("source-js", "js"),
    /** JasperReport Source file. */
    JRXML("jasperreport", "jrxml"),
    /** CSS Source file. */
    CSS("source-css", "css"),
    /** Wiki Source file. */
    WIKI("source-wiki", "wiki"),
    /** XML Source file. */
    XML("install-xml", "xml"),
    /** XSL Source file. */
    XSL("source-xsl", "xsl", "xslt");

    /**
     * Class used for mapping.
     */
    private static final class Mapper
    {
        /**
         * Mapping between an extensions and the related file type.
         */
        private static final Map<String, FileType> EXT2FILETYPE = new HashMap<String, FileType>();

        /**
         * Mapping between a type and the related file type.
         */
        private static final Map<String, FileType> TYPE2FILETYPE = new HashMap<String, FileType>();

        /**
         * hidden Constructor.
         */
        private Mapper()
        {
        }
    }

    /**
     * Internal type of the file (used e.g. from the eFaps Maven installer).
     */
    private final String type;


    /**
     * All extensions of this file type.
     */
    private final Set<String> extensions = new HashSet<String>();

    /**
     * Set of all update classes.
     */
    private final Set<Class<? extends AbstractUpdate>> clazzes = new HashSet<Class<? extends AbstractUpdate>>();


    /**
     * File type enum constructor.
     *
     * @param _type file type
     * @param _extensions extensions of the file type
     */
    private FileType(final String _type,
                     final String... _extensions)
    {
        this.type = _type;
        for (final String extension : _extensions) {
            this.extensions.add(extension);
            FileType.Mapper.EXT2FILETYPE.put(extension, this);
        }
        FileType.Mapper.TYPE2FILETYPE.put(_type, this);
        if ("source-java".equals(_type)) {
            this.clazzes.add(JavaUpdate.class);
        } else if ("source-js".equals(_type)) {
            this.clazzes.add(JavaScriptUpdate.class);
        } else if ("source-css".equals(_type)) {
            this.clazzes.add(CSSUpdate.class);
        } else if ("source-xsl".equals(_type)) {
            this.clazzes.add(XSLUpdate.class);
        } else if ("jasperreport".equals(_type)) {
            this.clazzes.add(JasperReportUpdate.class);
        } else if ("source-wiki".equals(_type)) {
            this.clazzes.add(WikiUpdate.class);
        } else if ("bpmn".equals(_type)) {
            this.clazzes.add(BPMUpdate.class);
        }
    }

    /**
     * Getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * Getter method for the instance variable {@link #clazzes}.
     *
     * @return value of instance variable {@link #clazzes}
     */
    public Set<Class<? extends AbstractUpdate>> getClazzes()
    {
        return this.clazzes;
    }

    /**
     * Depending on the extension the file type is returned.
     *
     * @param _extension extension for which the file type is searched
     * @return file type instance for given extension
     */
    public static FileType getFileTypeByExtension(final String _extension)
    {
        return FileType.Mapper.EXT2FILETYPE.get(_extension);
    }

    /**
     * Depending on the type the file type is returned.
     *
     * @param _type type for which the file type is searched
     * @return file type instance for given type
     */
    public static FileType getFileTypeByType(final String _type)
    {
        return FileType.Mapper.TYPE2FILETYPE.get(_type);
    }
}
