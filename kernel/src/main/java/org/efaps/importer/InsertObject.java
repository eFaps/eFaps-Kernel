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

package org.efaps.importer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkin;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * This class presents the main Object for the import of Data into eFaps and the
 * connected Database.<br>
 * <br>
 * Every InsertObject can be a child to an other InsertObject, so that a
 * parent-child-hirachie can be constructed. The first InsertObject must be the
 * child to a {@code org.efaps.importer.RootObject}.
 *
 * @author jmox
 * @version $Id$
 */
public class InsertObject extends AbstractObject {
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(InsertObject.class);

  /**
   * contains the Name of the Type of the current InsertObeject
   */
  private String type = null;

  /**
   * Map containing all Attributes of this InsertObject
   */
  private final Map<String, Object> attributes = new HashMap<String, Object>();

  /**
   * contains the Name of the Attribute, wich presents the
   * parent-child-relation, for this InsertObject
   */
  private String parentAttribute = null;

  /**
   * contains all Childs of this Insertobject
   */
  private final Map<String, List<AbstractObject>> childs =
      new HashMap<String, List<AbstractObject>>();

  /**
   * contains the ID for this InsertObject
   */
  private String id = null;

  /**
   * contains all {@link ForeignObjects} of this InsertObject
   */
  private final Set<ForeignObject> links = new HashSet<ForeignObject>();

  /**
   * contains all Attributes wich are defined as unique for this InsertObejct
   */
  private final Set<String> uniqueAttributes = new HashSet<String>();

  /**
   * contains the CheckinObject, if the InsertObject contains one
   */
  private CheckinObject ceckInObject = null;

  public InsertObject() {

  }

  /**
   * Constructor used by {@link InsertObjectFactory}
   *
   * @param _type
   *                Type of the InsertObject
   */
  public InsertObject(final String _type) {
    LOG.info("Creating new " + _type);
    setType(_type);
  }

  /**
   * set the Type of the InsertObject
   *
   * @param _type
   *                Type of the InsertObject
   */
  public void setType(final String _type) {
    this.type = _type;
  }

  /**
   * adds an Attribute to the <code>attributes</code> of this InsertObject and
   * in the case that the Parameter "_unique" equals "true" the Attribute will
   * also be added to the <code>uniqueAttributes.</code>
   *
   * @param _Name
   *                Name of the Attribute
   * @param _Value
   *                Value of the Attribute
   * @param _unique
   *                if _unique equals "true" the Attribute will be added to the
   *                uniqueAttributes of this InsertObject
   */
  public void addAttribute(final String _Name, final String _Value,
      final String _unique) {
    this.attributes.put(_Name, _Value.trim());

    if (_unique != null && _unique.equals("true")) {
      this.uniqueAttributes.add(_Name);
    }
  }

  /**
   * sets the <code>parentAttribute</code> of this InsertObject. If the
   * Parameter "_unique" equals "true" the Attribute will also be added to the
   * <code>uniqueAttributes.</code>
   *
   * @param _ParentAttribute
   *                Name of the Attribute
   * @param _unique
   *                if _unique equals "true" the Attribute will be added to the
   *                uniqueAttributes of this InsertObject
   */
  public void setParentAttribute(final String _ParentAttribute,
      final String _unique) {
    this.parentAttribute = _ParentAttribute;

    if (_unique != null && _unique.equals("true")) {
      this.uniqueAttributes.add(_ParentAttribute);
    }

  }

  /**
   * adds a Child to this InsertObject seperated for the diferend Types. If the
   * Type of the InsertObject is also an OrderObject, the Childs will be sorted.
   *
   * @param _object
   *                Child to be added
   */
  public void addChild(AbstractObject _object) {
    List<AbstractObject> list = this.childs.get(_object.getType());
    if (list == null) {

      list = new ArrayList<AbstractObject>();
      this.childs.put(_object.getType(), list);
      list.add(_object);
    } else {

      list.add(_object);
      if (RootObject.getOrder(_object.getType()) != null) {

        final TreeSet<AbstractObject> treeSet =
            new TreeSet<AbstractObject>(RootObject.getOrder(_object.getType()));

        treeSet.addAll(list);
        list.clear();
        list.addAll(treeSet);
      }

    }
  }

  @Override
  public boolean hasChilds() {

    return this.childs.size() > 0;
  }

