/*
 * Copyright by Apache Maven (see http://maven.apache.org)
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

package org.efaps.maven.java5;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.descriptor.Requirement;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.tools.plugin.Component;
import org.apache.maven.tools.plugin.Execute;
import org.apache.maven.tools.plugin.Goal;
import org.apache.maven.tools.plugin.Parameter;
import org.apache.maven.tools.plugin.PluginToolsRequest;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor;
import org.apache.maven.tools.plugin.lifecycle.Phase;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Thanks to Jason van Zyl and his
 * <href="http://www.jfrog.org/sites/mvn-anno-mojo/latest/">
 * &quot;Maven Anno Mojo&quot;</a> the problem that the
 * JavaMojoAnnotaitonDescriptorExtraction from Apache Maven does not work
 * sometimes for Maven 2.0.X was found: the class path of the compile
 * dependencies was not created correctly and so done manually within this
 * class as extendsion the Apache Maven JavaMojoAnnotaitonDescriptorExtraction.
 *
 * @author tmo
 * @version $Id$
 * @see org.apache.maven.tools.plugin.extractor.java.JavaMojoAnnotationDescriptorExtractor
 * @plexus.component role="org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor" role-hint="java5"
 */
public class EfapsAnnotationDescriptorExtractor extends AbstractLogEnabled implements Contextualizable, MojoDescriptorExtractor {

  /**
   *Property key name of the the XML user settings file location.
   *
   * @see #getLocalRepository   method using this global static variable
   */
  private static final String PROP_KEY_USER_SETTINGS_XML_LOCATION = "org.apache.maven.user-settings";

  /**
   * Property key name of the system property to get the local repository
   * location.
   *
   * @see #getLocalRepository   method using this global static variable
   */
  private final static String PROP_KEY_LOCAL_REPOSITORY_LOCATION = "maven.repo.local";

  /**
   * Property key name to the the user home directory
   *
   * @see #getLocalRepository   method using this global static variable
   */
  private final static String PROP_KEY_USER_HOME = "user.home";


  private ArtifactRepositoryLayout artifactRepositoryLayout = null;

  private ArtifactRepositoryFactory artifactRepositoryFactory = null;

  private ArtifactResolver artifactResolver = null;

  private MavenSettingsBuilder settingsBuilder = null;

  private ArtifactFactory artifactFactory = null;

  private ArtifactMetadataSource artifactMetadataSource;

  final ArtifactFilter filter = new ArtifactFilter() {
    public boolean include(final Artifact artifact) {
      return isCompileScope(artifact.getScope());
    }
  };

  /**
   * Initialize the container from the context.
   *
   */
  public void contextualize(final Context _context) throws ContextException {
    final PlexusContainer container = (PlexusContainer) _context.get(PlexusConstants.PLEXUS_KEY);
    try  {
      this.artifactRepositoryLayout = (ArtifactRepositoryLayout) container.lookup(ArtifactRepositoryLayout.ROLE);
    } catch (final ComponentLookupException e) {
      throw new ContextException("Could not get ArtifactRepositoryLayout from PlexusContainer", e);
    }
    try {
      this.artifactRepositoryFactory = (ArtifactRepositoryFactory) container.lookup(ArtifactRepositoryFactory.ROLE);
    } catch (final ComponentLookupException e) {
      throw new ContextException("Could not get ArtifactResolver from PlexusContainer", e);
    }
    try {
      this.settingsBuilder = (MavenSettingsBuilder) container.lookup(MavenSettingsBuilder.ROLE);
    } catch (final ComponentLookupException e) {
      throw new ContextException("Could not get ArtifactResolver from PlexusContainer", e);
    }
    try {
      this.artifactResolver = (ArtifactResolver) container.lookup(ArtifactResolver.ROLE);
    } catch (final ComponentLookupException e) {
      throw new ContextException("Could not get ArtifactResolver from PlexusContainer", e);
    }
    try {
        this.artifactFactory = (ArtifactFactory) container.lookup(ArtifactFactory.ROLE);
    } catch (final ComponentLookupException e) {
        throw new ContextException("Could not get ArtifactFactory from PlexusContainer", e);
    }
    try  {
      this.artifactMetadataSource = (ArtifactMetadataSource) container.lookup(ArtifactMetadataSource.ROLE, "maven");
    } catch (final ComponentLookupException e) {
      throw new ContextException("Could not get ArtifactMetadataSource from PlexusContainer", e);
    }
  }

