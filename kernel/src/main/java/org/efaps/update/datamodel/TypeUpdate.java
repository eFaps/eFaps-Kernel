/*
 * Copyright 2006 The eFaps Team
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
      digester.addCallParam("datamodel-type/definition/property/name", 0);
      digester.addCallParam("datamodel-type/definition/property/value", 1);

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
    /** Attribute Type of the attribute. */
    private final long type;
    /** SQL Table of the attribute. */
    private final long sqlTable;
    /** SQL Column of the attribute. */
    private final String sqlColumn;
    /** Type Link Id (used for links to another type). */
    private final long typeLinkId;

    private Attribute(final String _name,
                      final long _type,
                      final long _sqlTable,
                      final String _sqlColumn,
                      final long _typeLinkId)  {
      this.name = _name;
      this.type = _type;
      this.sqlTable = _sqlTable;
      this.sqlColumn = _sqlColumn;
      this.typeLinkId = _typeLinkId;
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
        .append("typeLinkId", this.typeLinkId)
        .toString();
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends DefinitionAbstract {
    
    ///////////////////////////////////////////////////////////////////////////
    // instance variables
    
    private final List < Attribute > attributes 
                                              = new ArrayList < Attribute > ();
    
    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     *
     */
    public Instance updateInDB(final Instance _instance,
                           final Set < Link > _allLinkTypes,
                           final Insert _insert) throws EFapsException, Exception  {

      Instance instance = super.updateInDB(_instance, _allLinkTypes, _insert);

      for (Attribute attr : this.attributes)  {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Attribute");
        query.addWhereExprEqValue("Name", attr.name);
        query.addWhereExprEqValue("ParentType", instance.getId());
        query.addSelect("OID");
        query.executeWithoutAccessCheck();
        Update update;

        if (query.next())  {
          update = new Update((String) query.get("OID"));
        } else  {
          update =  new Insert("Admin_DataModel_Attribute");
          update.add("ParentType",     "" + instance.getId());
          update.add("Name",           attr.name);
        }
        query.close();

        update.add("AttributeType",  "" + attr.type);
        update.add("Table",          "" + attr.sqlTable);
        update.add("SQLColumn",      attr.sqlColumn);
        if (attr.typeLinkId == 0)  {
          update.add("TypeLink", null);
        } else  {
          update.add("TypeLink", "" + attr.typeLinkId);
        }
        update.executeWithoutAccessCheck();
      }
      return instance;
    }

    /**
     * @todo throw Exception is not allowed
     */
    public void setParent(final String _parent) throws EFapsException {
      if ((_parent != null) && (_parent.length() > 0))  {
        // search for the instance
        SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Type");
        query.addWhereExprEqValue("Name", _parent);
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
    }
    
    public void addAttribute(final String _name,
                             final String _type,
                             final String _sqlTable,
                             final String _sqlColumn,
                             final String _typeLink) throws EFapsException  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_AttributeType");
      query.addWhereExprEqValue("Name", _type);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next())  {
        LOG.error("type[" + getValue("Name") + "].attribute[" + _name + "]: "
                  + "attribute type '" + _type + "' not found");
      }
      long attrTypeId = (new Instance((String) query.get("OID"))).getId();
      query.close();
      
      query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_SQLTable");
      query.addWhereExprEqValue("Name", _sqlTable);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next())  {
        LOG.error("type[" + getValue("Name") + "].attribute[" + _name + "]: "
                  + "SQL table '" + _sqlTable + "' not found");
      }
      long sqlTableId = (new Instance((String) query.get("OID"))).getId();
      query.close();

      long typeLinkId = 0;
      if ((_typeLink != null) && (_typeLink.length() > 0))  {
        query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Type");
        query.addWhereExprEqValue("Name", _typeLink);
        query.addSelect("ID");
        query.executeWithoutAccessCheck();
        if (!query.next())  {
          LOG.error("type[" + getValue("Name") + "].attribute[" + _name + "]: "
                      + " Type '" + _typeLink + "' as link not found");
        } else  {
          typeLinkId = (Long) query.get("ID");
        }
        query.close();
      }

      this.attributes.add(new Attribute(_name, attrTypeId, 
                                        sqlTableId, _sqlColumn, 
                                        typeLinkId));
    }
  }
}
