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

package org.efaps.earchive.svn;

import org.efaps.util.EFapsException;

import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.IRepositoryFactory;
import com.googlecode.jsvnserve.api.ServerException;

/**
 *
 * @author Jan Moxter
 * @version $Id$
 */
public class RepositoryFactory implements IRepositoryFactory {

  public IRepository createRepository(final String _user, final String _path)
      throws ServerException{
    try {
      return new EFapsRepository(_user, _path);
    } catch (final EFapsException e) {
      throw new ServerException(e.getMessage());
    }
  }
}