  /**
   * adds a ForeignObject to this InsertObject
   *
   * @param _Object
   *                ForeignObject to be added
   */
  public void addLink(ForeignObject _Object) {
    this.links.add(_Object);

  }

  /**
   * adds a <code>uniqueAttributes</code>
   *
   * @param _unique
   *                the Attribute will be added if the Parameter equals "true"
   * @param _Name
   *                Name of the Attribute
   */
  public void addUniqueAttribute(String _unique, String _Name) {
    if (_unique != null && _unique.equals("true")) {
      this.uniqueAttributes.add(_Name);
    }
  }

  @Override
  public void setID(String _id) {
    this.id = _id;
  }

  @Override
  public void dbAddChilds() {

    String ID = null;
    boolean noInsert = false;

    for (final List<AbstractObject> list : this.childs.values()) {
      for (final AbstractObject object : list) {
        noInsert = false;

        if (LOG.isInfoEnabled()) {
          LOG.info("adding Child:" + object.getType());
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug("this: " + this.toString());
          LOG.debug("Cild: " + object.toString());
        }
        try {
          if (object.getUniqueAttributes().size() > 0) {

            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(object.getType());
            query.addSelect("ID");
            for (final String element : object.getUniqueAttributes()) {

              if (object.getAttributes().get(element) != null) {
                query.addWhereExprEqValue(element, object.getAttributes().get(
                    element).toString());

              }

              if (object.getParrentAttribute() != null
                  && object.getParrentAttribute().equals(element)) {
                query.addWhereExprEqValue(element, this.id);

              }
              for (final ForeignObject link : object.getLinks()) {
                if (link.getLinkAttribute().equals(element)) {
                  final String foreignID = link.dbGetID();
                  if (foreignID != null) {
                    query.addWhereExprEqValue(element, foreignID);

                  } else {
                    noInsert = true;
                  }
                }
              }

            }
            query.executeWithoutAccessCheck();

            if (query.next() && !noInsert) {
              ID = object.dbUpdateOrInsert(this, query.get("ID").toString());

            } else {
              if (noInsert && object.hasChilds() == false) {
                LOG.error("skipt: " + object.toString());
              } else {

                ID = object.dbUpdateOrInsert(this, "");
              }
            }
            query.close();
          } else {
            ID = object.dbUpdateOrInsert(this, "");

          }
          object.setID(ID);

          if (object.isCheckinObject()) {
            object.dbCheckObjectIn();
          }

        }

        catch (final EFapsException e) {

          LOG.error("dbAddChilds() " + this.toString(), e);
        } catch (final Exception e) {

          LOG.error("dbAddChilds() " + this.toString(), e);
        }
      }
    }

    for (final List<AbstractObject> list : this.childs.values()) {
      for (final AbstractObject object : list) {
        object.dbAddChilds();
      }
    }
  }

  /**
   * Method to Create the Update or Insert of the Datebase
   *
   * @param _parent
   *                Parent-Object of this Object
   * @param _ID
   *                Id of the Object to be updated, if "" is given a Insert will
   *                be made
   * @return String with the ID of the new or updated Object, null if the
   *         creation of the new object was skipped, because of a foreign Object
   *         was not found
   */
  @Override
  public String dbUpdateOrInsert(final AbstractObject _parent, final String _ID) {
    Boolean noInsert = false;
    String ID = null;
    try {
      Update UpIn;
      if (_ID != "") {

        UpIn = new Update(Type.get(this.type), _ID);

      } else {

        UpIn = new Insert(this.type);

      }

      for (final Entry<String, Object> element : this.getAttributes().entrySet()) {
        if (element.getValue() instanceof Timestamp) {

          UpIn.add(element.getKey().toString(), (Timestamp) element.getValue());

        } else {
          UpIn.add(element.getKey().toString(), element.getValue().toString());
        }
      }
      if (this.getParrentAttribute() != null) {
        UpIn.add(this.getParrentAttribute(), _parent.getID());
      }
      for (final ForeignObject link : this.getLinks()) {

        final String foreignID = link.dbGetID();
        if (foreignID != null) {
          UpIn.add(link.getLinkAttribute(), foreignID);
        } else {
          noInsert = true;
          LOG.error("skipt: " + this.toString());
        }

      }
      if (!noInsert) {
        UpIn.executeWithoutAccessCheck();

        ID = UpIn.getId();
        UpIn.close();
      }
      return ID;
    } catch (final EFapsException e) {
      LOG.error("dbUpdateOrInsert() " + this.toString(), e);
    } catch (final Exception e) {
      LOG.error("dbUpdateOrInsert() " + this.toString(), e);
    }

    return null;
  }

