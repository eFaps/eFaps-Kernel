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

package org.efaps.update;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.event.Event;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class AbstractUpdate
{
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(AbstractUpdate.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

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

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  protected AbstractUpdate(final URL _url,
                           final String _dataModelTypeName)
  {
    this(_url, _dataModelTypeName, null);
  }

  /**
   *
   */
  protected AbstractUpdate(final URL _url,
                           final String _dataModelTypeName,
                           final Set<Link> _allLinkTypes)
  {
    this.url = _url;
    this.dataModelTypeName = _dataModelTypeName;
    this.allLinkTypes = _allLinkTypes;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Read event for given tags path with attributes and text.
   *
   * @param _tags         tags path as list
   * @param _attributes   map of attributes for current tag
   * @param _text         content text of this tags path
   * @todo error could not be thrown because db properties is not read correctly
   */
  public void readXML(final List<String> _tags,
                      final Map<String,String> _attributes,
                      final String _text)
  {
    if (_tags.size() == 1)  {
      final String value = _tags.get(0);
      if ("uuid".equals(value))  {
        this.uuid = _text;
      } else if ("file-application".equals(value))  {
        this.fileApplication = _text;
      } else if ("file-revision".equals(value))  {
        this.fileRevision = _text;
      } else if ("definition".equals(value))  {
        this.definitions.add(newDefinition());
      }
    } else if ("definition".equals(_tags.get(0))) {
      final AbstractDefinition curDef = this.definitions.get(this.definitions.size() - 1);
      curDef.readXML(_tags.subList(1, _tags.size()),
                     _attributes,
                     _text);
    } else  {
//      throw new Error("Unknown Tag '" + _tags + "' (file " + this.url + ")");
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
   * @param _definition   definition to add
   * @see #definitions
   */
  protected void addDefinition(final AbstractDefinition _definition)
  {
    this.definitions.add(_definition);
  }

  /**
   * The instance method returns the eFaps instance representing the read XML
   * configuration. If not already get from the eFaps database, the information
   * is read. If no instance exists in the database, a new one is automatically
   * created. The method searches for the given universal unique identifier in
   * {@link #uuid} the instance in the eFaps database and stores the result in
   * {@link #instance}. If no object is found in eFaps, {@link #instance} is
   * set to <code>null</code>. A new instance is created in the eFaps DB for
   * given universal unique identifier in {@link #uuid}. The name of the access
   * set is also the universal unique identifier, because the name of access set
   * is first updates in the version definition.<br/> The new created object is
   * stored as instance information in {@link #instance}.
   *
   * @param _jexlContext  context used to evaluate JEXL expressions
   * @throws EFapsException from called update methods
   */
  public void updateInDB(final JexlContext _jexlContext)
      throws EFapsException
  {
    try {
      for (final AbstractDefinition def : this.definitions) {
        if (def.isValidVersion(_jexlContext)) {
          if ((this.url != null) && LOG.isDebugEnabled()) {
            LOG.debug("Executing '" + this.url.toString() + "'");
          }
          def.updateInDB(this.allLinkTypes);
        }
      }
    } catch (final EFapsException e) {
      LOG.error("updateInDB", e);
      throw e;
    }
  }

  /**
   *
   * @param _jexlContext  context used to evaluate JEXL expressions
   * @throws EFapsException from called create methods
   */
  public void createInDB(final JexlContext _jexlContext)
      throws EFapsException
  {
    try {
      for (final AbstractDefinition def : this.definitions) {
        if (def.isValidVersion(_jexlContext)) {
          if ((this.url != null) && LOG.isDebugEnabled()) {
            LOG.debug("Executing '" + this.url.toString() + "'");
          }
          def.createInDB();
        }
      }
    } catch (final EFapsException e) {
      LOG.error("createInDB", e);
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
  protected URL getURL()
  {
    return this.url;
  }

  /**
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
  protected String getUUID()
  {
    return this.uuid;
  }

  /**
   * This is the setter method for instance variable {@link #fileApplication}.
   *
   * @param _fileApplication    new value for instance variable
   *                            {@link #fileApplication}
   * @see #fileApplication
   * @see #getFileApplication
   */
  public void setFileApplication(final String _fileApplication) {
    this.fileApplication = _fileApplication;
  }

  /**
   * This is the getter method for instance variable {@link #fileApplication}.
   *
   * @return value of instance variable {@link #fileApplication}
   * @see #fileApplication
   * @see #setFileApplication
   */
  public String getFileApplication() {
    return this.fileApplication;
  }

  /**
   * This is the setter method for instance variable {@link #fileRevision}.
   *
   * @param _fileRevision   new value for instance variable
   *                        {@link #fileRevision}
   * @see #fileRevision
   * @see #getFileRevision
   */
  public void setFileRevision(final String _fileRevision) {
    this.fileRevision = _fileRevision;
  }

  /**
   * This is the getter method for instance variable {@link #fileRevision}.
   *
   * @return value of instance variable {@link #fileRevision}
   * @see #fileRevision
   * @see #setFileRevision
   */
  public String getFileRevision() {
    return this.fileRevision;
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
   * This is the getter method for the instance variable
   * {@link #dataModelTypeName}.
   *
   * @return value of instance variable {@link #dataModelTypeName}
   */
  public String getDataModelTypeName() {
    return this.dataModelTypeName;
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
    return new ToStringBuilder(this)
            .append("uuid", this.uuid)
            .append("fileApplication", this.fileApplication)
            .append("fileRevision", this.fileRevision)
            .append("definitions", this.definitions)
            .toString();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static classes

  /**
   * The class is used to define the links with all information needed to update
   * the link information between the object to update and the related objects.
   *
   * @see #setLinksInDB
   */
  static protected class Link
  {
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
     * Constructor used to initialize the instance variables.
     *
     * @param _linkName         name of the link itself
     * @param _parentAttrName   name of the parent attribute in the link
     * @param _childTypeName    name of the child type
     * @param _childAttrName    name of the child attribute in the link
     * @see #linkName
     * @see #parentAttrName
     * @see #childTypeName
     * @see #childAttrName
     */
    public Link(final String _linkName,
                final String _parentAttrName,
                final String _childTypeName,
                final String _childAttrName)
    {
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
    public Type getChildType()
    {
      return Type.get(this.childTypeName);
    }

    /**
     * Link Type extracted from the link name.
     *
     * @return link type extracted from the link name
     */
    public Type getLinkType()
    {
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

    public OrderedLink(final String _linkName,
                       final String _parentAttrName,
                       final String _childTypeName,
                       final String _childAttrName)
    {
      super(_linkName, _parentAttrName, _childTypeName, _childAttrName);
    }
  }

  /**
   *
   */
  protected abstract class AbstractDefinition
  {
    /**
     * Expression of this definition if this definition must be installed.
     *
     * @see #readXML(List, Map, String)     defines this expression
     * @see #isValidVersion(JexlContext)    test this expression
     */
    private String expression = null;

    /**
     * this instance variable stores the type of Definition (default: "replace")
     * possible values: "replace", "update"
     *
     * @see #getType()
     * @see #setType(String)
     */
    private String type = "replace";

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
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     *
     */
    private final Map<Link, Set<LinkInstance>> links = new HashMap<Link, Set<LinkInstance>>();

    protected final List<Event> events = new ArrayList<Event>();

    /**
     * Name of attribute by which the search in the database is done. If not
     * specified (defined to <code>null</code>), the attribute &quot;UUID&quot;
     * is used.
     *
     * @see #searchInstance
     */
    private final String searchAttrName;

    /**
     * Instance of this definition.
     */
    protected Instance instance = null;

    /**
     * Default constructor for the attribute by which the object is
     * searched is &quot;UUID&quot;.
     */
    protected AbstractDefinition()
    {
      this(null);
    }

    /**
     * Constructor defining the search attribute.
     *
     * @param _searchAttrName   name of attribute by which the object is
     *                          searched
     * @see #searchInstance   method using the search attribute
     * @see #searchAttrName
     */
    protected AbstractDefinition(final String _searchAttrName)
    {
      this.searchAttrName = _searchAttrName;
    }

    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("name".equals(value))  {
        setName(_text);
      } else if ("property".equals(value))  {
        this.properties.put(_attributes.get("name"), _text);
      } else if ("version-expression".equals(value))  {
        this.expression = _text;
      } else  {
throw new Error("Unknown Tag '" + _tags + "' (file " + AbstractUpdate.this.url + ")");
      }
    }

    /**
     * Evaluates the JEXP expression defined in {@link #expression}. If this
     * expression returns true, the definition is a valid version and could
     * be executed.
     *
     * @param _jexlContext    context used to evaluate JEXL expressions
     * @return <i>true</i> if the definition is valid
     * @throws EFapsException if the JEXL expression in {@link #expression} could
     *                        not be evaluated
     * @see #expression
     */
    public boolean isValidVersion(final JexlContext _jexlContext)
        throws EFapsException
    {
      boolean exec;
      try {
        if (this.expression == null)  {
          final Expression jexlExpr = ExpressionFactory.createExpression("version==latest");
          exec = Boolean.parseBoolean((jexlExpr.evaluate(_jexlContext).toString()));
        } else  {
          final Expression jexlExpr = ExpressionFactory.createExpression(this.expression);
          exec = Boolean.parseBoolean((jexlExpr.evaluate(_jexlContext).toString()));
        }
      } catch (Exception e) {
        throw new EFapsException(getClass(),
                                 "isValidVersion.JEXLExpressionNotEvaluatable",
                                 e,
                                 this.expression);
      }
      return exec;
    }

    /**
     * Search for given data model type. If an attribute name for the search
     * is defined in {@link #searchAttrName}, the search is done with this
     * given attribute. If no attribute name is defined (value is
     * <code>null</code>, the method searches for given UUID. The result is
     * stored in {@link #instance}�(or set to null, if not found).<br/>
     * The search is only done, if no instance is defined (meaning if
     * {@link #instance} has no value).
     *
     * @see #instance         variable in which the search result is stored
     *                        (and the search is only done if the value is
     *                        <code>null</code>)
     * @see #searchAttrName   name of the attribute which them the search is
     *                        done
     */
    protected void searchInstance()
         throws EFapsException
     {
       if (this.instance == null)  {
         final SearchQuery query = new SearchQuery();
         query.setQueryTypes(AbstractUpdate.this.dataModelTypeName);
         if (this.searchAttrName == null)  {
           query.addWhereExprEqValue("UUID", AbstractUpdate.this.uuid);
         } else  {
           query.addWhereExprEqValue(this.searchAttrName,
                                     this.values.get(this.searchAttrName));
         }
         query.addSelect("OID");
         query.executeWithoutAccessCheck();
         if (query.next()) {
           this.instance = Instance.get((String) query.get("OID"));
         }
         query.close();
       }
     }

    public void createInDB()
        throws EFapsException
    {
      searchInstance();

      // if no instance exists, a new insert must be done
      if (this.instance == null) {
        final Insert insert = new Insert(AbstractUpdate.this.dataModelTypeName);
        insert.add("UUID", AbstractUpdate.this.uuid);
        createInDB(insert);
      }
    }

    protected void createInDB(final Insert _insert)
        throws EFapsException
    {
      if (_insert.getInstance().getType().getAttribute("Revision") != null) {
        _insert.add("Revision", AbstractUpdate.this.fileRevision);
      }
      final String name = this.values.get("Name");
      _insert.add("Name", (name == null) ? "-" : name);
  //    if (LOG.isInfoEnabled() && (name != null)) {
        LOG.info("    Insert "
            + _insert.getInstance().getType().getName()
            + " '" + name + "'");
 //     }
      _insert.executeWithoutAccessCheck();
      this.instance = _insert.getInstance();
    }

    /**
     * @param _allLinkTypes
     */
    protected void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException
    {
      final String name = this.values.get("Name");
      final Update update = new Update(this.instance);
      if (this.instance.getType().getAttribute("Revision") != null)  {
        update.add("Revision", AbstractUpdate.this.fileRevision);
      }
      for (final Map.Entry<String, String> entry : this.values.entrySet())  {
        update.add(entry.getKey(), entry.getValue());
      }
      if (LOG.isInfoEnabled() && (name != null)) {
        LOG.info("    Update "
            + this.instance.getType().getName()
            + " '"
            + name
            + "'");
      }
      update.executeWithoutAccessCheck();

      if (_allLinkTypes != null) {
        for (final Link linkType : _allLinkTypes) {
          setLinksInDB(this.instance, linkType, this.links.get(linkType));
        }
      }
      setPropertiesInDb(this.instance, this.properties);

      for (final Event event : this.events) {
        final Instance newInstance =
            event.updateInDB(this.instance, getValue("Name"));
        setPropertiesInDb(newInstance, event.getProperties());
      }
    }

    /**
     * Remove all links from given object (defined by the instance).
     *
     * @param _instance   instance for which all links must be removed
     * @param _linkType   type of link which must be removed
     * @throws EFapsException if existing links could not be removed (deleted)
     * @see #setLinksInDB used to remove all links for given instance with a
     *                    zero length set of link instances
     */
    protected void removeLinksInDB(final Instance _instance,
                                   final Link _linkType)
        throws EFapsException
    {
      setLinksInDB(_instance, _linkType, new HashSet<LinkInstance>());
    }

    /**
     * Sets the links from this object to the given list of objects (with the
     * object name) in the eFaps database.
     *
     * @param _instance   instance for which the links must be defined
     * @param _linktype   type of the link to be updated
     * @param _links      all links of the type _linktype which will be
     *                    connected to this instance
     * @throws EFapsException if links could not be defined
     */
    protected void setLinksInDB(final Instance _instance,
                                final Link _linktype,
                                final Set<LinkInstance> _links)
        throws EFapsException
    {

      final Map<Long, LinkInstance> existing =
          new HashMap<Long, LinkInstance>();

      boolean order = false;
      // only if new Links are given something must be done
      if (_links != null) {

        final List<LinkInstance> allLinks = new ArrayList<LinkInstance>();

        // add the existing Links as LinkInstance to the List of all Links
        SearchQuery query = new SearchQuery();
        query.setExpand(_instance, _linktype.linkName + "\\" + _linktype.parentAttrName);
        query.addSelect("OID");
        query.addSelect("Type");
        query.addSelect("ID");
        query.addSelect(_linktype.childAttrName + ".ID");
        query.addSelect(_linktype.childAttrName + ".Type");
        query.addSelect(_linktype.childAttrName + ".Name");
        query.executeWithoutAccessCheck();
        while (query.next()) {
          final Type type = (Type) query.get("Type");
          final Type childType =
              (Type) query.get(_linktype.childAttrName + ".Type");
          // check if this is a correct Link for this LinkType
          if (type.isKindOf(_linktype.getLinkType())
              && childType.isKindOf(_linktype.getChildType())) {

            final LinkInstance oldLink =
                new LinkInstance((String) query.get(_linktype.childAttrName
                    + ".Name"));
            final long childId =
                (Long) query.get(_linktype.childAttrName + ".ID");
            oldLink.setChildId(childId);
            oldLink.setOid((String) query.get("OID"));
            oldLink.setId((Long) query.get("ID"));
            allLinks.add(oldLink);
            existing.put(childId, oldLink);
          }
        }
        query.close();

        // add the new LinkInstances to the List of all Linkinstances
        for (final LinkInstance onelink : _links) {

          // search the id for the Linked Object
          query = new SearchQuery();
          query.setQueryTypes(_linktype.childTypeName);
          query.setExpandChildTypes(true);
          query.addWhereExprEqValue("Name", onelink.getName());
          query.addSelect("ID");
          query.executeWithoutAccessCheck();
          if (query.next()) {
            final Long id = (Long) query.get("ID");
            if (id != null) {
              boolean add = true;
              if (existing.get(id) != null) {
                existing.get(id).setUpdate(true);
                existing.get(id).setValues(onelink.getValuesMap());
                add = false;
              }
              if (add) {
                onelink.setChildId(id);
                onelink.setInsert(true);
                if (onelink.getOrder() == 0) {
                  allLinks.add(onelink);
                } else {
                  allLinks.add(onelink.getOrder() - 1, onelink);
                  order = true;
                }
              }
            }
          } else {
            LOG.error(_linktype.childTypeName
                + " '"
                + onelink.getName()
                + "' not found!");
          }
          query.close();
        }
        final Map<Long, LinkInstance> orderid =
            new TreeMap<Long, LinkInstance>();
        if (order) {
          for (final LinkInstance onelink : allLinks) {
            if (onelink.getId() != null) {
              orderid.put(onelink.getId(), onelink);
            }
          }
          int i = 0;
          for (final Entry<Long, LinkInstance> entry : orderid.entrySet()) {
            // if they are the same don't do anything
            if (!entry.getValue().equals(allLinks.get(i))) {
              final Update update = new Update(entry.getValue().getOid());
              update.add(_linktype.childAttrName, ""
                  + allLinks.get(i).getChildId());
              update.executeWithoutAccessCheck();
            }
            i++;
          }

          for (int j = i; j < allLinks.size(); j++) {
            final Insert insert = new Insert(_linktype.linkName);
            insert.add(_linktype.parentAttrName, "" + _instance.getId());
            insert.add(_linktype.childAttrName, ""
                + allLinks.get(j).getChildId());
            insert.executeWithoutAccessCheck();
          }

        } else {

          // insert, update the LinkInstances or in case of replace remove them
          for (final LinkInstance onelink : allLinks) {
            if (onelink.isUpdate()) {
              final Update update =
                  new Update(_linktype.getChildType(), onelink.getChildId()
                      .toString());

              for (final Map.Entry<String, String> value : onelink
                  .getValuesMap().entrySet()) {
                update.add(value.getKey(), value.getValue());
              }
              update.executeWithoutAccessCheck();
            } else if (onelink.isInsert()) {
              final Insert insert = new Insert(_linktype.linkName);
              insert.add(_linktype.parentAttrName, "" + _instance.getId());
              insert.add(_linktype.childAttrName, "" + onelink.getChildId());

              for (final Map.Entry<String, String> value : onelink
                  .getValuesMap().entrySet()) {
                insert.add(value.getKey(), value.getValue());
              }
              insert.executeWithoutAccessCheck();
              onelink.setOid(insert.getInstance().getOid());
            } else {
              if (!getType().equals("update") && onelink.getOid() != null) {
                final Delete del = new Delete(onelink.getOid());
                del.executeWithoutAccessCheck();
              }
            }

          }

        }
      }

    }

    /**
     * The properties are only set if the object to update could own properties
     * (meaning derived from 'Admin_Abstract').
     *
     * @param _instance     instance for which the properties must be set
     * @param _properties   new properties to set
     * @throws EFapsException if properties could not be set
     * @todo rework of the update algorithm (not always a complete delete and
     *       and new create is needed)
     * @todo description
     */
    protected void setPropertiesInDb(final Instance _instance,
                                     final Map<String, String> _properties)
        throws EFapsException
    {

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
     * @param _link       link type
     * @param _name       name of the object which is linked to
     * @param _values     values in the link itself (or null)
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

    public Set<LinkInstance> getLinks(final Link _linkType)
    {
      return this.links.get(_linkType);
    }

    /**
     * @param _name   name of the attribute
     * @param _value  value of the attribute
     * @see #values
     */
    protected void addValue(final String _name,
                            final String _value)
    {
      this.values.put(_name, _value);
    }

    /**
     * @param _name   name of the attribtue
     * @return value of the set attribute value in this definition
     * @see #values
     */
    protected String getValue(final String _name)
    {
      return this.values.get(_name);
    }

    /**
     * @see #values
     */
    protected void setName(final String _name)
    {
      addValue("Name", _name);
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a definition.
     *
     * @return string representation of this definition of an access type update
     */
    @Override
    public String toString()
    {
      return ToStringBuilder.reflectionToString(this);
    }

    /**
     * add a <code>Trigger</code> to this definition
     *
     * @param _event
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
     * This is the getter method for the instance variable {@link #properties}.
     *
     * @return value of instance variable {@link #properties}
     */
    public Map<String, String> getProperties()
    {
      return this.properties;
    }

    /**
     * This is the getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     */
    public String getType()
    {
      return this.type;
    }

    /**
     * This is the setter method for the instance variable {@link #type}.
     *
     * @param _type   the type to set
     */
    public void setType(final String _type)
    {
      this.type = _type;
    }
  }
}
