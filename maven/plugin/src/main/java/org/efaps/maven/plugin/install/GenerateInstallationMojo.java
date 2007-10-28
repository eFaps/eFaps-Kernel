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
package org.efaps.maven.plugin.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
@MojoGoal("generate-installation")
@MojoRequiresDependencyResolution("compile")
public class GenerateInstallationMojo extends AbstractEFapsInstallMojo {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Tag name of the application.
   */
  private final static String TAG_APPLICATION = "application";

  /**
   * Tag name of the install.
   */
  private final static String TAG_INSTALL = "install";

  /**
   * List of includes used to copy files.
   *
   * @see #getCopyFiles()
   */
  @MojoParameter()
  private List<String> copyIncludes = null;

  /**
   * List of excludes used to copy files.
   *
   * @see #getCopyFiles()
   */
  @MojoParameter()
  private List<String> copyExcludes = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Target directory where the eFaps installation files are copied. Default is
   * the classes directory so that the Maven standard jar goal could pack the
   * eFaps installation files.
   */
  @MojoParameter(expression = "${basedir}/target/classes")
  private File targetDirectory;

  /**
   * Name of the XML installation file (within the target directory).
   */
  @MojoParameter(defaultValue = "META-INF/efaps/install.xml")
  private String targetInstallFile;

  /**
   * Encoding of the target XML installation file.
   */
  @MojoParameter(defaultValue = "UTF-8")
  private String targetEncoding;