  /**
   *
   * @param _project
   * @param _pluginDescriptor
   */
  public List<MojoDescriptor> execute(final MavenProject _project,
                                      final PluginDescriptor _pluginDescriptor)
      throws InvalidPluginDescriptorException
  {

    final List<MojoDescriptor> descriptors = new ArrayList<MojoDescriptor>();

    // check all compiled classes for mojos
    final File classesDirectory = new File( _project.getBuild().getOutputDirectory() );
    final DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(classesDirectory);
    scanner.setIncludes( new String[] { "**/*.class" } );
    scanner.scan();
    if (getLogger().isDebugEnabled())  {
      getLogger().debug( "Scanning " + scanner.getIncludedFiles().length + " classes" );
    }
    final String[] included = scanner.getIncludedFiles();
    if (included.length > 0) {

      final ClassLoader cl = getClassLoader(_project);

      for (final String file : scanner.getIncludedFiles()) {
        final MojoDescriptor desc =
            scan(cl, file.substring(0, file.lastIndexOf(".class")).replace('/', '.'));
        if (desc != null) {
          desc.setPluginDescriptor(_pluginDescriptor);
          descriptors.add(desc);
          if (getLogger().isInfoEnabled()) {
            getLogger().info("Found mojo " + desc.getImplementation());
          }
        }
      }
    }

    final Resource resource = new Resource();
    resource.setDirectory( classesDirectory.getAbsolutePath() );
    resource.setIncludes( Collections.EMPTY_LIST );
    resource.setExcludes( Collections.EMPTY_LIST );
    _project.addResource( resource );

    return descriptors;
  }

  /**
   * Scan given class name for a mojo (using the annotations).
   *
   * @param _cl         class loader
   * @param _className  class name
   * @return mojo descriptor, or <code>null</code> if the given class is not a
   *         mojo
   * @throws InvalidPluginDescriptorException
   */
  private MojoDescriptor scan(final ClassLoader _cl,
                              final String _className ) throws InvalidPluginDescriptorException  {
    final MojoDescriptor mojoDescriptor;
    Class<?> c;
    try {
      c = _cl.loadClass( _className );
    } catch ( final ClassNotFoundException e)  {
      throw new InvalidPluginDescriptorException( "Error scanning class " + _className, e );
    }

    final Goal goalAnno = c.getAnnotation(Goal.class);
    if (goalAnno == null)  {
      getLogger().debug( "  Not a mojo: " + c.getName() );
      mojoDescriptor = null;
    } else  {
      mojoDescriptor = new MojoDescriptor();
      mojoDescriptor.setRole( Mojo.ROLE );
      mojoDescriptor.setImplementation( c.getName() );
      mojoDescriptor.setLanguage( "java" );
      mojoDescriptor.setInstantiationStrategy( goalAnno.instantiationStrategy() );
      mojoDescriptor.setExecutionStrategy( goalAnno.executionStrategy() );
      mojoDescriptor.setGoal( goalAnno.name() );
      mojoDescriptor.setAggregator( goalAnno.aggregator() );
      mojoDescriptor.setDependencyResolutionRequired( goalAnno.requiresDependencyResolutionScope() );
      mojoDescriptor.setDirectInvocationOnly( goalAnno.requiresDirectInvocation() );
      mojoDescriptor.setProjectRequired( goalAnno.requiresProject() );
      mojoDescriptor.setOnlineRequired( goalAnno.requiresOnline() );
      mojoDescriptor.setInheritedByDefault( goalAnno.inheritByDefault() );

      if (!Phase.VOID.equals( goalAnno.defaultPhase())) {
        mojoDescriptor.setPhase( goalAnno.defaultPhase().key() );
      }

      final Deprecated deprecatedAnno = c.getAnnotation( Deprecated.class );

      if ( deprecatedAnno != null )  {
        mojoDescriptor.setDeprecated( "true" );
      }

      final Execute executeAnno = c.getAnnotation( Execute.class );

      if (executeAnno != null)  {
        final String lifecycle = nullify(executeAnno.lifecycle());
        mojoDescriptor.setExecuteLifecycle(lifecycle);

        if (Phase.VOID.equals(executeAnno.phase()))  {
          mojoDescriptor.setExecutePhase( executeAnno.phase().key());
        }

        final String customPhase = executeAnno.customPhase();

        if (customPhase.length() > 0)  {
          if (!Phase.VOID.equals( executeAnno.phase()))   {
            getLogger().warn( "Custom phase is overriding \"phase\" field." );
          }
          if (lifecycle == null)  {
            getLogger().warn(
                                    "Setting a custom phase without a lifecycle is prone to error. If the phase is not custom, set the \"phase\" field instead." );
          }
          mojoDescriptor.setExecutePhase( executeAnno.customPhase() );
        }

        mojoDescriptor.setExecuteGoal( nullify( executeAnno.goal() ) );
      }

      Class<?> cur = c;
      while (!Object.class.equals( cur ))  {
        attachFieldParameters( cur, mojoDescriptor );
        cur = cur.getSuperclass();
      }

      if (getLogger().isDebugEnabled())  {
        getLogger().debug("  Component found: " + mojoDescriptor.getHumanReadableKey());
      }
    }

    return mojoDescriptor;
  }

