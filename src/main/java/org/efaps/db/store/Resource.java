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
     * The store resource could handle three different
     * events:
     * <ul>
     * <li>delete</li>
     * <li>write</li>
     * <li>read</li>
     * </ul>
     * These three events are defined here and set in instance variable
     * {@link #storeEvent}.
     */
    enum StoreEvent
    {
        /** delete. */
        DELETE,
        /** write. */
        WRITE,
        /** read. */
        READ,
        /** not known yet.    **/
        UNKNOWN;
    };

    /**
     * Method to open the Resource.
     *
     * @param _event Event the store is opened for
     * @throws EFapsException on error
     */
    void open(StoreEvent _event) throws EFapsException;

    /**
     * The input stream with the attached content of the object returned.
     *
     * @return input stream with the content of the file
     * @throws EFapsException if an error occurs
     */
    InputStream read() throws EFapsException;

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
    long write(InputStream _in,
               long _size,
               String _fileName)
        throws EFapsException;

    /**
     * Will delete the file.
     *
     * @throws EFapsException if an error occurs
     */
    void delete() throws EFapsException;

    /**
     * @return if the Resource was opened.
     */
    boolean isOpened();

    /**
     * The output stream is written with the content of the file. From method
     * {@link #read()} the input stream is used and copied into the output
     * stream.
     *
     * @param _out    output stream where the file content must be written
     * @throws EFapsException if an error occurs
     */
    void read(OutputStream _out) throws EFapsException;

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
     * Method called to initialize this StoreResource.
     * @see org.efaps.db.store.Resource#initialize(org.efaps.db.Instance,
     *  java.util.Map, org.efaps.db.store.Resource.Compress)
     * @param _instance     Instance of the object this StoreResource is wanted
     *                      for
     * @param _store        Store this StoreResource belongs to
     * @throws EFapsException on error
     */
    void initialize(Instance _instance,
                    Store _store)
        throws EFapsException;

    /**
     * Check if the Resource exists. Gives the possibility to check before
     * reading. deleting etc.
     *
     * @return true, if successful
     * @throws EFapsException on error
     */
    boolean exists()
        throws EFapsException;;
}