  @Override
  public String getID() {
    return this.id;
  }

  @Override
  public String getType() {
    return this.type;

  }

  @Override
  public Map<String, Object> getAttributes() {
    for (final Entry<String, Object> element : this.attributes.entrySet()) {

      final Attribute attribute =
          Type.get(this.type).getAttribute(element.getKey().toString());
      // TODO das ist nur ein
      // hack damit CreatedType als DateTimeType behandelt werden kann
      if (attribute.getAttributeType().getClassRepr().getName().equals(
          "org.efaps.admin.datamodel.attributetype.DateTimeType")
          || attribute.getAttributeType().getClassRepr().getName().equals(
              "org.efaps.admin.datamodel.attributetype.CreatedType")) {

        final Date date =
            new SimpleDateFormat(RootObject.DATEFORMAT).parse(element
                .getValue().toString(), new ParsePosition(0));

        this.attributes.put(element.getKey(), new Timestamp(date
            .getTime()));
      }
    }
    return this.attributes;
  }

  @Override
  public Object getAttribute(final String _attribute) {

    return (this.attributes.get(_attribute));
  }

  @Override
  public String getParrentAttribute() {

    return this.parentAttribute;
  }

  @Override
  public Set<ForeignObject> getLinks() {

    return this.links;
  }

  @Override
  public Set<String> getUniqueAttributes() {
    return this.uniqueAttributes;
  }

  /**
   * method to Create a new {@link CheckinObject} and store it
   *
   * @param _Name
   *                Name of the CheckinObject
   * @param _URL
   *                URL to the File of the CheckinObject
   */
  public void setCheckinObject(String _Name, String _URL) {
    this.ceckInObject = new CheckinObject(_Name, _URL);

  }

  @Override
  public boolean isCheckinObject() {
    if (this.ceckInObject != null) {
      return true;
    }
    return false;
  }

  @Override
  public void dbCheckObjectIn() {

    final Checkin checkin = new Checkin(new Instance(this.type, this.id));

    try {
      checkin.executeWithoutAccessCheck(this.ceckInObject.getName(),
          this.ceckInObject.getInputStream(), -1);
    } catch (final EFapsException e) {

      LOG.error("checkObjectin() " + this.toString(), e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    final StringBuilder tmp = new StringBuilder();
    tmp.append("Type: ");
    tmp.append(this.type);
    tmp.append(" - ParentAttribute: ");
    tmp.append(this.parentAttribute);
    tmp.append(" - Attributes: ");
    tmp.append(this.attributes.toString());
    tmp.append(" - Links: ");
    tmp.append(this.links.toString());
    return tmp.toString();
  }

  /**
   * Class to store the Information, needed to Check in a InsertObject
   *
   * @author jmox
   * @version $Id$
   *
   */
  public class CheckinObject {

    /**
     * contains the Filename of the CheckinObject
     */
    private String name = null;

    /**
     * contains the URL to the File
     */
    private String url = null;

    /**
     * constructor setting the Filename and the URL of the CheckinObject
     *
     * @param _name
     *                Filename of the CheckinObject
     * @param _url
     *                URL to the File
     */
    public CheckinObject(String _name, String _url) {
      this.name = _name.trim();
      this.url = _url.trim();
    }

    /**
     * get the Name of the CheckinObject
     *
     * @return Filename of the CheckinObject
     */
    public String getName() {
      return this.name;
    }

    /**
     * get the URL of the CheckinObject
     *
     * @return URL to the File
     */
    public String getURL() {
      return this.url;
    }

    /**
     * get an Inputstream of the File to check in
     *
     * @return Inputstream of the File
     */
    public InputStream getInputStream() {
      try {
        final InputStream inputstream = new FileInputStream(this.url);
        return inputstream;
      } catch (final FileNotFoundException e) {

        LOG.error("getInputStream()", e);
      }

      return null;

    }
  }

}