  /**
   *
   * @param _cur            class to get the field parameters
   * @param _mojoDescriptor mojo descriptor where to attach the field
   *                        parameters
   * @throws InvalidPluginDescriptorException
   */
  private void attachFieldParameters(final Class<?> _cur,
                                     final MojoDescriptor _mojoDescriptor)
      throws InvalidPluginDescriptorException
  {
    for (final Field f : _cur.getDeclaredFields())  {
      final org.apache.maven.plugin.descriptor.Parameter paramDescriptor =
            new org.apache.maven.plugin.descriptor.Parameter();

      paramDescriptor.setName( f.getName() );

      final Parameter paramAnno = f.getAnnotation(Parameter.class);

      if (paramAnno != null)  {
        paramDescriptor.setAlias( nullify( paramAnno.alias() ) );
        paramDescriptor.setDefaultValue( nullify( paramAnno.defaultValue() ) );
        paramDescriptor.setEditable( !paramAnno.readonly() );
        paramDescriptor.setExpression( nullify( paramAnno.expression() ) );

        if ("${reports}".equals(paramDescriptor.getExpression()))  {
          _mojoDescriptor.setRequiresReports( true );
        }

        paramDescriptor.setImplementation( nullify( paramAnno.implementation() ) );
        paramDescriptor.setRequired( paramAnno.required() );

        final String property = nullify( paramAnno.property() );

        if (property != null)  {
          paramDescriptor.setName( property );
        }
      }

      final Component componentAnno = f.getAnnotation( Component.class );

      if (componentAnno != null)  {
        String role = nullify( componentAnno.role() );
        if (role == null)  {
          role = f.getType().getCanonicalName();
        }
        paramDescriptor.setRequirement( new Requirement( role, nullify( componentAnno.roleHint() ) ) );
      }

      if ((paramAnno != null) || (componentAnno != null))  {
        paramDescriptor.setType(f.getType().getCanonicalName());
        _mojoDescriptor.addParameter( paramDescriptor );
      }
    }
  }

  /**
   *
   * @param _project  maven project
   * @return
   * @throws InvalidPluginDescriptorException
   */
  protected ClassLoader getClassLoader(final MavenProject _project)
  throws InvalidPluginDescriptorException
  {
    final List<URL> urls = new ArrayList<URL>();

    // append all compile dependencies to the urls
    final Set<Artifact> toResolve = new HashSet<Artifact>();
    for (final Object obj : _project.getDependencies()) {
      final Dependency dependency = (Dependency)obj;
      final String scope = dependency.getScope();
      if (isCompileScope(scope))  {
        final Artifact artifact = this.artifactFactory.createArtifact(
                  dependency.getGroupId(),
                  dependency.getArtifactId(),
                  dependency.getVersion(),
                  scope,
                  dependency.getType());
        toResolve.add(artifact);
      }
    }
    try {
      final ArtifactResolutionResult result = this.artifactResolver.resolveTransitively(
                         toResolve,
                         _project.getArtifact(),
                         getManagedVersionMap(_project),
                         getLocalRepository(),
                         _project.getRemoteArtifactRepositories(),
                         this.artifactMetadataSource,
                         this.filter);
      for (final Object obj : result.getArtifacts()) {
        final Artifact artifact = (Artifact) obj;
        urls.add(artifact.getFile().toURL());
      }
    } catch (final Exception e) {
      throw new InvalidPluginDescriptorException(
          "Failed to resolve transitively artifacts: " + e.getMessage(), e);
    }

    // append compile class path elements
    for (final Object obj : _project.getArtifacts())  {
      final Artifact cpe = (Artifact) obj;
      try  {
        urls.add( cpe.getFile().toURL() ); // URI().toURL() );
      } catch (final MalformedURLException e)  {
        getLogger().warn( "Cannot convert '" + cpe + "' to URL", e );
      }
    }

    // append target output directory (where the compiled files are)
    try  {
      urls.add(new File(_project.getBuild().getOutputDirectory()).toURL());
    } catch (final MalformedURLException e)  {
      getLogger().warn( "Cannot convert '" + _project.getBuild().getOutputDirectory() + "' to URL", e );
    }

    if (getLogger().isDebugEnabled())  {
      getLogger().debug( "URLS: \n" + urls.toString().replaceAll( ",", "\n  " ));
    } else if (getLogger().isInfoEnabled())  {
      getLogger().info( "URLS: \n" + urls.toString().replaceAll( ",", "\n  " ));
    }

    return new URLClassLoader(urls.toArray(new URL[urls.size()]),
                              getClass().getClassLoader());
  }

