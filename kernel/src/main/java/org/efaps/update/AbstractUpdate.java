/*
 * Copyright 2003-2008 The eFaps Team
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.event.Event;
import org.efaps.util.EFapsException;

/**
 * This class is the major class for importing or updating of types, commands
 * and so on in eFaps.<br/>For every kind of Object in eFaps a own class
 * extends this AbstractUpdate. In this classes the xml-Files is read and with
 * the digester converted in Objects. After reading all Objects of one XML-File
 * the Objects are inserted coresponding to the Version.
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG =
      LoggerFactory.getLogger(AbstractUpdate.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The URL of the xml file is stored in this instance variable.
   */
  private URL url = null;

  /**
   * The name of the data model type is store in this instance variable.
   */
  private final String dataModelTypeName;

  /**
   * All known link types are set to this instance varaible.
   */
  private final Set<Link> allLinkTypes;

  /**
   * The univeral unique identifier of the object is stored in this instance
   * variable.
   *
   * @see #setUUID
   */
  private String uuid = null;

  /**
   * All definitions of versions are added to this list.
   */
  private final List<AbstractDefinition> definitions =
      new ArrayList<AbstractDefinition>();

  /**
   * This instance variable stores if the Type to be updated is abstract or not
   */
  private boolean abstractType = false;

  private String application;

  private Long maxVersion;

  private String rootDir;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  protected AbstractUpdate(final String _dataModelTypeName) {
    this(_dataModelTypeName, null);
  }

  /**
   *
   */
  protected AbstractUpdate(final String _dataModelTypeName,
                           final Set<Link> _allLinkTypes) {
    this.dataModelTypeName = _dataModelTypeName;
    this.allLinkTypes = _allLinkTypes;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Adds one definition of a update for a specific version to all definitions
   * in {@link #definitions}.
   *
   * @param _definition
   *                definition to add
   * @see #definitions
   */
  public void addDefinition(final AbstractDefinition _definition) {
    this.definitions.add(_definition);
  }

  /**
   * The instance method returns the eFaps instance representing the read XML
   * configuration. If not already get from the eFaps databasse, the information
   * is read. If no instance exists in the database, a new one is automatically
   * created. The method searchs for the given universal unique identifier in
   * {@link #uuid} the instance in the eFaps database and stores the result in
   * {@link #instance}. If no object is found in eFaps, {@link #instance} is
   * set to <code>null</code>. A new instance is created in the eFaps db for
   * given univeral unique identifier in {@link #uuid}. The name of the access
   * set is also the universal unique identifier, because the name of access set
   * is first updates in the version definition.<br/> The new created object is
   * stored as instance information in {@link #instance}.
   *
   * @param _jexlContext
   *                expression context used to evaluate
   */
  public void updateInDB(final JexlContext _jexlContext) throws EFapsException,
                                                        Exception {
    try {

      for (final AbstractDefinition def : this.definitions) {

        final Expression jexlExpr =
            ExpressionFactory.createExpression(def.mode);
        final boolean exec =
            Boolean.parseBoolean((jexlExpr.evaluate(_jexlContext).toString()));
        if (exec) {
          if ((this.url != null) && LOG.isInfoEnabled()) {
            LOG.info("Executing '" + this.url.toString() + "'");
          }
          def.updateInDB(Type.get(this.dataModelTypeName), this.uuid,
              this.allLinkTypes, this.abstractType);
        }
      }
    } catch (final Exception e) {
      LOG.error("updateInDB", e);
      throw e;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * This is the getter method for the instance variable {@link #url}.
   *
   * @return value of instance variable {@link #url}
   */
  public URL getURL() {
    return this.url;
  }

  /**
   * @see #url
   */
  protected void setURL(final URL _url) {
    this.url = _url;
  }

  /**
   * @see #uuid
   */
  public void setUUID(final String _uuid) {
    this.uuid = _uuid;
  }

  /**
   * This is the getter method for instance variable {@link #uuid}.
   *
   * @return value of instance variable {@link #uuid}
   * @see #uuid
   * @see #setUUID
   */
  public String getUUID() {
    return this.uuid;
  }

  /**
   * This is the getter method for the instance variable {@link #abstractType}.
   *
   * @return value of instance variable {@link #abstractType}
   */

  public boolean isAbstractType() {
    return this.abstractType;
  }

  /**
   * This is the setter method for the instance variable {@link #abstractType}.
   *
   * @param _abstractType
   *                the abstractType to set
   */
  public void setAbstractType(final String _abstractType) {
    this.abstractType = Boolean.parseBoolean(_abstractType);
  }

  /**
   * This is the getter method for instance variable {@link #definitions}.
   *
   * @return value of instance variable {@link #definitions}
   * @see #definitions
   */
  protected List<AbstractDefinition> getDefinitions() {
    return this.definitions;
  }

  /**
   * This is the getter method for the instance variable {@link #application}.
   *
   * @return value of instance variable {@link #application}
   */
  public String getApplication() {
    return this.application;
  }

  /**
   * This is the setter method for the instance variable {@link #application}.
   *
   * @param application
   *                the application to set
   */
  public void setApplication(String application) {
    this.application = application;
  }

  /**
   * This is the getter method for the instance variable
   * {@link #dataModelTypeName}.
   *
   * @return value of instance variable {@link #dataModelTypeName}
   */
  public String getDataModelTypeName() {
    return this.dataModelTypeName;
  }

  /**
   * This is the getter method for the instance variable {@link #maxVersion}.
   *
   * @return value of instance variable {@link #maxVersion}
   */
  public Long getMaxVersion() {
    return this.maxVersion;
  }

  /**
   * This is the setter method for the instance variable {@link #maxVersion}.
   *
   * @param maxVersion
   *                the maxVersion to set
   */
  public void setMaxVersion(Long maxVersion) {
    this.maxVersion = maxVersion;
  }

  /**
   * This is the getter method for the instance variable {@link #rootDir}.
   *
   * @return value of instance variable {@link #rootDir}
   */
  public String getRootDir() {
    return this.rootDir;
  }

  /**
   * This is the setter method for the instance variable {@link #rootDir}.
   *
   * @param rootDir
   *                the rootDir to set
   */
  public void setRootDir(String rootDir) {
    this.rootDir = rootDir;
  }

  /**
   * This is the getter method for the instance variable {@link #allLinkTypes}.
   *
   * @return value of instance variable {@link #allLinkTypes}
   */
  protected Set<Link> getAllLinkTypes() {
    return this.allLinkTypes;
  }

  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this abstract update
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("uuid", this.uuid).append(
        "definitions", this.definitions).toString();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static classes

  /**
   * The class is used to define the links with all information needed to update
   * the link information between the object to update and the related objects.
   *
   * @see #setLinksInDB
   */
  static protected class Link {

    /** Name of the link. */
    private final String linkName;

    /**
     * Name of the parent attribute in the link. The parent attribute stores the
     * id of the objecto udpate.
     */
    private final String parentAttrName;

    /**
     * Name of the child type used to query for the given name to which a link
     * must be set.
     */
    private final String childTypeName;

    /** Name of the child attribute in the link. */
    private final String childAttrName;

    /**
     * Constructor used to initialise the instance variables.
     *
     * @param _linkName
     *                name of the link itself
     * @param _parentAttrName
     *                name of the parent attribute in the link
     * @param _childTypeName
     *                name of the child type
     * @param _childAttrName
     *                name of the child attribute in the link
     * @see #linkName
     * @see #parentAttrName
     * @see #childTypeName
     * @see #childAttrName
     */
    public Link(final String _linkName, final String _parentAttrName,
                final String _childTypeName, final String _childAttrName) {
      this.linkName = _linkName;
      this.parentAttrName = _parentAttrName;
      this.childTypeName = _childTypeName;
      this.childAttrName = _childAttrName;
    }

    /**
     * Child Type extracted from the child type name.
     *
     * @return child type extracted from the child type name
     */
    public Type getChildType() {
      return Type.get(this.childTypeName);
    }

    /**
     * Link Type extracted from the link name.
     *
     * @return link type extracted from the link name
     */
    public Type getLinkType() {
      return Type.get(this.linkName);
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a link.
     *
     * @return string representation of this link
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("linkName", this.linkName)
          .append("parentAttrName", this.parentAttrName).append(
              "childTypeName", this.childTypeName).append("childAttrName",
              this.childAttrName).toString();
    }
  }

  /**
   * Some links has a order in the database. This means that the connections
   * must be made in the order they are defined in the xml update file.
   */
  static protected class OrderedLink extends Link {

    public OrderedLink(final String _linkName, final String _parentAttrName,
                       final String _childTypeName, final String _childAttrName) {
      super(_linkName, _parentAttrName, _childTypeName, _childAttrName);
    }
  }

  /**
   *
   */
  protected abstract static class AbstractDefinition {

    /**
     * Name of the application for which this definition is defined.
     *
     * @see #setVersion
     */
    private String application = null;

    /**
     * Number of the global version of the application.
     *
     * @see #setVersion
     */
    private long globalVersion = 0;

    /**
     * Text of the local version of this definition.
     *
     * @see #setVersion
     */
    private String localVersion = null;

    /**
     * @see #setVersion
     */
    private String mode = null;

    /**
     * The value depending on the attribute name for this definition.
     *
     * @see #addValue
     * @see #getValue
     */
    private final Map<String, String> values = new HashMap<String, String>();

    /**
     * Property value depending on the property name for this definition
     *
     * @see #addProperty.
     */
    private final Map<String, String> properties =
        new HashMap<String, String>();

    /**
     *
     */
    private final Map<Link, Map<String, Map<String, String>>> links =
        new HashMap<Link, Map<String, Map<String, String>>>();

    protected final List<Event> events = new ArrayList<Event>();

    public void updateInDB(final Type _dataModelType, final String _uuid,
                           final Set<Link> _allLinkTypes,
                           final boolean _abstractType) throws EFapsException,
                                                       Exception {
      Instance instance = null;
      Insert insert = null;

      // search for the instance
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(_dataModelType.getName());
      query.addWhereExprEqValue("UUID", _uuid);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        instance = new Instance((String) query.get("OID"));
      }
      query.close();

      // if no instance exists, a new insert must be done
      if (instance == null) {
        insert = new Insert(_dataModelType);
        insert.add("UUID", _uuid);
        if (insert.getInstance().getType().getAttribute("Abstract") != null) {
          insert.add("Abstract", ((Boolean) _abstractType).toString());
        }
      }

      updateInDB(instance, _allLinkTypes, insert);
    }

    /**
     *
     */
    public Instance updateInDB(final Instance _instance,
                               final Set<Link> _allLinkTypes,
                               final Insert _insert) throws EFapsException,
                                                    Exception {
      Instance instance = _instance;

      if (_insert == null) {
        final String name = this.values.get("Name");
        final Update update = new Update(_instance);
        if (_instance.getType().getAttribute("Revision") != null) {
          update.add("Revision", this.globalVersion + "#" + this.localVersion);
        }
        for (final Map.Entry<String, String> entry : this.values.entrySet()) {
          update.add(entry.getKey(), entry.getValue());
        }
        if (LOG.isInfoEnabled() && (name != null)) {
          LOG.info("    Update "
              + _instance.getType().getName()
              + " "
              + "'"
              + name
              + "'");
        }
        update.executeWithoutAccessCheck();

      } else {

        if (_insert.getInstance().getType().getAttribute("Revision") != null) {
          _insert.add("Revision", this.globalVersion + "#" + this.localVersion);
        }
        final String name = this.values.get("Name");
        if (name == null) {
          _insert.add("Name", "-");
        }
        for (final Map.Entry<String, String> entry : this.values.entrySet()) {
          _insert.add(entry.getKey(), entry.getValue());
        }
        if (LOG.isInfoEnabled() && (name != null)) {
          LOG.info("    Insert "
              + _insert.getInstance().getType().getName()
              + " "
              + "'"
              + name
              + "'");
        }
        _insert.executeWithoutAccessCheck();
        instance = _insert.getInstance();
      }
      if (_allLinkTypes != null) {
        for (final Link linkType : _allLinkTypes) {
          setLinksInDB(instance, linkType, this.links.get(linkType));
        }
      }
      setPropertiesInDb(instance, this.properties);

      for (final Event event : this.events) {
        final Instance newInstance =
            event.updateInDB(instance, getValue("Name"));
        setPropertiesInDb(newInstance, event.getProperties());
      }

      return instance;
    }

    /**
     * Sets the links from this object to the given list of objects (with the
     * object name) in the eFaps database.
     *
     * @param _instance
     *                instance for which the access types must be set
     * @param _link
     *                link to update
     * @param _objNames
     *                string list of all object names to set for this object
     * @todo it could be that more than one current from same target is defined!
     *       E.g. menu to child!
     * @todo ordered link is only a hack, the current connection are always
     *       disconnected
     */
    protected void setLinksInDB(final Instance _instance, final Link _link,
                                final Map<String, Map<String, String>> _links)
                                                                              throws EFapsException,
                                                                              Exception {
      // get ids from current object
      final Map<Long, String> currents = new HashMap<Long, String>();
      SearchQuery query = new SearchQuery();
      query.setExpand(_instance, _link.linkName + "\\" + _link.parentAttrName);
      query.addSelect(_link.childAttrName + ".ID");
      query.addSelect("OID");
      query.addSelect("Type");
      query.addSelect(_link.childAttrName + ".Type");
      query.executeWithoutAccessCheck();
      while (query.next()) {
        final Type typeLink = (Type) query.get("Type");
        final Type typeChild = (Type) query.get(_link.childAttrName + ".Type");
        if (typeLink.isKindOf(_link.getLinkType())
            && typeChild.isKindOf(_link.getChildType())) {
          if (_link instanceof OrderedLink) {
            final Delete del = new Delete((String) query.get("OID"));
            del.executeWithoutAccessCheck();
          } else {
            currents.put((Long) query.get(_link.childAttrName + ".ID"),
                (String) query.get("OID"));
          }
        }
      }
      query.close();
      // get ids for target
      Map<Long, Map<String, String>> targets;
      if (_link instanceof OrderedLink) {
        targets = new LinkedHashMap<Long, Map<String, String>>();
      } else {
        targets = new HashMap<Long, Map<String, String>>();
      }
      if (_links != null) {
        for (final Map.Entry<String, Map<String, String>> linkEntry : _links
            .entrySet()) {
          query = new SearchQuery();
          query.setQueryTypes(_link.childTypeName);
          query.setExpandChildTypes(true);
          query.addWhereExprEqValue("Name", linkEntry.getKey());
          query.addSelect("ID");
          query.executeWithoutAccessCheck();
          if (query.next()) {
            targets.put((Long) query.get("ID"), linkEntry.getValue());
          } else {
            LOG.error(_link.childTypeName
                + " '"
                + linkEntry.getKey()
                + "' not found!");
          }
          query.close();
        }
      }

      // insert needed new links and update already existing
      for (final Map.Entry<Long, Map<String, String>> target : targets
          .entrySet()) {
        if (currents.get(target.getKey()) == null) {
          final Insert insert = new Insert(_link.linkName);
          insert.add(_link.parentAttrName, "" + _instance.getId());
          insert.add(_link.childAttrName, "" + target.getKey());
          if (target.getValue() != null) {
            for (final Map.Entry<String, String> value : target.getValue()
                .entrySet()) {
              insert.add(value.getKey(), value.getValue());
            }
          }
          insert.executeWithoutAccessCheck();
        } else {
          if (target.getValue() != null) {
            final Update update = new Update(currents.get(target.getKey()));
            for (final Map.Entry<String, String> value : target.getValue()
                .entrySet()) {
              update.add(value.getKey(), value.getValue());
            }
            update.executeWithoutAccessCheck();
          }
          currents.remove(target.getKey());
        }
      }

      // remove unneeded current links to access types
      for (final String oid : currents.values()) {
        final Delete del = new Delete(oid);
        del.executeWithoutAccessCheck();
      }
    }

    /**
     * The properties are only set if the object to update could own properties
     * (meaning derived from 'Admin_Abstract').
     *
     * @param _instance
     *                instance for which the propertie must be set
     * @param _properties
     *                new properties to set
     * @todo rework of the update algorithmus (not always a complete delete and
     *       and new create is needed)
     * @todo description
     */
    protected void setPropertiesInDb(final Instance _instance,
                                     final Map<String, String> _properties)
                                                                           throws EFapsException,
                                                                           Exception {

      if (_instance.getType().isKindOf(Type.get("Admin_Abstract"))) {
        // remove old properties
        final SearchQuery query = new SearchQuery();
        query.setExpand(_instance, "Admin_Common_Property\\Abstract");
        query.addSelect("OID");
        query.executeWithoutAccessCheck();
        while (query.next()) {
          final String propOid = (String) query.get("OID");
          final Delete del = new Delete(propOid);
          del.executeWithoutAccessCheck();
        }
        query.close();

        // add current properites
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
     * The version information of this defintion is set.
     *
     * @param _application
     *                name of the application for which the version is defined
     * @param _globalVersion
     *                global version
     */
    public void setVersion(final String _application,
                           final String _globalVersion,
                           final String _localVersion, final String _mode) {
      this.application = _application;
      this.globalVersion = Long.valueOf(_globalVersion);
      this.localVersion = _localVersion;
      this.mode = _mode;
    }

    /**
     * @param _link
     *                link type
     * @param _name
     *                name of the object which is linked to
     * @param _values
     *                values in the link itself (or null)
     */
    protected void addLink(final Link _link, final String _name,
                           final Map<String, String> _values) {
      Map<String, Map<String, String>> oneLink = this.links.get(_link);
      if (oneLink == null) {
        if (_link instanceof OrderedLink) {
          oneLink = new LinkedHashMap<String, Map<String, String>>();
        } else {
          oneLink = new HashMap<String, Map<String, String>>();
        }
        this.links.put(_link, oneLink);
      }
      oneLink.put(_name, _values);
    }

    /**
     * @param _link
     *                link type
     * @param _name
     *                name of the object which is linked to
     * @param _values
     *                values in the link itself (or null)
     */
    protected void addLink(final Link _link, final String _name,
                           final String... _values) {
      Map<String, String> valuesMap = null;
      if (_values.length > 0) {
        valuesMap = new HashMap<String, String>();
        for (int i = 0; i < _values.length; i += 2) {
          valuesMap.put(_values[i], _values[i + 1]);
        }
      }
      addLink(_link, _name, valuesMap);
    }

    public Map<String, Map<String, String>> getLinks(final Link _linkType) {
      return this.links.get(_linkType);
    }

    /**
     * @param _name
     *                name of the attribute
     * @param _value
     *                value of the attribute
     * @see #values
     */
    protected void addValue(final String _name, final String _value) {
      this.values.put(_name, _value);
    }

    /**
     * @param _name
     *                name of the attribtue
     * @return value of the set attribute value in this definition
     * @see #values
     */
    protected String getValue(final String _name) {
      return this.values.get(_name);
    }

    /**
     * Add a new property with given name and value to this definition.
     *
     * @param _name
     *                name of the property to add
     * @param _value
     *                value of the property to add
     * @see #properties
     */
    public void addProperty(final String _name, final String _value) {
      this.properties.put(_name, _value);
    }

    /**
     * @see #values
     */
    public void setName(final String _name) {
      addValue("Name", _name);
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a definition.
     *
     * @return string representation of this definition of an access type update
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("application", this.application)
          .append("global version", this.globalVersion).append("local version",
              this.localVersion).append("mode", this.mode).append("values",
              this.values).append("properties", this.properties).append(
              "links", this.links).toString();
    }

    /**
     * add a <code>Trigger</code> to this definition
     *
     * @param _event
     */
    public void addEvent(final Event _event) {
      this.events.add(_event);
    }

    /**
     * This is the getter method for the instance variable {@link #events}.
     *
     * @return value of instance variable {@link #events}
     */
    public List<Event> getEvents() {
      return this.events;
    }

    /**
     * This is the getter method for the instance variable {@link #properties}.
     *
     * @return value of instance variable {@link #properties}
     */
    public Map<String, String> getProperties() {
      return this.properties;
    }

  }




}
