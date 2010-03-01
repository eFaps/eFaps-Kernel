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
import java.util.List;
import java.util.UUID;

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

    @POST
    @Consumes("multipart/mixed")
    public void updateFromFile(final MultiPart _multiPart)
    {

        try {
            //Admin_REST
            if (Context.getThreadContext().getPerson().isAssigned(Role.get(
                            UUID.fromString("2d142645-140d-46ad-af67-835161a8d732")))) {

                final File temp = File.createTempFile("eFaps", ".tmp");

                final File tmpfld = temp.getParentFile();
                temp.delete();
                final File storeFolder = new File(tmpfld, "eFapsUpdate");
                if (!storeFolder.exists()) {
                    storeFolder.mkdirs();
                }
                final List<BodyPart> parts = _multiPart.getBodyParts();
                for (final BodyPart part : parts) {
                    final BodyPartEntity entity = (BodyPartEntity) part.getEntity();
                    final InputStream in = entity.getInputStream();

                    final File file = new File(storeFolder, part.getContentDisposition().getFileName());

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

                    final Install install = new Install();
                    install.addFile(file.toURI().toURL(), filetype.getType());
                    install.updateLatest();
                }
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InstallationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final CacheReloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @GET
    public String test()
    {
        return "not implemented yet";
    }

}
