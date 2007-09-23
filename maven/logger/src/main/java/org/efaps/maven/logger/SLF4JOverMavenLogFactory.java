// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) nonlb radix(10) lradix(10) 
// Source File Name:   Log4jLoggerFactory.java

package org.efaps.maven.logger;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * 
 * @author tmo
 * @version $Id$
 */
public class SLF4JOverMavenLogFactory implements ILoggerFactory {

  final SLF4JOverMavenLog logInstance = new SLF4JOverMavenLog();

  public Logger getLogger(String name) {
    return this.logInstance;
  }
}