/*
 * Copyright 2003 - 2023 The eFaps Team
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

package org.efaps.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.efaps.admin.AppConfigHandler;
import org.efaps.update.FileType;
import org.efaps.update.Install;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.joda.time.DateTime;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

/**
 * Rest API to update files in eFaps.
 *
 * @author The eFaps Team
 *
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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void updateFromFile(final FormDataMultiPart _multiPart)
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

                final List<InstallFile> installFiles = new ArrayList<>();

                final Iterator<FormDataBodyPart> revPartsIter = _multiPart.getFields("eFaps_Revision").iterator();
                final Iterator<FormDataBodyPart> datePartsIter = _multiPart.getFields("eFaps_Date").iterator();

                for (final FormDataBodyPart filePart : _multiPart.getFields("eFaps_File")) {
                    final FormDataBodyPart revPart = revPartsIter.next();
                    final FormDataBodyPart datePart = datePartsIter.next();

                    final BodyPartEntity entity = (BodyPartEntity) filePart.getEntity();
                    final InputStream in = entity.getInputStream();

                    final File file = new File(dateFolder, filePart.getContentDisposition().getFileName());

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
                        final InstallFile installFile = new InstallFile().setName(file.getName())
                                        .setURL(file.toURI().toURL()).setType(filetype.getType());
                        if (revPart.getValue() != null && !revPart.getValue().isEmpty()) {
                            installFile.setRevision(revPart.getValue());
                        }
                        if (datePart.getValue() != null && !datePart.getValue().isEmpty()) {
                            installFile.setDate(new DateTime(datePart.getValue()));
                        }
                        installFiles.add(installFile);
                    }
                }
                Collections.sort(installFiles, Comparator.comparing(InstallFile::getName));

                if (!installFiles.isEmpty()) {
                    final Install install = new Install(true);
                    for (final InstallFile installFile : installFiles) {
                        AbstractRest.LOG.info("...Adding to Update: '{}' ", installFile.getName());
                        install.addFile(installFile);
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
