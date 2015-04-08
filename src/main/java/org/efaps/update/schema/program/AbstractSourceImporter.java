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

package org.efaps.update.schema.program;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.UUID;

import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIType;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.update.Install.InstallFile;
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
     * Defines the encoding of source code within eFaps.
     *
     * @see #readCode()
     * @see #newCodeInputStream()
     */
    protected static final String ENCODING = "UTF8";

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
     * CIType.
     */
    private final CIType ciType;

    /**
     * Constructor used to read the source code from given URL and extract the
     * class name.
     *
     * @param _type     related eFaps type
     * @param _url      URL to the source code
     * @throws InstallationException on error
     * @see #readCode()
     * @see #evalProgramName()
     * @see #evalUUID()
     * @see #evalRevision()
     */
    public AbstractSourceImporter(final CIType _type,
                                  final InstallFile _installFile)
        throws InstallationException
    {
        this.ciType = _type;
        this.url = _installFile.getUrl();
        readCode();
        this.programName = evalProgramName();
        this.eFapsUUID = evalUUID();
        this.revision = evalRevision();
    }

    /**
     * Read the code from the file defined through {@link #url} with character
     * set {@link #ENCODING}.
     *
     * @throws InstallationException if the source code could not read from URL
     * @see #url
     * @see #ENCODING
     */
    protected void readCode()
        throws InstallationException
    {
        try {
            final char[] buf = new char[1024];

            final InputStream input = getUrl().openStream();

            final Reader reader = new InputStreamReader(input, AbstractSourceImporter.ENCODING);
            int length;
            while ((length = reader.read(buf)) > 0) {
                this.code.append(buf, 0, length);
            }
            reader.close();
        } catch (final IOException e) {
            throw new InstallationException("Could not read source code from url '" + this.url + "'.", e);
        }
    }

    /**
     * This Method extracts the Name from the program.
     *
     * @return Name of the program
     * @throws InstallationException on error
     */
    protected abstract String evalProgramName()
        throws InstallationException;

    /**
     * This Method extracts the UUID from the program.
     *
     * @return UUID of the program
     * @throws InstallationException on error
     */
    protected abstract UUID evalUUID()
        throws InstallationException;;

    /**
     * This Method extracts the Revision from the program.
     *
     * @return Revision of the program
     * @throws InstallationException on error
     */
    protected abstract String evalRevision()
         throws InstallationException;;

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
            // check if type exists. Necessary for first time installations
            if (this.ciType.getType() != null) {
                final QueryBuilder queryBldr = new QueryBuilder(this.ciType);
                queryBldr.addWhereAttrEqValue(CIAdminProgram.Abstract.Name, this.programName);
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                if (query.next()) {
                    instance = query.getCurrentValue();
                }
            }
        } catch (final EFapsException e)  {
            throw new InstallationException("Could not found '" + this.ciType + "' '" + this.programName + "'", e);
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
        final Insert insert;
        try {
            insert = new Insert(this.ciType);
            insert.add("Name", this.programName);
            if (getEFapsUUID() != null) {
                insert.add("UUID", getEFapsUUID().toString());
            }
            insert.execute();
        } catch (final EFapsException e)  {
            throw new InstallationException("Could not create " + this.ciType + " " + getProgramName(), e);
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
            final InputStream is = newCodeInputStream();
            final Checkin checkin = new Checkin(_instance);
            checkin.executeWithoutAccessCheck(getProgramName(), is, is.available());
        } catch (final UnsupportedEncodingException e) {
            throw new InstallationException("Encoding failed for " + this.programName, e);
        } catch (final EFapsException e)  {
            throw new InstallationException("Could not check in " + this.programName, e);
        } catch (final IOException e) {
            throw new InstallationException("Reading from inoutstream faild for " + this.programName, e);
        }
    }

    /**
     * Creates a new byte array input stream for {@link #code} which is encoded
     * in character set {@link #ENCODING}.
     *
     * @return byte array input stream
     * @throws UnsupportedEncodingException if {@link #code} could not be
     *                                      encoded
     * @see #code
     */
    protected InputStream newCodeInputStream()
        throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream(this.code.toString().getBytes(AbstractSourceImporter.ENCODING));
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
     * Getter method for instance variable {@link #url}.
     *
     * @return value for instance variable {@link #url}
     */
    public URL getUrl()
    {
        return this.url;
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
