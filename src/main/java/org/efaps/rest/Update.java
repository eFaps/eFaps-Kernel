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

package org.efaps.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.efaps.admin.AppConfigHandler;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartMediaTypes;

/**
 * Rest API to update files in eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Path("/update")
public class Update
    extends AbstractRest
{
    /**
     * Name of the folder inside the "official" temporary folder.
     */
    public static final String TMPFOLDERNAME = "eFapsUpdate";

    /**
     * Called on post to consume files used to update.
     *
     * @param _multiPart Mulitpart containing the update files
     */
    @POST
    @Consumes(MultiPartMediaTypes.MULTIPART_MIXED)
    public void updateFromFile(final MultiPart _multiPart)
    {
        try {
            if (hasAccess()) {
                AbstractRest.LOG.info("===Start of Update via REST===");
                File tmpfld = AppConfigHandler.get().getTempFolder();
                if (tmpfld == null) {
                    final File temp = File.createTempFile("eFaps", ".tmp");
                    tmpfld = temp.getParentFile();
                    temp.delete();
                }
                final File updateFolder = new File(tmpfld, Update.TMPFOLDERNAME);
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

                    final FileType filetype = FileType.getFileTypeByExtension(ending);

                    AbstractRest.LOG.info("= Receieved: '{}'", file.getName());
                    if (filetype != null) {
                        files.put(file, filetype);
                    }
                }
                if (!files.isEmpty()) {
                    final Install install = new Install(true);
                    for (final Entry<File, FileType> entry : files.entrySet()) {
                        AbstractRest.LOG.info("...Adding to Update: '{}' ", entry.getKey().getName());
                        install.addFile(new InstallFile().setURL(entry.getKey().toURI().toURL())
                                        .setType(entry.getValue().getType()));
                    }
                    install.updateLatest(null);
                }
                AbstractRest.LOG.info("===End of Update via REST===");
            }
        } catch (final IOException e) {
            AbstractRest.LOG.error("IOException", e);
        } catch (final InstallationException e) {
            AbstractRest.LOG.error("InstallationException", e);
        } catch (final CacheReloadException e) {
            AbstractRest.LOG.error("CacheReloadException", e);
        } catch (final EFapsException e) {
            AbstractRest.LOG.error("EFapsException", e);
        }
    }

    /**
     * @return not implemented.
     */
    @GET
    public String test()
    {
        String ret = "";
        try {
            if (hasAccess()) {
                ret = "not implemented yet";
            }
        } catch (final CacheReloadException e) {
            AbstractRest.LOG.error("CacheReloadException", e);
        } catch (final EFapsException e) {
            AbstractRest.LOG.error("EFapsException", e);
        }
        return ret;
    }
}
