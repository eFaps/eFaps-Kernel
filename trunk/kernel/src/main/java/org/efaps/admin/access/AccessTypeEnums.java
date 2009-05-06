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

package org.efaps.admin.access;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * All access types which are used directly in eFaps are referenced with the
 * universal unique identifiers of the access types. To get e.g. the access 
 * type for read access call 
 * <code>AccessTypeEnums.READ.getAccessType()</code>.
 *
 * @author tmo
 * @version $Id$
 */
public enum AccessTypeEnums   {

  /////////////////////////////////////////////////////////////////////////////
  // enum definitions

  /** Access type for checkin access. */
  CHECKIN("cbec0a52-3249-4959-9a4d-c9aadb894104"),
  /** Access type for checkout access. */
  CHECKOUT("cec9467b-94e8-463c-9036-75fba39b9baa"),
  /** Access type for create access. */
  CREATE("1dd13a42-e04f-4bce-85cf-3931ae94267f"),
  /** Access type for delete access. */
  DELETE("47d74dd6-4929-4927-a490-82998363c409"),
  /** Access type for modify access. */
  MODIFY("778592ad-a99f-4912-b94d-b05c305381d8"),
  /** Access type for read access. */
  READ("b55491d8-49be-44af-860c-6dd4cc28a8ca"),
  /** Access type for show access. */
  SHOW("df0fed9f-4749-4444-bd78-323b2d6bc563");

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The universal unique identifier of one access type enum is stored in this
   * instance variable.
   */
  private final UUID uuid;

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * The constructor creates a new enum instance of access type for given 
   * universal unique identifiers of access types.
   *
   * @param _uuid universal unique identifier of the access type
   * @see #uuid
   */
  private AccessTypeEnums(final String _uuid)  {
    this.uuid = UUID.fromString(_uuid);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The internal cached access type for the universal unique identifier in
   * {@link #uuid} is returned with the help of static method 
   * {@link AccessType.getAccessType(UUID)}.
   *
   * @return related access type to the universal unique identifier
   * @see #uuid
   * @see AccessType.getAccessType(UUID)
   */
  public AccessType getAccessType()  {
    return AccessType.getAccessType(this.uuid);
  }
  
  /**
   * The related access type from {@link #getAccessType} is packed in a list.
   *
   * @return related access type as list
   * @see #getAccessType
   */
   public List < AccessType > getAccessTypeAsList()  {
     List < AccessType > ret = new ArrayList < AccessType > (1);
     ret.add(getAccessType());
     return ret;
   }
}
