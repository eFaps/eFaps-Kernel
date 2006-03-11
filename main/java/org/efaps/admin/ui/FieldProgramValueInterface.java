/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.admin.ui;

import java.util.List;

import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.db.AbstractQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;

/**
 * The interface is used for fields to create values which can not directly
 * evaluate from the database.
 */
public interface FieldProgramValueInterface  {

  /////////////////////////////////////////////////////////////////////////////
  // methods for view mode

  /**
   * The instance method must add all attributes which must be selected
   * in a query to evaluate the value.
   *
   * @param _context  context for this request
   * @param _query    query to add the attributes
   */
  public void addSelectAttributes(Context _context, AbstractQuery _query) throws Exception;

  /**
   * The instance method must return the attribute value depending on the
   * query.
   *
   * @param _context  context for this request
   * @param _query    query with the attribute values
   */
  public AttributeTypeInterface evalAttributeValue(Context _context, AbstractQuery _query) throws Exception;

  /////////////////////////////////////////////////////////////////////////////
  // methods for search mode

  /**
   * The instance method must add the where clause for the given value.
   *
   * @param _context  context for this request
   * @param _query    query to add the where clause
   * @param _value    value from the input form of the search
   */
  public void addSearchWhere(Context _context, SearchQuery _query, String _value) throws Exception;

  /**
   * The instance method must return the attribute value depending on the
   * parent instance object (for connect search mode).
   *
   * @param _context  context for this request
   * @param _instance business object for which the search is done (if
   *                  connect search mode is selected)
   */
  public AttributeTypeInterface evalSearchAttributeValue(Context _context, Instance _instance) throws Exception;
}