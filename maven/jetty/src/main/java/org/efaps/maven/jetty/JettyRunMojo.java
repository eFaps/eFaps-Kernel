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

package org.efaps.maven.jetty;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;

import org.efaps.maven.jetty.configuration.ServerDefinition;
import org.efaps.maven.plugin.EFapsAbstractMojo;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Execute;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Parameter;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.lifecycle.Phase;

/**
 * The goal starts the Jetty web server.
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
@Goal(name = "run",
      requiresDependencyResolutionScope = "compile",
      requiresDirectInvocation = true)
@Execute(phase = Phase.INSTALL)
public class JettyRunMojo extends EFapsAbstractMojo {

  /**
   * Defines the Port on which the Jetty is started. Default value is
   * <i>8888</i>.
   */
  @Parameter(defaultValue = "8888")
  private int port;

  /**
   * Defines the Host (Adapter) on which the jetty is started. Default value
   * is <i>localhost</i>.
   */
  @Parameter(defaultValue = "127.0.0.1")
  private String host;

  /**
   *
   */
  @Parameter(required = true)
  private String jaasConfigFile;

  /**
   *
   */
  @Parameter(required = true)
  private String configFile;

  /**
   *
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    init();

    final Server server = new Server();

    getLog().info("Starting jetty Version "
                  + server.getClass().getPackage().getImplementationVersion());

    final Connector connector = new SelectChannelConnector();
    connector.setPort(this.port);
    connector.setHost(this.host);
    server.addConnector(connector);

    final ContextHandlerCollection contexts = new ContextHandlerCollection();
    server.setHandler(contexts);

    System.setProperty("java.security.auth.login.config",
                       this.jaasConfigFile);

    final Context handler = new Context(contexts,"/eFaps", Context.SESSIONS);

    final ServerDefinition serverDef = ServerDefinition.read(this.configFile);
    serverDef.updateServer(handler);

    try {
      getLog().info("Starting Server");
      server.start();
      getLog().info("Server Started");
      server.join();
    } catch (final Exception e) {
      throw new MojoExecutionException("Could not Start Jetty Server", e);
    }
  }

}
