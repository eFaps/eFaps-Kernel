/*
 * Copyright 2005 The eFaps Team
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

package org.efaps.webdav.method;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;

/**
 * The class implements WebDAV method <i>MKCOL</i> (a new collection is
 * created).
 */
public class MkColMethod extends AbstractMethod  {

  /**
   *
   */
  public void run(final HttpServletRequest _request, final HttpServletResponse _response) throws IOException, ServletException  {
    try  {
      Context context = Context.getThreadContext();

      String[] uri = _request.getPathInfo().split("/");
      Instance instance = getFolderInstance(context, uri.length - 2, uri);

      Insert insert = new Insert(context, "TeamCenter_Folder");
      insert.add(context, "ParentFolder", ""+instance.getId());
      insert.add(context, "Name", uri[uri.length - 1]);
      insert.execute(context);
      insert.close();

    } catch (IOException e)  {
      throw e;
    } catch (ServletException e)  {
      throw e;
    } catch (Throwable e)  {
      throw new ServletException(e);
    }
  }
}