/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Path("/update")
public class Update
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Update.class);


    /**
     * Called on post to consume files used to update.
     *
     * @param _multiPart Mulitpart containing the update files
     */
    @POST
    @Consumes("multipart/mixed")
    public void updateFromFile(final MultiPart _multiPart)
    {
        try {
            Update.LOG.info("===Start of Update via REST==");
            //Admin_REST
            if (Context.getThreadContext().getPerson().isAssigned(Role.get(
                            UUID.fromString("2d142645-140d-46ad-af67-835161a8d732")))) {

                final File temp = File.createTempFile("eFaps", ".tmp");

                final File tmpfld = temp.getParentFile();
                temp.delete();
                final File updateFolder = new File(tmpfld, "eFapsUpdate");
                if (!updateFolder.exists()) {
                    updateFolder.mkdirs();
                }
                final File dateFolder = new File(updateFolder, ((Long) new Date().getTime()).toString());
                dateFolder.mkdirs();

                final Map<File, FileType> files = new TreeMap<File, FileType>();
                for (final BodyPart part : _multiPart.getBodyParts()) {
                    final BodyPartEntity entity = (BodyPartEntity) part.getEntity();
                    final InputStream in = entity.getInputStream();

                    final File file = new File(dateFolder, part.getContentDisposition().getFileName());

                    final FileOutputStream out = new FileOutputStream(file);
                    final byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();

                    final String ending = file.getName().substring(file.getName().lastIndexOf(".") + 1);

                    final FileType filetype = FileType.getFileTypeByExensione(ending);

                    Update.LOG.info("= Receieved: " + file.getName());
                    if (filetype != null) {
                        files.put(file, filetype);
                    }
                }

                for (final Entry<File, FileType> entry : files.entrySet()) {
                    Update.LOG.info("...Updating " + entry.getKey().getName());
                    final Install install = new Install();
                    install.addFile(entry.getKey().toURI().toURL(), entry.getValue().getType());
                    install.updateLatest();
                }
                Update.LOG.info("===End of Update via REST==");
            }
        } catch (final IOException e) {
            Update.LOG.error("IOException", e);
        } catch (final InstallationException e) {
            Update.LOG.error("InstallationException", e);
        } catch (final CacheReloadException e) {
            Update.LOG.error("CacheReloadException", e);
        } catch (final EFapsException e) {
            Update.LOG.error("EFapsException", e);
        }
    }

    @GET
    public String test()
    {
        return "not implemented yet";
    }
}
