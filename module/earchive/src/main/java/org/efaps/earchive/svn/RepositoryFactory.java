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

import org.tmatesoft.svn.core.SVNException;

import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.IRepositoryFactory;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class RepositoryFactory
        implements IRepositoryFactory
{

    /**
     * Name of the Repository.
     */
    private final String repositoryName;

    public RepositoryFactory(final String _repositoryName) throws SVNException
    {
      this.repositoryName = _repositoryName;
//        this.svnURL = SVNURL.parseURIDecoded(_svnURI);
//        SVNRepositoryFactoryImpl.setup();
//        FSRepositoryFactory.setup();
    }

    public IRepository createRepository(final String _user, final String _path)
    {
        try {
            return new EFapsRepository(_user, "/proxy", _path.substring("/proxy".length()), this.repositoryName);
        } catch (final SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
