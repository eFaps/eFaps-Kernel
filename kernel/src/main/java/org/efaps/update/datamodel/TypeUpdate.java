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

package org.efaps.update.datamodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class TypeUpdate extends AbstractUpdate  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log LOG = LogFactory.getLog(TypeUpdate.class);


  private final static Set <Link> ALLLINKS = new HashSet < Link > ();  {
/*    ALLLINKS.add(LINK2ACCESSTYPE);
    ALLLINKS.add(LINK2DATAMODELTYPE);
    ALLLINKS.add(LINK2PERSON);
    ALLLINKS.add(LINK2ROLE);
    ALLLINKS.add(LINK2GROUP);
*/
  }

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public TypeUpdate() {
    super("Admin_DataModel_Type", ALLLINKS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods

  public static TypeUpdate readXMLFile(final String _fileName) throws IOException  {
//    } catch (IOException e)  {
//      LOG.error("could not open file '" + _fileName + "'", e);
    return readXMLFile(new File(_fileName));
  }

  public static TypeUpdate readXMLFile(final File _file) throws IOException  {
    TypeUpdate ret = null;

    try  {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("datamodel-type", TypeUpdate.class);

      digester.addCallMethod("datamodel-type/uuid", "setUUID", 1);
      digester.addCallParam("datamodel-type/uuid", 0);

      digester.addObjectCreate("datamodel-type/definition", Definition.class);
      digester.addSetNext("datamodel-type/definition", "addDefinition");

      digester.addCallMethod("datamodel-type/definition/version", "setVersion", 4);
      digester.addCallParam("datamodel-type/definition/version/application", 0);
      digester.addCallParam("datamodel-type/definition/version/global", 1);
      digester.addCallParam("datamodel-type/definition/version/local", 2);
      digester.addCallParam("datamodel-type/definition/version/mode", 3);
      
      digester.addCallMethod("datamodel-type/definition/name", "setName", 1);
      digester.addCallParam("datamodel-type/definition/name", 0);

      digester.addCallMethod("datamodel-type/definition/parent", "setParent", 1);
      digester.addCallParam("datamodel-type/definition/parent", 0);

      digester.addCallMethod("datamodel-type/definition/attribute", "addAttribute", 5);
      digester.addCallParam("datamodel-type/definition/attribute/name", 0);
      digester.addCallParam("datamodel-type/definition/attribute/type", 1);
      digester.addCallParam("datamodel-type/definition/attribute/sqltable", 2);
      digester.addCallParam("datamodel-type/definition/attribute/sqlcolumn", 3);
      digester.addCallParam("datamodel-type/definition/attribute/typelink", 4);

      digester.addCallMethod("datamodel-type/definition/property", "addProperty", 2);
      digester.addCallParam("datamodel-type/definition/property", 0, "name");
      digester.addCallParam("datamodel-type/definition/property", 1);

      ret = (TypeUpdate) digester.parse(_file);
    } catch (SAXException e)  {
e.printStackTrace();
      //      LOG.error("could not read file '" + _fileName + "'", e);
    }
    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The class defines an attribute of a type.
   */
  private static class Attribute  {
    /** Name of the attribute. */
    private final String name;
    /** Name of the Attribute Type of the attribute. */
    private final String type;
    /** Name of the SQL Table of the attribute. */
    private final String sqlTable;
    /** SQL Column of the attribute. */
    private final String sqlColumn;
    /** Name of the Linked Type  (used for links to another type). */
    private final String typeLink;

    private Attribute(final String _name,
                      final String _type,
                      final String _sqlTable,
                      final String _sqlColumn,
                      final String _typeLink)  {
      this.name = _name;
      this.type = _type;
      this.sqlTable = _sqlTable;
      this.sqlColumn = _sqlColumn;
      this.typeLink = _typeLink;
    }

    /**
     * For given type defined with the instance parameter, this attribute is
     * searched by name. If the attribute exists, the attribute is updated.
     * Otherwise the attribute is created for this type.
     *
     * @param _instance   type instance to update with this attribute
     * @param _typeName   name of the type to update
     * @see #getAttrTypeId
     * @see #getSqlTableId
     * @see #getTypeLinkId
     * @todo throw Exception is not allowed
     */
    protected void updateInDB(final Instance _instance, 
                              final String _typeName) throws Exception  {
      long attrTypeId = getAttrTypeId(_typeName);
      long sqlTableId = getSqlTableId(_typeName);
      long typeLinkId = getTypeLinkId(_typeName);

      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_Attribute");
      query.addWhereExprEqValue("Name", this.name);
      query.addWhereExprEqValue("ParentType", _instance.getId());
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      Update update;

      if (query.next())  {
        update = new Update((String) query.get("OID"));
      } else  {
        update =  new Insert("Admin_DataModel_Attribute");
        update.add("ParentType",     "" + _instance.getId());
        update.add("Name",           this.name);
      }
      query.close();

      update.add("AttributeType",  "" + attrTypeId);
      update.add("Table",          "" + sqlTableId);
      update.add("SQLColumn",      this.sqlColumn);
      if (typeLinkId == 0)  {
        update.add("TypeLink", null);
      } else  {
        update.add("TypeLink", "" + typeLinkId);
      }
      update.executeWithoutAccessCheck();
    }

    /**
     * Makes a search query to return the id of the attribute type defined in
     * {@link #type}.
     *
     * @return id of the attribute type
     * @see #type
     */
    private long getAttrTypeId(final String _typeName) throws EFapsException  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_AttributeType");
      query.addWhereExprEqValue("Name", this.type);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next())  {
        LOG.error("type[" + _typeName + "]."
                  + "attribute[" + this.name + "]: "
                  + "attribute type '" + this.type + "' not found");
      }
      long attrTypeId = (new Instance((String) query.get("OID"))).getId();
      query.close();
      return attrTypeId;
    }
    
    /**
     * Makes a search query to return the id of the SQL table defined in
     * {@link #sqlTable}.
     *
     * @return id of the SQL table
     * @see #sqlTable
     */
    private long getSqlTableId(final String _typeName) throws EFapsException  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_SQLTable");
      query.addWhereExprEqValue("Name", this.sqlTable);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next())  {
        LOG.error("type[" + _typeName + "]."
                  + "attribute[" + this.name + "]: "
                  + "SQL table '" + this.sqlTable + "' not found");
      }
      long sqlTableId = (new Instance((String) query.get("OID"))).getId();
      query.close();
      return sqlTableId;
    }

    /**
     * Makes a search query to return the id of the SQL table defined in
     * {@link #typeLink}.
     *
     * @return id of the linked type (or 0 if no type link is defined)
     * @see #typeLink
     */
    private long getTypeLinkId(final String _typeName) throws EFapsException  {
      long typeLinkId = 0;
      if ((this.typeLink != null) && (this.typeLink.length() > 0))  {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Type");
        query.addWhereExprEqValue("Name", this.typeLink);
        query.addSelect("ID");
        query.executeWithoutAccessCheck();
        if (!query.next())  {
          LOG.error("type[" + _typeName + "]."
                      + "attribute[" + this.name + "]: "
                      + " Type '" + this.typeLink + "' as link not found");
        } else  {
          typeLinkId = (Long) query.get("ID");
        }
        query.close();
      }
      return typeLinkId;
    }

    /**
     * Returns a string representation with values of all instance variables
     * of an attribute.
     *
     * @return string representation of this definition of an attribute
     */
    public String toString()  {
      return new ToStringBuilder(this)
        .append("name",       this.name)
        .append("type",       this.type)
        .append("sqlTable",   this.sqlTable)
        .append("sqlColumn",  this.sqlColumn)
        .append("typeLink",   this.typeLink)
        .toString();
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends DefinitionAbstract {
    
    ///////////////////////////////////////////////////////////////////////////
    // instance variables
    
    /**
     * Stores the name of the parent type. The parent type could not be
     * evaluated because it could be that the type does not exists (and so
     * the type id is evaluated before the insert / update from method
     * {@link #updateInDB}).
     *
     * @see #setParent
     * @see #updateInDB
     */
    private String parentType = null;

    /**
     * All attributes of the type are stored in this list.
     *
     * @see #updateInDB
     * @see #addAttribute
     */
    private final List < Attribute > attributes 
                                              = new ArrayList < Attribute > ();
    
    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * If a parent type in {@link #parentType} is defined, the type id is
     * evaluated and added to attributes to update (if no parent type is
     * defined, the parent type id is set to <code>null</code>).
     * After the type is updated (or inserted if needed), all attributes must
     * be updated.
     *
     * @todo throw Exception is not allowed
     * @see #parentType
     * @see #attributes
     */
    public Instance updateInDB(final Instance _instance,
                               final Set < Link > _allLinkTypes,
                               final Insert _insert) throws EFapsException, Exception  {
      // set the id of the parent type (if defined)
      if ((this.parentType != null) && (this.parentType.length() > 0))  {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Type");
        query.addWhereExprEqValue("Name", this.parentType);
        query.addSelect("OID");
        query.executeWithoutAccessCheck();
        if (query.next())  {
          Instance instance = new Instance((String) query.get("OID"));
          addValue("ParentType", "" + instance.getId());
        }  else  {
          addValue("ParentType", null);
        }
        query.close();
      } else  {
        addValue("ParentType", null);
      }

      Instance instance = super.updateInDB(_instance, _allLinkTypes, _insert);

      for (Attribute attr : this.attributes)  {
        attr.updateInDB(instance, getValue("Name"));
      }
      return instance;
    }

    /**
     * Setter method for instance variable {@link #parentType}.
     *
     * @param _parentType new value to set
     * @see #parentType
     */
    public void setParent(final String _parentType)  {
      this.parentType = _parentType;
    }
    
    public void addAttribute(final String _name,
                             final String _type,
                             final String _sqlTable,
                             final String _sqlColumn,
                             final String _typeLink)  {
      this.attributes.add(new Attribute(_name, _type, 
                                        _sqlTable, _sqlColumn, 
                                        _typeLink));
    }
  }
}
