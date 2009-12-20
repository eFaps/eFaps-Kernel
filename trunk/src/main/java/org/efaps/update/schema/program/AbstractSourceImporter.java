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

package org.efaps.update.schema.program;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.UUID;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Class used to import programs into eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractSourceImporter
{
    /**
     * Defines the encoding of the ESJP source code within eFaps.
     */
    private static final String ENCODING = "UTF8";

    /**
     * Related type of the source.
     */
    private final EFapsClassNames type;

    /**
     * URL of the source file in file system (or in jar, ...).
     */
    private final URL url;

    /**
     * Source code itself.
     */
    private final StringBuilder code = new StringBuilder();

    /**
     * eFaps UUID of the program.
     */
    private final UUID eFapsUUID;

    /**
     * eFaps revision of the program.
     */
    private final String revision;

    /**
     * Name of the program in eFaps.
     *
     * @see #getClassName
     */
    private final String programName;

    /**
     * Constructor used to read the source code from given URL and extract the
     * class name.
     *
     * @param _url url to the ESJP source code
     * @throws InstallationException on error
     * @see #readCode()
     * @see #evalProgramName()
     * @see #evalUUID()
     * @see #evalRevision()
     */
    public AbstractSourceImporter(final EFapsClassNames _type,
                                  final URL _url)
        throws InstallationException
    {
        this.type = _type;
        this.url = _url;
        readCode();
        this.programName = evalProgramName();
        this.eFapsUUID = evalUUID();
        this.revision = evalRevision();
    }

    /**
     * Read the code from the file defined through {@link #url}.
     *
     * @throws InstallationException if the source code could not read from URL
     * @see #url
     */
    protected void readCode()
        throws InstallationException
    {
        try {
            final char[] buf = new char[1024];

            final InputStream input = getUrl().openStream();

            final Reader reader = new InputStreamReader(input);
            int length;
            while ((length = reader.read(buf)) > 0) {
                getCode().append(buf, 0, length);
            }
            reader.close();
        } catch (final IOException e) {
            throw new InstallationException("Could not read ESJP source code from url '" + this.url + "'.", e);
        }
    }

    /**
     * This Method extracts the Name from the program.
     *
     * @return Name of the program
     */
    protected abstract String evalProgramName();

    /**
     * This Method extracts the UUID from the program.
     *
     * @return UUID of the program
     */
    protected abstract UUID evalUUID();

    /**
     * This Method extracts the Revision from the program.
     *
     * @return Revision of the program
     */
    protected abstract String evalRevision();

    /**
     * Import related source code into the eFaps DataBase. If the source code
     * does not exists, the source code is created in eFaps.
     *
     * @throws InstallationException on error
     * @see #searchInstance()
     * @see #createInstance()
     * @see #updateDB(Instance)
     */
    public void execute()
        throws InstallationException
    {
        Instance instance = searchInstance();
        if (instance == null) {
            instance = createInstance();
        }
        updateDB(instance);
    }

    /**
     * Method to search the Instance which is imported.
     *
     * @return Instance of the imported program
     * @throws InstallationException if search failed
     */
    public Instance searchInstance()
        throws InstallationException
    {
        Instance instance = null;

        try {
            final Type esjpType = Type.get(this.type);
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(esjpType.getName());
            query.addWhereExprEqValue("Name", this.programName);
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            if (query.next()) {
                instance = Instance.get((String) query.get("OID"));
            }
            query.close();
        } catch (final EFapsException e)  {
            throw new InstallationException("Could not found '" + this.type + "' '" + this.programName + "'", e);
        }

        return instance;
    }

    /**
     * Creates an instance of a source object in eFaps for given name.
     *
     * @return new created instance
     * @throws InstallationException on error
     * @see #programName
     */
    protected Instance createInstance()
        throws InstallationException
    {
        final Type esjpType = Type.get(this.type);
        final Insert insert;
        try {
            insert = new Insert(esjpType);
            insert.add("Name", this.programName);
            if (getEFapsUUID() != null) {
                insert.add("UUID", getEFapsUUID().toString());
            }
            insert.execute();
        } catch (final EFapsException e)  {
            throw new InstallationException("Could not create " + this.type + " " + getProgramName(), e);
        }
        return insert.getInstance();
    }

    /**
     * Stores the read source code in eFaps. This is done with a check in.
     *
     * @param _instance         instance (object id) of the source code object
     *                          in eFaps
     * @throws InstallationException   if source code in eFaps could not
     *                                 updated or the source code could not encoded
     */
    public void updateDB(final Instance _instance)
        throws InstallationException
    {
        try {
            final InputStream is
                = new ByteArrayInputStream(getCode().toString().getBytes(AbstractSourceImporter.ENCODING));
            final Checkin checkin = new Checkin(_instance);
            checkin.executeWithoutAccessCheck(getProgramName(), is, getCode().length());
        } catch (final UnsupportedEncodingException e) {
            throw new InstallationException("Encoding failed for " + this.programName, e);
        } catch (final EFapsException e)  {
            throw new InstallationException("Could not check in " + this.programName, e);
        }
    }

    /**
     * Getter method for instance variable {@link #url}.
     *
     * @return value for instance variable {@link #url}
     */
    public URL getUrl()
    {
        return this.url;
    }

    /**
     * Getter method for instance variable {@link #code}.
     *
     * @return value for instance variable {@link #code}
     */
    public StringBuilder getCode()
    {
        return this.code;
    }

    /**
     * Getter Method for instance variable {@link #eFapsUUID}.
     *
     * @return value for instance variable {@link #eFapsUUID}
     */
    public UUID getEFapsUUID()
    {
        return this.eFapsUUID;
    }

    /**
     * Getter Method for instance variable {@link #revision}.
     *
     * @return value for instance variable {@link #revision}
     */
    public String getRevision()
    {
        return this.revision;
    }

    /**
     * Getter method for instance variable {@link #programName}.
     *
     * @return value of instance variable className
     * @see #programName
     */
    public final String getProgramName()
    {
        return this.programName;
    }
}
