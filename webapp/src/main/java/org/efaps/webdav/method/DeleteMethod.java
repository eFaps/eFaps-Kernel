/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.webdav.method;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.efaps.db.Context;
import org.efaps.db.Delete;
import org.efaps.db.Instance;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class DeleteMethod extends AbstractMethod  {

  /**
   *
   */
  public void run(final HttpServletRequest _request, final HttpServletResponse _response) throws IOException, ServletException  {
    try  {
      Context context = Context.getThreadContext();

      Instance instance = getFolderInstance(context, _request.getPathInfo());

      Delete delete = new Delete(instance);
      delete.execute();

      _response.setStatus(Status.NO_CONTENT.code);

    } catch (IOException e)  {
e.printStackTrace();
      throw e;
    } catch (ServletException e)  {
e.printStackTrace();
      throw e;
    } catch (Throwable e)  {
e.printStackTrace();
      throw new ServletException(e);
    }
  }
}