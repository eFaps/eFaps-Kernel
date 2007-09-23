/*
 * * Copyright 2003-2007 The eFaps Team
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

package org.efaps.maven.run;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tmo
 * @version $Id$
 */
public class MavenLoggerOverSLF4J implements Log {

  final Logger logger = LoggerFactory.getLogger(MavenLoggerOverSLF4J.class);

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#debug(java.lang.CharSequence)
   */
  public void debug(final CharSequence _text) {
    this.logger.debug(_text.toString());
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#debug(java.lang.Throwable)
   */
  public void debug(final Throwable _e) {
    this.logger.debug("", _e);
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#debug(java.lang.CharSequence, java.lang.Throwable)
   */
  public void debug(final CharSequence _text, final Throwable _e) {
    this.logger.debug(_text.toString(), _e);
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#error(java.lang.CharSequence)
   */
  public void error(final CharSequence _text) {
    this.logger.error(_text.toString());
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#error(java.lang.Throwable)
   */
  public void error(final Throwable _e) {
    this.logger.error("", _e);
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#error(java.lang.CharSequence, java.lang.Throwable)
   */
  public void error(final CharSequence _text, final Throwable _e) {
    this.logger.error(_text.toString(), _e);
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#info(java.lang.CharSequence)
   */
  public void info(final CharSequence _text) {
    System.out.println(""+_text);
   this.logger.info(_text.toString());
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#info(java.lang.Throwable)
   */
  public void info(final Throwable _e)  {
    this.logger.info("", _e);
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#info(java.lang.CharSequence, java.lang.Throwable)
   */
  public void info(final CharSequence _text, final Throwable _e)  {
System.out.println(""+_text);
    this.logger.info(_text.toString(), _e);
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#isDebugEnabled()
   */
  public boolean isDebugEnabled()  {
    return this.logger.isDebugEnabled();
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#isErrorEnabled()
   */
  public boolean isErrorEnabled() {
    return this.logger.isErrorEnabled();
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#isInfoEnabled()
   */
  public boolean isInfoEnabled() {
//    return this.logger.isInfoEnabled();
return true;
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#isWarnEnabled()
   */
  public boolean isWarnEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#warn(java.lang.CharSequence)
   */
  public void warn(CharSequence _arg0) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#warn(java.lang.Throwable)
   */
  public void warn(Throwable _arg0) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.logging.Log#warn(java.lang.CharSequence, java.lang.Throwable)
   */
  public void warn(CharSequence _arg0, Throwable _arg1) {
    // TODO Auto-generated method stub
    
  }
  
}