  /**
   *
   * @param _project
   * @return
   * @throws InvalidPluginDescriptorException
   */
  protected Map<String, Artifact> getManagedVersionMap(final MavenProject _project)
      throws InvalidPluginDescriptorException {

    final Map<String, Artifact> map = new HashMap<String, Artifact>();
    final DependencyManagement dependencyManagement = _project.getDependencyManagement();
    final String projectId = _project.getId();

    if ((dependencyManagement != null) && (dependencyManagement.getDependencies() != null))  {
      for (final Object obj : dependencyManagement.getDependencies()) {
        final Dependency d = (Dependency) obj;

        try {
          final VersionRange versionRange = VersionRange.createFromVersionSpec(d.getVersion());
          final Artifact artifact = this.artifactFactory.createDependencyArtifact(
                  d.getGroupId(),
                  d.getArtifactId(),
                  versionRange,
                  d.getType(),
                  d.getClassifier(),
                  d.getScope(),
                  d.isOptional());
          map.put(d.getManagementKey(), artifact);
        } catch (final InvalidVersionSpecificationException e)  {
          throw new InvalidPluginDescriptorException("Unable to parse version '" + d.getVersion() +
                  "' for dependency '" + d.getManagementKey() + "' in project " + projectId + " : " + e.getMessage(), e);
        }
      }
    }
    return map;
  }

  /**
   * Retrieve the local repository path using the following search pattern:
   * <ol>
   * <li>System Property</li>
   * <li>localRepository specified in user settings file</li>
   * <li><code>${user.home}/.m2/repository</code></li>
   * </ol>
   *
   * @see #PROP_KEY_LOCAL_REPOSITORY_LOCATION
   * @see #PROP_KEY_USER_SETTINGS_XML_LOCATION
   * @see #PROP_KEY_USER_HOME
   */
  protected ArtifactRepository getLocalRepository() throws InvalidPluginDescriptorException {
    String localRepositoryPath = System.getProperty(PROP_KEY_LOCAL_REPOSITORY_LOCATION);
    if (localRepositoryPath == null)  {

      final File userSettingsPath = new File(System.getProperty(PROP_KEY_USER_SETTINGS_XML_LOCATION)+"");

      try {
        final Settings settings = this.settingsBuilder.buildSettings(userSettingsPath);
        localRepositoryPath = settings.getLocalRepository();
      } catch (final IOException e)  {
        throw new InvalidPluginDescriptorException("Error reading settings file", e);
      }  catch (final XmlPullParserException e)   {
        throw new InvalidPluginDescriptorException(e.getMessage() + e.getDetail() + e.getLineNumber() +
                   e.getColumnNumber());
      }

    }
    if (localRepositoryPath == null)  {
      localRepositoryPath =
          new File(new File(System.getProperty(PROP_KEY_USER_HOME), ".m2"), "repository")
          .getAbsolutePath();
    }

    // get local repository path as URL
    final File directory = new File(localRepositoryPath);
    String repositoryUrl = directory.getAbsolutePath();
    if (!repositoryUrl.startsWith("file:")) {
      repositoryUrl = "file://" + repositoryUrl;
    }

    final ArtifactRepository localRepository = new DefaultArtifactRepository("local",
                                                                             repositoryUrl,
                                                                             this.artifactRepositoryLayout);

    this.artifactRepositoryFactory.setGlobalUpdatePolicy(ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS);
    this.artifactRepositoryFactory.setGlobalChecksumPolicy(ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);

    return localRepository;
  }

  /**
   * Test if given scope is a compile scope.
   *
   * @param _scope  scope to test
   * @return <i>true</i> if given scope is not a system, test or runtime scrope
   */
  protected boolean isCompileScope(final String _scope)  {
    return !Artifact.SCOPE_SYSTEM.equals(_scope)
           && !Artifact.SCOPE_TEST.equals(_scope)
           && !Artifact.SCOPE_RUNTIME.equals(_scope);
  }

  /**
   * Returns a <code>null</code> if the string value is a zero length string.
   *
   * @param _value  text value to nullify
   * @return <code>null</code> if string is <code>null</null> or a zero length
   *         string; otherwise the value itself is returned
   */
  protected String nullify(final String _value)  {
    return (_value == null) || (_value.trim().length() == 0)
           ? null
           : _value;
  }
  public List execute(final PluginToolsRequest _pluginToolRequest) throws ExtractionException,
  InvalidPluginDescriptorException {
return execute(_pluginToolRequest.getProject(), _pluginToolRequest.getPluginDescriptor());
}
}
