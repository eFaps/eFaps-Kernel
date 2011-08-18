/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.db.store;

import java.io.InputStream;
import java.io.OutputStream;

import javax.transaction.xa.XAResource;

import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * Interface for the StoreResource used to archive files in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface Resource
    extends XAResource
{
    /**
     * Store resource could implement the possibility to compress and uncompress
     * data while writing to and reading from files.
     */
    enum Compress
    {
        /** File should not be compressed.*/
        NONE,
        /** File should be compressed using zip.*/
        ZIP,
        /** File should  be compressed using gzip.*/
        GZIP
    };

    /**
     * Writes the file with the given input stream.
     *
     * @param _in       input stream
     * @param _size     size of the data to write (or negative if the size is
     *                  not known)
     * @param _fileName name of the file
     * @return length of the file which is stored
     * @throws EFapsException if an error occurs
     */
    long write(final InputStream _in,
               final long _size,
               final String _fileName)
        throws EFapsException;

    /**
     * Method to open the Resource.
     *
     * @throws EFapsException on error
     */
    void open() throws EFapsException;

    /**
     * Method to commit the Resource.
     *
     * @throws EFapsException on error
     */
    void commit() throws EFapsException;

    /**
     * @return if the Resource was opened.
     */
    boolean isOpened();

    /**
     * Method to open the Resource.
     *
     * @throws EFapsException on error
     */
    void abort() throws EFapsException;

    /**
     * The input stream with the attached content of the object returned.
     *
     * @return input stream with the content of the file
     * @throws EFapsException if an error occurs
     */
    InputStream read() throws EFapsException;

    /**
     * The output stream is written with the content of the file. From method
     * {@link #read()} the input stream is used and copied into the output
     * stream.
     *
     * @param _out    output stream where the file content must be written
     * @throws EFapsException if an error occurs
     */
    void read(final OutputStream _out) throws EFapsException;


    /**
     * Get the name of the file.
     * @return filename
     * @throws EFapsException on error
     */
    String getFileName() throws EFapsException;

    /**
     * Get the Length of the file in byte.
     * @return filelength
     * @throws EFapsException on error
     */
    Long getFileLength() throws EFapsException;

    /**
     * Will delete the file.
     *
     * @throws EFapsException if an error occurs
     */
    void delete() throws EFapsException;

    /**
     * Method called to initialize this StoreResource.
     * @see org.efaps.db.store.Resource#initialize(org.efaps.db.Instance,
     *  java.util.Map, org.efaps.db.store.Resource.Compress)
     * @param _instance     Instance of the object this StoreResource is wanted
     *                      for
     * @param _store        Store this StoreResource belongs to
     * @throws EFapsException on error
     */
    void initialize(final Instance _instance,
                    final Store _store)
        throws EFapsException;
}
