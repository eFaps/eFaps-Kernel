/*
 * Copyright 2003-2007 The eFaps Team
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
import org.jfrog.maven.annomojo.annotations.MojoExecute;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDirectInvocation;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;

import org.efaps.maven.plugin.goal.EFapsAbstractMojo;

/**
 * The goal starts the Jetty web server.
 * 
 * @author tmo
 * @version $Id$
 */
@MojoGoal("run")
@MojoRequiresDependencyResolution("compile")
@MojoRequiresDirectInvocation
@MojoExecute(phase="install")
public class JettyRunMojo extends EFapsAbstractMojo {

  /**
   * Defines the Port on which the Jetty is started. Default value is
   * <i>8888</i>.
   */
  @MojoParameter(defaultValue = "8888")
  private int port;
  
  /**
   * Defines the Host (Adapter) on which the jetty is started. Default value
   * is <i>localhost</i>.
   */
  @MojoParameter(defaultValue = "127.0.0.1")
  private String host;

  /**
   * 
   */
  @MojoParameter(required = true)
  private String jaasConfigFile;

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

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    server.setHandler(contexts);
    
    System.setProperty("java.security.auth.login.config", 
                       this.jaasConfigFile);
    
    Context handler = new Context(contexts,"/eFaps", Context.SESSIONS);
    
    FilterHolder filter = new FilterHolder();
    filter.setName("eFaps");
    filter.setClassName("org.apache.wicket.protocol.http.WicketFilter");
    filter.setInitParameter("applicationClassName", "org.efaps.webapp.EFapsApplication");
    handler.addFilter(filter, "/*", 1);

    filter = new FilterHolder();
    filter.setName("eFapsTransactionFilter");
    filter.setClassName("org.efaps.webapp.filter.TransactionFilter");
    handler.addFilter(filter, "/*", 1);
    
    ServletHolder servletHolder = new ServletHolder();
    servletHolder.setName("eFapsServlet");
    servletHolder.setDisplayName("eFapsServlet");
    servletHolder.setClassName("org.efaps.servlet.RequestHandler");
    servletHolder.setInitOrder(1);
    handler.addServlet(servletHolder,"/*");

    servletHolder = new ServletHolder();
    servletHolder.setName("eFapsImageServlet");
    servletHolder.setDisplayName("eFapsImageServlet");
    servletHolder.setClassName("org.efaps.servlet.ImageServlet");
    handler.addServlet(servletHolder,"/servlet/image/*");


    try {
      getLog().info("Starting Server");
      server.start();
      getLog().info("Server Started");
      server.join();
    } catch (Exception e) {
      throw new MojoExecutionException("Could not Start Jetty Server", e);
    }
  }
  
}