  /**
   * Name of the root package where the installation is copied.
   */
  @MojoParameter(defaultValue = "org/efaps/installations/applications")
  private String rootPackage;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Generates the installation XML file and copies all eFaps definition
   * installation files.
   *
   * @see #generateInstallFile(Collection)
   * @see #copyFiles(String, Collection)
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    final String rootPackage = generateInstallFile();
    copyFiles(rootPackage);
  }

  /**
   * Copy all eFaps installation files from the eFaps root directory in the
   * related target classes directory.
   *
   * @param _rootPackage  root package include application sub directory
   * @param _files        files to copy (from the source directory in the
   *                      in the target directory with the root package)
   * @throws MojoExecutionException if some files could not be copied
   * @see AbstractEFapsInstallMojo#getEFapsDir()  to get source directory
   * @see #targetDirectory                        to get target directory
   * @see #getCopyFiles()                         get all files to copy
   */
  protected void copyFiles(final String _rootPackage)
  throws MojoExecutionException  {
    try {
      for (final String fileName : getCopyFiles())  {
        final File srcFile = new File(getEFapsDir(), fileName);
        final File dstFile = new File(targetDirectory, _rootPackage + fileName);
          FileUtils.copyFile(srcFile, dstFile, true);
      }
    } catch (IOException e) {
      throw new MojoExecutionException("could not copy files", e);
   }
  }

  /**
   * Generates the installation XML file:
   * <ul>
   * <li>get the version XML file from
   *     {@link AbstractEFapsInstallMojo#getVersionFile()}</li>
   * <li>append all files from the file set (parameter _files)</li>
   * <li>store the new XML installation XML file
   *     ({@link #targetInstallFile})</li>
   * </ul>
   *
   * @return root package path
   * @throws MojoExecutionException if version file could not read or
   *                                interpreted
   * @throws MojoFailureException   if application name in version file is not
   *                                given
   * @see AbstractEFapsInstallMojo#getVersionFile() to get the version file
   *                                                (defining all versions to
   *                                                install)
   * @see #targetDirectory                          target directory
   * @see #targetInstallFile                        name and path of the
   *                                                installation XML file in
   *                                                the target directory
   */
  protected String generateInstallFile() throws MojoExecutionException, MojoFailureException  {
    try {
      final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
  
      // read version file
      final Document doc = docBuilder.parse(getVersionFile());
  
      // get install node
      final NodeList nodeList = doc.getElementsByTagName(TAG_INSTALL);
      final Node installNode = nodeList.item(0);

      // get application name
      final NodeList subNodeList = installNode.getChildNodes();
      String application = null;
      for (int idx = 0; idx < subNodeList.getLength(); idx++)  {
        final Node subNode = subNodeList.item(idx);
        if ((Node.ELEMENT_NODE == subNode.getNodeType()) 
            && TAG_APPLICATION.equals(subNode.getNodeName()))  {
          final Node subSubNode = subNode.getFirstChild();
          if (Node.TEXT_NODE == subSubNode.getNodeType())  {
            application = subSubNode.getNodeValue();
          }
          break;
        }
      }

      // test if application name is given
      if (application == null)  {
        throw new MojoFailureException("Application name in '" 
                                       + getVersionFile() + "' not given");
      }
      application = application.trim();

      // prepare root package name
      final String rootPackage = this.rootPackage.replaceAll("/*$", "")
                                                 .replaceAll("^/*", "")
                                 + "/" + application + "/";

      // create files node and append to install node
      final Node files = doc.createElement("files");
      installNode.appendChild(files);
  
      // append all file name to files node (sorted alphabetical)
      final Set<String> filesSet = new TreeSet<String>(getFiles()); 
      for (final String fileName : filesSet)  {
        final Node file = doc.createElement("file");
        final Attr fileAttr = doc.createAttribute("name");
        fileAttr.setValue(rootPackage + fileName);
        file.getAttributes().setNamedItem(fileAttr);
        files.appendChild(file);
      }

      // prepare file install of the target install file
      final File targetInstallFile = new File(this.targetDirectory,
                                              this.targetInstallFile);

      // get parent directory of target installation file
      // and create directories (if needed)
      final File parentDir = targetInstallFile.getParentFile();
      if (!parentDir.exists())  {
        parentDir.mkdirs();
      }

      // open transformer (to convert XML in memory to a stream).
      final Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
  
      // initialize StreamResult with File object to save to file
      // flush output stream and write to file (and close file)
      final OutputStream os = new FileOutputStream(targetInstallFile);
      final StreamResult result = new StreamResult(new OutputStreamWriter(os, this.targetEncoding));
      final DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);
      os.flush();
      os.close();

      return rootPackage;
    } catch (MojoFailureException e)  {
      throw e;
    } catch (Exception e) {
      throw new MojoExecutionException("could not create target XML "
                                       + "installation file", e);
    }
  }

  /**
   * Uses the {@link #copyIncludes} and {@link #copyExcludes} together with the
   * root directory {@link AbstractEFapsInstallMojo#getEFapsDir()} to get all 
   * related and matched files. The files are used to idendtify which are
   * copied in the target directory.
   *
   * @see #copyIncludes defines includes; if not specified by maven, the
   *                    default value is <code>**&#x002f;*.xml</code>,
   *                    <code>**&#x002f;*.gif</code>,
   *                    <code>**&#x002f;*.png</code>,
   *                    <code>**&#x002f;*.java</code>,
   *                    <code>**&#x002f;*.js</code> and
   *                    <code>**&#x002f;*.properties</code>.
   * @see #copyExcludes defines excludes; if not specified by maven , the
   *                    default value is <code>**&#x002f;version.xml</code>
   * @see FileSet
   */
  protected Collection<String> getCopyFiles()  {
    FileSet fileSet = new FileSet();
    fileSet.setRootDirectory(getEFapsDir().toString());
    if (this.copyIncludes == null)  {
      fileSet.addInclude("**/*.xml");
      fileSet.addInclude("**/*.gif");
      fileSet.addInclude("**/*.png");
      fileSet.addInclude("**/*.java");
      fileSet.addInclude("**/*.js");
      fileSet.addInclude("**/*.properties");
    } else  {
      fileSet.addIncludes(this.copyIncludes);
    }
    if (this.copyExcludes == null)  {
      fileSet.addExclude("**/versions.xml");
    } else  {
      fileSet.addExcludes(this.copyExcludes);
    }
    return fileSet.getFiles();
  }
}
