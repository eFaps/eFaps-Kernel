/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.update;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIAdminEvent;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.update.event.Event;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * <p>
 * This class is the major class for importing or updating of types, commands
 * and so on in eFaps.
 * <p/>
 * <p>
 * For every kind of Object in eFaps a own class extends this AbstractUpdate. In
 * this classes the XML-Files is read and with the digester converted in
 * Objects. After reading all Objects of one XML-File the Objects are inserted
 * corresponding to the Version.
 * </p>
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUpdate
    implements IUpdate
{

    /**
     * Logging instance used to give logging information of this class.
     */
    public static final Logger LOG = LoggerFactory.getLogger(AbstractUpdate.class);

    /**
     * The URL of the xml file is stored in this instance variable.
     */
    private final URL url;

    /**
     * The name of the data model type is store in this instance variable.
     */
    private final String dataModelTypeName;

    /**
     * All known link types are set to this instance variable.
     */
    private final Set<Link> allLinkTypes;

    /**
     * The universal unique identifier of the object is stored in this instance
     * variable.
     *
     * @see #setUUID
     */
    private String uuid = null;

    /**
     * Name of the file application for which this XML file is defined.
     *
     * @see #setFileApplication
     */
    private String fileApplication = null;

    /**
     * Revision of the XML file.
     *
     * @see #setFileRevision
     */
    private String fileRevision = null;

    /**
     * All definitions of versions are added to this list.
     */
    private final List<AbstractDefinition> definitions = new ArrayList<AbstractDefinition>();

    /**
     * Default constructor with no defined possible links for given
     * <code>_dataModelTypeName</code>.
     *
     * @param _url URL of the update file
     * @param _dataModelTypeName name of the data model type to update
     */
    protected AbstractUpdate(final URL _url,
                             final String _dataModelTypeName)
    {
        this(_url, _dataModelTypeName, null);
    }

    /**
     * Default constructor with defined possible links
     * <code>_allLinkTypes</code> for given <code>_dataModelTypeName</code>.
     *
     * @param _url URL of the update file
     * @param _dataModelTypeName name of the data model type to update
     * @param _allLinkTypes all possible type link
     */
    protected AbstractUpdate(final URL _url,
                             final String _dataModelTypeName,
                             final Set<Link> _allLinkTypes)
    {
        this.url = _url;
        this.dataModelTypeName = _dataModelTypeName;
        this.allLinkTypes = _allLinkTypes;
    }

    /**
     * Read event for given tags path with attributes and text.
     *
     * @param _tags tags path as list
     * @param _attributes map of attributes for current tag
     * @param _text content text of this tags path TODO: error could not be
     *            thrown because db properties is not read correctly
     * @throws SAXException on error
     * @throws EFapsException on error
     */
    @Override
    public void readXML(final List<String> _tags,
                        final Map<String, String> _attributes,
                        final String _text)
        throws SAXException, EFapsException
    {
        if (_tags.size() == 1) {
            final String value = _tags.get(0);
            if ("uuid".equals(value)) {
                this.uuid = _text;
            } else if ("file-application".equals(value)) {
                this.fileApplication = _text;
            } else if ("file-revision".equals(value)) {
                this.fileRevision = _text;
            } else if ("definition".equals(value)) {
                this.definitions.add(newDefinition());
            }
        } else if ("definition".equals(_tags.get(0))) {
            final AbstractDefinition curDef = this.definitions.get(this.definitions.size() - 1);
            curDef.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
        } else {
            throw new SAXException("Unknown XML Tag: " + _tags + " for: " + this.url);
        }
    }

    /**
     * Creates a new definition instance used from
     * {@link #readXML(List, Map, String)}.
     *
     * @return new definition instance
     */
    protected abstract AbstractDefinition newDefinition();

    /**
     * Adds one definition of a update for a specific version to all definitions
     * in {@link #definitions}.
     *
     * @param _definition definition to add
     * @see #definitions
     */
    protected void addDefinition(final AbstractDefinition _definition)
    {
        this.definitions.add(_definition);
    }

    /**
     * The instance method returns the eFaps instance representing the read XML
     * configuration. If not already get from the eFaps database, the
     * information is read. If no instance exists in the database, a new one is
     * automatically created. The method searches for the given universal unique
     * identifier in {@link #uuid} the instance in the eFaps database and stores
     * the result in {@link #instance}. If no object is found in eFaps,
     * {@link #instance} is set to <code>null</code>. A new instance is created
     * in the eFaps DB for given universal unique identifier in {@link #uuid}.
     * The name of the access set is also the universal unique identifier,
     * because the name of access set is first updates in the version
     * definition.<br/>
     * The new created object is stored as instance information in
     * {@link #instance}.
     *
     * @param _jexlContext  context used to evaluate JEXL expressions
     * @param _step         current step of the update life cycle
     * @param _profiles     the Profiles assigned
     * @throws InstallationException from called update methods
     */
    @Override
    public void updateInDB(final JexlContext _jexlContext,
                           final UpdateLifecycle _step,
                           final Set<Profile> _profiles)
        throws InstallationException
    {
        for (final AbstractDefinition def : this.definitions) {
            // only execute if
            // 1. valid version
            // 2. application dependcies are met
            // 3. profiles is empty or the profile list contains the profile of the definition
            //    or the default profile must be applied
            if (def.isValidVersion(_jexlContext) && def.appDependenciesMet()
                            && (def.getProfiles().isEmpty()
                                            || CollectionUtils.containsAny(_profiles, def.getProfiles())
                                            || def.isApplyDefault(_profiles, this.definitions))) {
                if ((this.url != null) && AbstractUpdate.LOG.isDebugEnabled()) {
                    AbstractUpdate.LOG.debug("Executing '" + this.url.toString() + "'");
                }
                def.updateInDB(_step, this.allLinkTypes);
            }
        }
    }

    /**
     * This is the getter method for the instance variable {@link #url}.
     *
     * @return value of instance variable {@link #url}
     */
    @Override
    public URL getURL()
    {
        return this.url;
    }

    /**
     * Defines the new {@link #uuid UUID} of the updated object.
     *
     * @param _uuid new UUID for the object to update
     * @see #uuid
     */
    protected void setUUID(final String _uuid)
    {
        this.uuid = _uuid;
    }

    /**
     * This is the getter method for instance variable {@link #uuid}.
     *
     * @return value of instance variable {@link #uuid}
     * @see #uuid
     * @see #setUUID
     */
    public String getUUID()
    {
        return this.uuid;
    }

    /**
     * This is the setter method for instance variable {@link #fileApplication}.
     *
     * @param _fileApplication new value for instance variable
     *            {@link #fileApplication}
     * @see #fileApplication
     * @see #getFileApplication
     */
    public void setFileApplication(final String _fileApplication)
    {
        this.fileApplication = _fileApplication;
    }

    /**
     * This is the getter method for instance variable {@link #fileApplication}.
     *
     * @return value of instance variable {@link #fileApplication}
     * @see #fileApplication
     * @see #setFileApplication
     */
    @Override
    public String getFileApplication()
    {
        return this.fileApplication;
    }

    /**
     * This is the setter method for instance variable {@link #fileRevision}.
     *
     * @param _fileRevision new value for instance variable
     *            {@link #fileRevision}
     * @see #fileRevision
     * @see #getFileRevision
     */
    public void setFileRevision(final String _fileRevision)
    {
        this.fileRevision = _fileRevision;
    }

    /**
     * This is the getter method for instance variable {@link #fileRevision}.
     *
     * @return value of instance variable {@link #fileRevision}
     * @see #fileRevision
     * @see #setFileRevision
     */
    public String getFileRevision()
    {
        return this.fileRevision;
    }

    /**
     * This is the getter method for instance variable {@link #definitions}.
     *
     * @return value of instance variable {@link #definitions}
     * @see #definitions
     */
    public List<AbstractDefinition> getDefinitions()
    {
        return this.definitions;
    }

    /**
     * This is the getter method for the instance variable
     * {@link #dataModelTypeName}.
     *
     * @return value of instance variable {@link #dataModelTypeName}
     */
    public String getDataModelTypeName()
    {
        return this.dataModelTypeName;
    }

    /**
     * This is the getter method for the instance variable {@link #allLinkTypes}
     * .
     *
     * @return value of instance variable {@link #allLinkTypes}
     */
    protected Set<Link> getAllLinkTypes()
    {
        return this.allLinkTypes;
    }

    /**
     * Returns a string representation with values of all instance variables.
     *
     * @return string representation of this abstract update
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("uuid", this.uuid).append("fileApplication", this.fileApplication)
                        .append("fileRevision", this.fileRevision).append("definitions", this.definitions).toString();
    }

    /**
     * The class is used to define the links with all information needed to
     * update the link information between the object to update and the related
     * objects.
     *
     * @see #setLinksInDB
     */
    protected static class Link
    {
        /** Name of the link. */
        private final String linkName;

        /**
         * Name of the parent attribute in the link. The parent attribute stores
         * the id of the objecto udpate.
         */
        private final String parentAttrName;

        /**
         * Name of the child type used to query for the given name to which a
         * link must be set.
         */
        private final String childTypeName;

        /** Name of the child attribute in the link. */
        private final String childAttrName;

        /**
         * set of key attributes.
         */
        private final Set<String> keyAttributes = new HashSet<String>();

        /**
         * Include the child types during evaluation.
         */
        private boolean includeChildTypes = false;

        /**
         * Constructor used to initialize the instance variables.
         *
         * @param _linkName name of the link itself
         * @param _parentAttrName name of the parent attribute in the link
         * @param _childTypeName name of the child type
         * @param _childAttrName name of the child attribute in the link
         * @param _keyAttributes list of attributes used to identify the object
         *            to be connected default "Name"
         * @see #linkName
         * @see #parentAttrName
         * @see #childTypeName
         * @see #childAttrName
         */
        public Link(final String _linkName,
                    final String _parentAttrName,
                    final String _childTypeName,
                    final String _childAttrName,
                    final String... _keyAttributes)
        {
            this.linkName = _linkName;
            this.parentAttrName = _parentAttrName;
            this.childTypeName = _childTypeName;
            this.childAttrName = _childAttrName;
            for (final String keyAttribute : _keyAttributes) {
                this.keyAttributes.add(keyAttribute);
            }
            // set the default if necessary
            if (this.keyAttributes.size() < 1) {
                this.keyAttributes.add("Name");
            }
        }

        /**
         * Child Type extracted from the child type name.
         *
         * @return child type extracted from the child type name
         * @throws CacheReloadException on error
         */
        public Type getChildType()
            throws CacheReloadException
        {
            return Type.get(this.childTypeName);
        }

        /**
         * Link Type extracted from the link name.
         *
         * @return link type extracted from the link name
         * @throws CacheReloadException on error
         */
        public Type getLinkType()
            throws CacheReloadException
        {
            return Type.get(this.linkName);
        }

        /**
         * Getter method for instance variable {@link #keyAttributes}.
         *
         * @return value of instance variable {@link #keyAttributes}
         */
        public Set<String> getKeyAttributes()
        {
            return this.keyAttributes;
        }

        /**
         * Getter method for the instance variable {@link #includeChildTypes}.
         *
         * @return value of instance variable {@link #includeChildTypes}
         */
        public boolean isIncludeChildTypes()
        {
            return this.includeChildTypes;
        }

        /**
         * Setter method for instance variable {@link #includeChildTypes}.
         *
         * @param _includeChildTypes value for instance variable {@link #includeChildTypes}
         * @return this instance for chaining
         */
        public Link setIncludeChildTypes(final boolean _includeChildTypes)
        {
            this.includeChildTypes = _includeChildTypes;
            return this;
        }

        /**
         * Returns a string representation with values of all instance variables
         * of a link.
         *
         * @return string representation of this link
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).append("linkName", this.linkName).append("parentAttrName",
                            this.parentAttrName).append("childTypeName", this.childTypeName).append("childAttrName",
                            this.childAttrName).toString();
        }
    }

    /**
     * Some links has a order in the database. This means that the connections
     * must be made in the order they are defined in the XML update file.
     */
    protected static class OrderedLink
        extends AbstractUpdate.Link
    {
        /**
         * @param _linkName name of the link itself
         * @param _parentAttrName name of the parent attribute in the link
         * @param _childTypeName name of the child type
         * @param _childAttrName name of the child attribute in the link
         */
        public OrderedLink(final String _linkName,
                           final String _parentAttrName,
                           final String _childTypeName,
                           final String _childAttrName)
        {
            super(_linkName, _parentAttrName, _childTypeName, _childAttrName);
        }
    }

    /**
     * Some links has a order in the database. This means that the connections
     * must be made in the order they are defined in the XML update file.
     */
    protected static class UniqueLink
        extends AbstractUpdate.Link
    {
        /**
         * Link must be unique over a group of links.
         */
        private final Set<Link> uniqueGroup = new HashSet<Link>();

        /**
         * @param _linkName name of the link itself
         * @param _parentAttrName name of the parent attribute in the link
         * @param _childTypeName name of the child type
         * @param _childAttrName name of the child attribute in the link
         */
        public UniqueLink(final String _linkName,
                          final String _parentAttrName,
                          final String _childTypeName,
                          final String _childAttrName)
        {
            super(_linkName, _parentAttrName, _childTypeName, _childAttrName);
        }

        /**
         * @param _link link to be added to the unique group
         */
        public void add2UniqueGroup(final Link _link)
        {
            this.uniqueGroup.add(_link);
        }

        /**
         * Getter method for the instance variable {@link #uniqueGroup}.
         *
         * @return value of instance variable {@link #uniqueGroup}
         */
        public Set<Link> getUniqueGroup()
        {
            return this.uniqueGroup;
        }
    }

    /**
     * Base Definition.
     */
    public abstract class AbstractDefinition
    {
        /**
         * Expression of this definition if this definition must be installed.
         *
         * @see #readXML(List, Map, String) defines this expression
         * @see #isValidVersion(JexlContext) test this expression
         */
        private String expression = null;

        /**
         * Instance of this definition.
         */
        private Instance instance = null;

        /**
         * The value depending on the attribute name for this definition.
         *
         * @see #addValue
         * @see #getValue
         */
        private final Map<String, String> values = new HashMap<String, String>();

        /**
         * Property value depending on the property name for this definition.
         *
         * @see #addProperty.
         */
        private final Map<String, String> properties = new HashMap<String, String>();

        /**
         *
         */
        private final Map<AbstractUpdate.Link, Set<LinkInstance>> links = new HashMap<AbstractUpdate.Link,
                                                                                                   Set<LinkInstance>>();

        /**
         * Name of attribute by which the search in the database is done. If not
         * specified (defined to <code>null</code>), the attribute
         * &quot;UUID&quot; is used.
         *
         * @see #searchInstance
         */
        private final String searchAttrName;

        /**
         * list of events.
         */
        private final List<Event> events = new ArrayList<Event>();

        /**
         * Profiles this Definition is activated for.
         */
        private final Set<Profile> profiles = new HashSet<Profile>();

        /**
         * Application Dependencies this Definition is activated for.
         */
        private final Map<AppDependency, Boolean> appDependencies = new HashMap<AppDependency, Boolean>();

        /**
         * Default constructor for the attribute by which the object is searched
         * is &quot;UUID&quot;.
         */
        protected AbstractDefinition()
        {
            this(null);
        }

        /**
         * Constructor defining the search attribute.
         *
         * @param _searchAttrName name of attribute by which the object is
         *            searched
         * @see #searchInstance method using the search attribute
         * @see #searchAttrName
         */
        protected AbstractDefinition(final String _searchAttrName)
        {
            this.searchAttrName = _searchAttrName;
        }

        /**
         * @param _tags         tag to reas
         * @param _attributes   attributes
         * @param _text         text
         * @throws EFapsException on error
         */
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("name".equals(value)) {
                setName(_text);
            } else if ("property".equals(value)) {
                this.properties.put(_attributes.get("name"), _text);
            } else if ("version-expression".equals(value)) {
                this.expression = _text;
            } else if ("profiles".equals(value))  {
                if (_tags.size() > 1)  {
                    final String subValue = _tags.get(1);
                    if ("profile".equals(subValue))  {
                        this.profiles.add(Profile.getProfile(_attributes.get("name")));
                    }
                }
            } else if ("application-dependencies".equals(value))  {
                if (_tags.size() > 1)  {
                    final String subValue = _tags.get(1);
                    if ("application".equals(subValue))  {
                        final AppDependency dep = AppDependency.getAppDependency(_attributes.get("name"));
                        final Boolean exclude = Boolean.valueOf(_attributes.get("exclude"));
                        this.appDependencies.put(dep, exclude);
                    }
                }
            } else {
                throw new Error("Unknown Tag '" + _tags + "' (file " + AbstractUpdate.this.url + ")");
            }
        }

        /**
         * Evaluates the JEXP expression defined in {@link #expression}. If this
         * expression returns true, the definition is a valid version and could
         * be executed.
         *
         * @param _jexlContext context used to evaluate JEXL expressions
         * @return <i>true</i> if the definition is valid
         * @throws InstallationException if the JEXL expression in {@link #expression}
         *             could not be evaluated
         * @see #expression
         */
        public boolean isValidVersion(final JexlContext _jexlContext)
            throws InstallationException
        {
            boolean exec;
            try {
                if (this.expression == null) {
                    final Expression jexlExpr = ExpressionFactory.createExpression("version==latest");
                    exec = Boolean.parseBoolean(jexlExpr.evaluate(_jexlContext).toString());
                } else {
                    final Expression jexlExpr = ExpressionFactory.createExpression(this.expression);
                    exec = Boolean.parseBoolean(jexlExpr.evaluate(_jexlContext).toString());
                }
                //CHECKSTYLE:OFF
            } catch (final Exception e) {
              //CHECKSTYLE:ON
                throw new InstallationException("isValidVersion.JEXLExpressionNotEvaluatable", e);
            }
            return exec;
        }

        /**
         * @return true if one of the AppDependencies is met
         * @throws InstallationException on error
         */
        public boolean appDependenciesMet()
            throws InstallationException
        {
            boolean ret;
            if (this.appDependencies.isEmpty()) {
                ret = true;
            } else {
                ret = false;
                for (final Entry<AppDependency, Boolean> entry : this.appDependencies.entrySet()) {
                    final boolean met = entry.getKey().isMet();
                    if ((met && !entry.getValue()) || (!met && entry.getValue())) {
                        ret = true;
                        break;
                    }
                }
            }
            return ret;
        }

        /**
         * @param _profiles set of profiles as defined by an version
         *                  file o via SystemConfiguration for the execution of an CIItem
         * @param  _definitions the list of definitions that are contained in the same CIItem
         * @return true if this definition must be
         * @throws InstallationException on error
         */
        public boolean isApplyDefault(final Set<Profile> _profiles,
                                      final List<AbstractDefinition> _definitions)
            throws InstallationException
        {
            boolean ret = false;
            // only applies if this definition is marked as the default
            if (getProfiles().contains(Profile.getDefaultProfile())) {
                ret = true;
                // if one of the definitions is enabled by its profile the default must not be applied
                for (final AbstractDefinition def: _definitions) {
                    if (CollectionUtils.containsAny(_profiles, def.getProfiles())) {
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }

        /**
         * In case that this Definition does not have a profile assigned,
         * the Default Profile will be assigned on the first call of this method.
         * @param _profile Profile to be checked
         * @return true if this Definition belongs to the given Profile,
         *   else false
         */
        public boolean assignedTo(final Profile _profile)
        {
            return this.profiles.isEmpty() ? true : this.profiles.contains(_profile);
        }

        /**
         * @param _step current update step
         * @param _allLinkTypes set of all type of links
         * @throws InstallationException if update failed
         */
        protected void updateInDB(final UpdateLifecycle _step,
                                  final Set<AbstractUpdate.Link> _allLinkTypes)
            throws InstallationException
        {
            if (_step == UpdateLifecycle.EFAPS_CREATE) {
                searchInstance();

                // if no instance exists, a new insert must be done
                if (this.instance == null) {
                    final Insert insert;
                    try {
                        insert = new Insert(getDataModelTypeName());
                        insert.add("UUID", AbstractUpdate.this.uuid);
                    } catch (final EFapsException e) {
                        throw new InstallationException("Initialize for the insert of '"
                                        + getDataModelTypeName() + "' with UUID '"
                                        + AbstractUpdate.this.uuid + "' failed", e);
                    }
                    createInDB(insert);
                }

            } else if (_step == UpdateLifecycle.EFAPS_UPDATE) {
                try {
                    final String name = this.values.get("Name");
                    final Update update = new Update(this.instance);
                    if (this.instance.getType().getAttribute("Revision") != null) {
                        update.add("Revision", AbstractUpdate.this.fileRevision);
                    }
                    for (final Map.Entry<String, String> entry : this.values.entrySet()) {
                        update.add(entry.getKey(), entry.getValue());
                    }
                    if (AbstractUpdate.LOG.isInfoEnabled() && (name != null)) {
                        AbstractUpdate.LOG.info("    Update " + this.instance.getType().getName() + " '" + name + "'");
                    }
                    update.executeWithoutAccessCheck();

                    if (_allLinkTypes != null) {
                        for (final Link linkType : _allLinkTypes) {
                            setLinksInDB(this.instance, linkType, this.links.get(linkType));
                        }
                    }
                    setPropertiesInDb(this.instance, this.properties);
                    final List<Instance> eventInstList = new ArrayList<Instance>();
                    for (final Event event : getEvents()) {
                        final Instance eventInst = event.updateInDB(this.instance, getValue("Name"));
                        setPropertiesInDb(eventInst, event.getProperties());
                        eventInstList.add(eventInst);
                    }
                    removeObsoleteEvents(this.instance, eventInstList);
                } catch (final EFapsException e) {
                    throw new InstallationException("update did not work", e);
                }
            }
        }

        /**
         * @param _instance Instance that will be checked 4 obsolete events
         * @param _eventInstList list of valid event Instances
         * @throws EFapsException on error
         */
        protected void removeObsoleteEvents(final Instance _instance,
                                            final List<Instance> _eventInstList)
            throws EFapsException
        {
            // check it not first install and only for the objects that are inside the "t_cmabstract" sql table
            if (CIAdminEvent.Definition.getType().getMainTable() != null
                            && CIAdmin.Abstract.getType().getMainTable() != null
                            && CIAdmin.Abstract.getType().getMainTable().equals(_instance.getType().getMainTable())) {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminEvent.Definition);
                queryBldr.addWhereAttrEqValue(CIAdminEvent.Definition.Abstract, _instance);
                final InstanceQuery query = queryBldr.getQuery();
                final List<Instance> instances = query.executeWithoutAccessCheck();
                final List<?> obsoletes = ListUtils.removeAll(instances, _eventInstList);
                for (final Object inst : obsoletes) {
                    new Delete((Instance) inst).executeWithoutAccessCheck();
                }
            }
        }

        /**
         * Search for given data model type. If an attribute name for the search
         * is defined in {@link #searchAttrName}, the search is done with this
         * given attribute. If no attribute name is defined (value is
         * <code>null</code>, the method searches for given UUID. The result is
         * stored in {@link #instance} (or set to null, if not found).<br/>
         * The search is only done, if no instance is defined (meaning if
         * {@link #instance} has no value).
         *
         * @see #instance variable in which the search result is stored (and the
         *      search is only done if the value is <code>null</code>)
         * @see #searchAttrName name of the attribute which them the search is
         *      done
         * @throws InstallationException if search for the instance failed
         */
        protected void searchInstance()
            throws InstallationException
        {
            if (this.instance == null) {
                try {
                    final QueryBuilder queryBldr = new QueryBuilder(Type.get(getDataModelTypeName()));
                    if (this.searchAttrName == null) {
                        queryBldr.addWhereAttrEqValue("UUID", AbstractUpdate.this.uuid);
                    } else {
                        queryBldr.addWhereAttrEqValue(this.searchAttrName, this.values.get(this.searchAttrName));
                    }
                    final InstanceQuery query = queryBldr.getQuery();
                    query.executeWithoutAccessCheck();
                    if (query.next()) {
                        this.instance = query.getCurrentValue();
                    }
                } catch (final EFapsException e) {
                    throw new InstallationException("Search for '" + getDataModelTypeName() + "' for '"
                                    + ((this.searchAttrName == null)
                                                    ? AbstractUpdate.this.uuid
                                                    : this.values.get(this.searchAttrName))
                                    + "' failed", e);
                }
            }
        }

        /**
         * Inserts current instance defined by <code>_insert</code> into the
         * eFaps database without any access check.
         *
         * @param _insert insert instance
         * @throws InstallationException if insert failed
         */
        protected void createInDB(final Insert _insert)
            throws InstallationException
        {
            try {
                if (_insert.getInstance().getType().getAttribute("Revision") != null) {
                    _insert.add("Revision", AbstractUpdate.this.fileRevision);
                }
                final String name = this.values.get("Name");
                _insert.add("Name", (name == null) ? "-" : name);
                if (AbstractUpdate.LOG.isInfoEnabled()) {
                    AbstractUpdate.LOG.info("    Insert " + _insert.getInstance().getType().getName()
                                    + " '" + name + "'");
                }
                _insert.executeWithoutAccessCheck();
            } catch (final EFapsException e) {
                throw new InstallationException("Insert for '" + _insert.getInstance().getType().getName()
                                + "' '" + this.values.get("Name") + " failed", e);
            }
            this.instance = _insert.getInstance();
        }

        /**
         * Remove all links  of a specific type from a
         * given object (defined by the instance).
         *
         * @param _instance instance for which all links must be removed
         * @param _linkType type of link which must be removed
         * @throws EFapsException if existing links could not be removed
         *             (deleted)
         * @see #setLinksInDB used to remove all links for given instance with a
         *      zero length set of link instances
         */
        protected void removeLinksInDB(final Instance _instance,
                                       final Link _linkType)
            throws EFapsException
        {
            setLinksInDB(_instance, _linkType, new HashSet<LinkInstance>());
        }

        /**
         * Sets the links from this object to the given list of objects (with
         * the object name) in the eFaps database.
         *
         * @param _instance instance for which the links must be defined
         * @param _linktype type of the link to be updated
         * @param _links all links of the type _linktype which will be connected
         *            to this instance
         * @throws EFapsException if links could not be defined
         */
        protected void setLinksInDB(final Instance _instance,
                                    final Link _linktype,
                                    final Set<LinkInstance> _links)
            throws EFapsException
        {
            if (_links != null) {
                final Iterator<LinkInstance> linkIter = _links.iterator();
                // 1. search the object to be linked to, if not found remove it
                while (linkIter.hasNext()) {
                    final LinkInstance oneLink = linkIter.next();
                    final QueryBuilder queryBldr = new QueryBuilder(Type.get(_linktype.childTypeName));
                    for (final Entry<String, String> entry : oneLink.getKeyAttr2Value().entrySet()) {
                        queryBldr.addWhereAttrEqValue(entry.getKey(), entry.getValue());
                    }
                    final InstanceQuery query = queryBldr.getQuery();
                    final List<Instance> childInsts = query.executeWithoutAccessCheck();
                    // only if a child object is found the next steps are done
                    if (childInsts.size() == 1) {
                        oneLink.setChildInstance(childInsts.get(0));
                        // 2. search if a link already exists
                        final QueryBuilder linkQueryBldr = new QueryBuilder(Type.get(_linktype.linkName));
                        linkQueryBldr.addWhereAttrEqValue(_linktype.parentAttrName, _instance);
                        linkQueryBldr.addWhereAttrEqValue(_linktype.childAttrName, oneLink.getChildInstance());
                        final InstanceQuery linkQuery = linkQueryBldr.getQuery();
                        final List<Instance> linkInsts = linkQuery.executeWithoutAccessCheck();
                        if (linkInsts.size() == 1) {
                            oneLink.setInstance(linkInsts.get(0));
                        }
                    } else if (childInsts.size() < 1) {
                        linkIter.remove();
                        AbstractUpdate.LOG.error("No object found for link definition {}", oneLink);
                    } else {
                        linkIter.remove();
                        AbstractUpdate.LOG.error("more than one object found for link definition {}", oneLink);
                    }
                }

                final List<Instance> childInsts = new ArrayList<Instance>();
                for (final LinkInstance oneLink : _links) {
                    childInsts.add(oneLink.getChildInstance());
                }

                // 3. look if there are any other links of this type already connected to the parent
                // which are not given explicitly and remove them
                final QueryBuilder attrQueryBldr = new QueryBuilder(Type.get(_linktype.childTypeName));
                final AttributeQuery attrQuery = attrQueryBldr.getAttributeQuery("ID");
                attrQuery.setIncludeChildTypes(_linktype.isIncludeChildTypes());
                final QueryBuilder queryBldr = new QueryBuilder(Type.get(_linktype.linkName));
                queryBldr.addWhereAttrEqValue(_linktype.parentAttrName, _instance);
                queryBldr.addWhereAttrInQuery(_linktype.childAttrName, attrQuery);
                if (!childInsts.isEmpty()) {
                    queryBldr.addWhereAttrNotEqValue(_linktype.childAttrName, childInsts.toArray());
                }
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                while (query.next()) {
                    new Delete(query.getCurrentValue()).executeWithoutTrigger();
                }

                // 4. check if the link must be unique and remove existing links
                if (_linktype instanceof UniqueLink) {
                    // remove also the links from the unique group
                    for (final Link checkLink : ((UniqueLink) _linktype).getUniqueGroup()) {
                        final QueryBuilder unGrpAttrQueryBldr = new QueryBuilder(Type.get(checkLink.childTypeName));
                        final AttributeQuery unGrpAttrQuery = unGrpAttrQueryBldr.getAttributeQuery("ID");
                        unGrpAttrQuery.setIncludeChildTypes(_linktype.isIncludeChildTypes());
                        final QueryBuilder unGrpQueryBldr = new QueryBuilder(Type.get(checkLink.linkName));
                        unGrpQueryBldr.addWhereAttrEqValue(checkLink.parentAttrName, _instance);
                        unGrpQueryBldr.addWhereAttrInQuery(checkLink.childAttrName, unGrpAttrQuery);
                        final InstanceQuery unGrpQuery = unGrpQueryBldr.getQuery();
                        unGrpQuery.executeWithoutAccessCheck();
                        while (unGrpQuery.next()) {
                            new Delete(unGrpQuery.getCurrentValue()).executeWithoutTrigger();
                        }
                    }
                }

                // 5. Insert the links which are not existing yet and update given values
                for (final LinkInstance oneLink : _links) {
                    Update update;
                    boolean exec = false;
                    if (oneLink.getInstance() == null) {
                        update = new Insert(_linktype.linkName);
                        update.add(_linktype.parentAttrName, _instance);
                        update.add(_linktype.childAttrName, oneLink.getChildInstance());
                        exec = true;
                    } else {
                        update = new Update(oneLink.getInstance());
                    }
                    for (final Entry<String, String> entry : oneLink.getValuesMap().entrySet()) {
                        update.add(entry.getKey(), entry.getValue());
                        exec = true;
                    }
                    if (exec) {
                        update.executeWithoutAccessCheck();
                        oneLink.setInstance(update.getInstance());
                    }
                }

                // 6. Order if necessary
                if (_linktype instanceof OrderedLink) {
                    final List<Instance> childOrder = new ArrayList<Instance>();
                    for (final LinkInstance oneLink : _links) {
                        childOrder.add(oneLink.getChildInstance());
                    }
                    final ArrayList<LinkInstance> linkOrder = new ArrayList<LinkInstance>(_links);
                    Collections.sort(linkOrder, new Comparator<LinkInstance>()
                    {
                        @Override
                        public int compare(final LinkInstance _o1,
                                           final LinkInstance _o2)
                        {
                            return Long.valueOf(_o1.getInstance().getId()).compareTo(
                                            Long.valueOf(_o2.getInstance().getId()));
                        }
                    });

                    final Iterator<LinkInstance> sortedIter = linkOrder.iterator();
                    final Iterator<Instance> childIter = childOrder.iterator();
                    while (sortedIter.hasNext()) {
                        final LinkInstance linkInst = sortedIter.next();
                        final Instance childInst = childIter.next();
                        if (!linkInst.getChildInstance().equals(childInst)) {
                            final Update update = new Update(linkInst.getInstance());
                            update.add(_linktype.childAttrName, childInst);
                            update.executeWithoutAccessCheck();
                        }
                    }
                }
            }
        }

        /**
         * The properties are only set if the object to update could own
         * properties (meaning derived from 'Admin_Abstract').
         *
         * @param _instance instance for which the properties must be set
         * @param _properties new properties to set
         * @throws EFapsException if properties could not be set TODO: rework of
         *             the update algorithm (not always a complete delete and
         *             and new create is needed)
         */
        protected void setPropertiesInDb(final Instance _instance,
                                         final Map<String, String> _properties)
            throws EFapsException
        {

            if (_instance.getType().isKindOf(CIAdmin.Abstract.getType())) {
                // remove old properties
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.Property);
                queryBldr.addWhereAttrEqValue(CIAdminCommon.Property.Abstract, _instance.getId());
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                while (query.next()) {
                    new Delete(query.getCurrentValue()).executeWithoutAccessCheck();
                }

                // add current properties
                if (_properties != null) {
                    for (final Map.Entry<String, String> entry : _properties.entrySet()) {
                        final Insert insert = new Insert("Admin_Common_Property");
                        insert.add("Name", entry.getKey());
                        insert.add("Value", entry.getValue());
                        insert.add("Abstract", "" + _instance.getId());
                        insert.executeWithoutAccessCheck();
                    }
                }
            }
        }

        /**
         * @param _link link type
         * @param _linkinstance name of the object which is linked to and values
         *            in the link itself (or null)
         */
        protected void addLink(final Link _link,
                               final LinkInstance _linkinstance)
        {
            Set<LinkInstance> oneLink = this.links.get(_link);
            if (oneLink == null) {
                if (_link instanceof OrderedLink) {
                    oneLink = new LinkedHashSet<LinkInstance>();
                } else {
                    oneLink = new HashSet<LinkInstance>();
                }
                this.links.put(_link, oneLink);
            }
            oneLink.add(_linkinstance);
        }

        /**
         * @param _linkType type the links are wanted for
         * @return Set of links
         */
        public Set<LinkInstance> getLinks(final Link _linkType)
        {
            return this.links.get(_linkType);
        }

        /**
         * @param _name name of the attribute
         * @param _value value of the attribute
         * @see #values
         */
        protected void addValue(final String _name,
                                final String _value)
        {
            this.values.put(_name, _value);
        }

        /**
         * @param _name name of the attribute
         * @return value of the set attribute value in this definition
         * @see #values
         */
        public String getValue(final String _name)
        {
            return this.values.get(_name);
        }

        /**
         * @param _name Name ot set
         */
        protected void setName(final String _name)
        {
            addValue("Name", _name);
        }

        /**
         * Adds a trigger <code>_event</code> to this definition.
         *
         * @param _event trigger event to add
         * @see #events
         */
        protected void addEvent(final Event _event)
        {
            this.events.add(_event);
        }

        /**
         * This is the getter method for the instance variable {@link #events}.
         *
         * @return value of instance variable {@link #events}
         */
        public List<Event> getEvents()
        {
            return this.events;
        }

        /**
         * This is the getter method for the instance variable
         * {@link #properties}.
         *
         * @return value of instance variable {@link #properties}
         */
        public Map<String, String> getProperties()
        {
            return this.properties;
        }

        /**
         * This is the getter method for the instance variable {@link #instance}.
         *
         * @return value of instance variable {@link #instance}
         */
        protected Instance getInstance()
        {
            return this.instance;
        }

        /**
         * This is the setter method for the instance variable {@link #instance}.
         *
         * @param _instance value for instance variable {@link #instance}
         */
        protected void setInstance(final Instance _instance)
        {
            this.instance = _instance;
        }

        /**
         * Getter method for the instance variable {@link #profiles}.
         *
         * @return value of instance variable {@link #profiles}
         */
        public Set<Profile> getProfiles()
        {
            return this.profiles;
        }

        /**
         * Returns a string representation with values of all instance variables
         * of a definition.
         *
         * @return string representation of this definition of an access type
         *         update
         */
        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
