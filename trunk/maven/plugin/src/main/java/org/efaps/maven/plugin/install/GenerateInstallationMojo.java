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

package org.efaps.maven.plugin.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
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
import org.apache.tools.ant.DirectoryScanner;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Parameter;

/**
 * @author The eFaps Team
 */
@Goal(name = "generate-installation",
      requiresDependencyResolutionScope = "compile")
public class GenerateInstallationMojo
    extends AbstractEFapsInstallMojo
{
    /**
     * Tag name of the application.
     */
    private static final String TAG_APPLICATION = "application";

    /**
     * Tag name of the install.
     */
    private static final String TAG_INSTALL = "install";

    /**
     * Default list of includes used to evaluate the files to copy.
     *
     * @see #getCopyFiles
     */
    private static final Set<String> DEFAULT_COPYINCLUDES = new HashSet<String>();
    static {
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.css");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.gif");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.java");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.js");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.jrxml");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.png");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.properties");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.xml");
        GenerateInstallationMojo.DEFAULT_COPYINCLUDES.add("**/*.xsl");
    }

    /**
     * Default list of excludes used to evaluate the files to copy.
     *
     * @see #getCopyFiles
     */
    private static final Set<String> DEFAULT_COPYEXCLUDES = new HashSet<String>();
    static {
        GenerateInstallationMojo.DEFAULT_COPYEXCLUDES.add("**/versions.xml");
    }

    /**
     * List of includes used to copy files.
     *
     * @see #getCopyFiles()
     */
    @Parameter
    private final List<String> copyIncludes = null;

    /**
     * List of excludes used to copy files.
     *
     * @see #getCopyFiles()
     */
    @Parameter
    private final List<String> copyExcludes = null;

    /**
     * Target directory where the eFaps installation files are copied. Default
     * is the classes directory so that the Maven standard jar goal could pack
     * the eFaps installation files.
     */
    @Parameter(expression = "${basedir}/target/classes")
    private File targetDirectory;

    /**
     * Name of the XML installation file (within the target directory).
     */
    @Parameter(defaultValue = "META-INF/efaps/install.xml")
    private String targetInstallFile;

    /**
     * Encoding of the target XML installation file.
     */
    @Parameter(defaultValue = "UTF-8")
    private String targetEncoding;

    /**
     * Name of the root package where the installation is copied.
     */
    @Parameter(defaultValue = "org/efaps/installations/applications")
    private String rootPackage;

    /**
     * Generates the installation XML file and copies all eFaps definition
     * installation files.
     *
     * @see #generateInstallFile()
     * @see #copyFiles(String)
     * @throws MojoExecutionException on error
     * @throws MojoFailureException on error
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        copyFiles(generateInstallFile());
    }

    /**
     * Generates the installation XML file.
     * <ul>
     * <li>get the version XML file from
     * {@link AbstractEFapsInstallMojo#getVersionFile()}</li>
     * <li>append all files from the file set (parameter _files)</li>
     * <li>store the new XML installation XML file ({@link #targetInstallFile})</li>
     * </ul>
     *
     * @return root package path
     * @throws MojoExecutionException if version file could not read or
     *             interpreted
     * @throws MojoFailureException if application name in version file is not
     *             given
     * @see AbstractEFapsInstallMojo#getVersionFile() to get the version file
     *      (defining all versions to install)
     * @see #targetDirectory target directory
     * @see #targetInstallFile name and path of the installation XML file in the
     *      target directory
     */
    protected String generateInstallFile()
        throws MojoExecutionException, MojoFailureException
    {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // read version file
            final Document doc = docBuilder.parse(getVersionFile());

            // get install node
            final NodeList nodeList = doc.getElementsByTagName(GenerateInstallationMojo.TAG_INSTALL);
            final Node installNode = nodeList.item(0);

            // get application name
            final NodeList subNodeList = installNode.getChildNodes();
            String application = null;
            for (int idx = 0; idx < subNodeList.getLength(); idx++) {
                final Node subNode = subNodeList.item(idx);
                if ((Node.ELEMENT_NODE == subNode.getNodeType())
                        && GenerateInstallationMojo.TAG_APPLICATION.equals(subNode.getNodeName())) {
                    final Node subSubNode = subNode.getFirstChild();
                    if (Node.TEXT_NODE == subSubNode.getNodeType()) {
                        application = subSubNode.getNodeValue();
                    }
                    break;
                }
            }

            // test if application name is given
            if (application == null) {
                throw new MojoFailureException("Application name in '" + getVersionFile() + "' not given");
            }
            application = application.trim();

            // prepare root package name
            final String rootPackageTmp = this.rootPackage.replaceAll("/*$", "").replaceAll("^/*", "")
                + "/" + application + "/";
            // store the root package name in to the file
            final Node rootPackageName = doc.createElement("rootPackage");
            installNode.appendChild(rootPackageName);
            final Attr packAttr = doc.createAttribute("name");
            packAttr.setValue(rootPackageTmp);
            rootPackageName.getAttributes().setNamedItem(packAttr);

            // create files node and append to install node
            final Node files = doc.createElement("files");
            installNode.appendChild(files);

            // append all file name and the type to files node (sorted
            // alphabetical)
            final Set<String> filesSet = new TreeSet<String>(Arrays.asList(getFiles()));
            for (final String fileName : filesSet) {
                final Node file = doc.createElement("file");

                final Attr typeAttr = doc.createAttribute("type");

                final String type = getTypeMapping().get(fileName.substring(fileName.lastIndexOf(".") + 1));
                if (type == null) {
                    typeAttr.setValue("unknown");
                } else {
                    typeAttr.setValue(type);
                }
                file.getAttributes().setNamedItem(typeAttr);

                final Attr fileAttr = doc.createAttribute("name");
                fileAttr.setValue(rootPackageTmp + fileName);
                file.getAttributes().setNamedItem(fileAttr);

                files.appendChild(file);
            }

            // prepare file install of the target install file
            final File targetInstallFileTmp = new File(this.targetDirectory, this.targetInstallFile);

            // get parent directory of target installation file
            // and create directories (if needed)
            final File parentDir = targetInstallFileTmp.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            // open transformer (to convert XML in memory to a stream).
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // initialize StreamResult with File object to save to file
            // flush output stream and write to file (and close file)
            final OutputStream os = new FileOutputStream(targetInstallFileTmp);
            final StreamResult result = new StreamResult(new OutputStreamWriter(os, this.targetEncoding));
            final DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            os.flush();
            os.close();

            return rootPackageTmp;
        } catch (final MojoFailureException e) {
            throw e;
        } catch (final Exception e) {
            throw new MojoExecutionException("could not create target XML " + "installation file", e);
        }
    }

    /**
     * Copy all eFaps installation files from the eFaps root directory in the
     * related target classes directory.
     *
     * @param _rootPackage root package include application sub directory
     * @throws MojoExecutionException if some files could not be copied
     * @see AbstractEFapsInstallMojo#getEFapsDir() to get source directory
     * @see #targetDirectory to get target directory
     * @see #getCopyFiles() get all files to copy
     */
    protected void copyFiles(final String _rootPackage)
        throws MojoExecutionException
    {
        try {
            for (final String fileName : getCopyFiles()) {
                final File srcFile = new File(getEFapsDir(), fileName);
                final File dstFile = new File(this.targetDirectory, _rootPackage + fileName);
                FileUtils.copyFile(srcFile, dstFile, true);
            }
        } catch (final IOException e) {
            throw new MojoExecutionException("could not copy files", e);
        }
    }

    /**
     * Uses the {@link #copyIncludes} and {@link #copyExcludes} together with
     * the root directory {@link AbstractEFapsInstallMojo#getEFapsDir()} to get
     * all related and matched files. The files are used to idendtify which are
     * copied in the target directory.<br>
     * <br>
     * The instance variable {@link #copyIncludes} defines includes; if not
     * specified by maven, the default value is: <li>
     * <code>**&#x002f;*.css</code></li> <li><code>**&#x002f;*.gif</code></li>
     * <li><code>**&#x002f;*.java</code></li> <li><code>**&#x002f;*.js</code></li>
     * <li><code>**&#x002f;*.png</code></li> <li>
     * <code>**&#x002f;*.properties</code></li> <li><code>**&#x002f;*.xml</code>
     * </li> <li><code>**&#x002f;*.xsl</code></li> <br>
     * The instance variable {@link#copyExcludes} defines excludes; if not
     * specified by maven , the default value is: <li>
     * <code>**&#x002f;version.xml</code></li>
     *
     * @return String array of files to copy
     * @see #copyIncludes
     * @see #copyExcludes
     * @see #DEFAULT_COPYINCLUDES definition of the default includes
     * @see #DEFAULT_COPYEXCLUDES definition of the default excludes
     */
    protected String[] getCopyFiles()
    {
        // scan
        final DirectoryScanner ds = new DirectoryScanner();
        final String[] includes = (this.copyIncludes == null)
            ? GenerateInstallationMojo.DEFAULT_COPYINCLUDES
                            .toArray(new String[GenerateInstallationMojo.DEFAULT_COPYINCLUDES.size()])
            : this.copyIncludes.toArray(new String[this.copyIncludes.size()]);
        final String[] excludes = (this.copyIncludes == null)
            ? GenerateInstallationMojo.DEFAULT_COPYEXCLUDES
                            .toArray(new String[GenerateInstallationMojo.DEFAULT_COPYEXCLUDES.size()])
            : this.copyExcludes.toArray(new String[this.copyExcludes.size()]);
        ds.setIncludes(includes);
        ds.setExcludes(excludes);
        ds.setBasedir(getEFapsDir().toString());
        ds.setCaseSensitive(true);
        ds.scan();
        return ds.getIncludedFiles();
    }
}
