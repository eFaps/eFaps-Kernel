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

import java.io.IOException;

import com.googlecode.jsvnserve.SVNServer;


/**
 * TODO comment!
 *
 * @author Jan Moxter
 * @version $Id$
 */
public class SVNProxyServer extends Thread {

  private static SVNProxyServer SERVER;

  /**
   * Private Constructor to provide a singleton.
   */
  private SVNProxyServer() {
  }

  @Override
  public void start() {
    try {
      final SVNServer svnServer = new SVNServer();
      svnServer.setPort(9999);
      svnServer.setRepositoryFactory(new RepositoryFactory());
      svnServer.setCallbackHandler(new LoginHandler());

      svnServer.start();

    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    while (!isInterrupted()) {
      try
      {
       Thread.sleep(500);
      }
      catch ( final InterruptedException e )
      {
       interrupt();
       System.out.println( "Unterbrechung in sleep()" );
      }
    }
  }

  public static SVNProxyServer get() {
    if (SERVER == null) {
      SERVER = new SVNProxyServer();
    }
    return SERVER;
  }
}
