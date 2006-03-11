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

package org.efaps.beans;

import java.util.List;

public interface CollectionBeanInterface extends AbstractBeanInterface  {

  /**
   * The query of this user interface is executed and the result is stored
   * (cached) internally.
   */
  public void execute() throws Exception;

  /**
   * The method must return the list of all rows of this web table user
   * interface.
   *
   * @return list of all rows of the user interface table
   */
  public List getValues();

  /**
   * The method returns the node id for this table or form bean user interface
   * used e.g. in references.
   *
   * @return node id as string
   */
  public String getNodeId();

  /**
   * The instance method returns, if the user interface is in connect mode
   * (e.g. search and connect web wizards).
   *
   * @return <i>true</i> if the user interface is in connect mode, otherwise
   *         <i>false</i>
   */
  public boolean isConnectMode();

  /**
   * The instance method returns, if the user interface is in create mode
   * (e.g. create web form).
   *
   * @return <i>true</i> if the user interface is in create mode, otherwise
   *         <i>false</i>
   */
  public boolean isCreateMode();

  /**
   * The instance method returns, if the user interface is in edit mode
   * (e.g. edit web form or edit web tables).
   *
   * @return <i>true</i> if the user interface is in edit mode, otherwise
   *         <i>false</i>
   */
  public boolean isEditMode();

  /**
   * The instance method returns, if the user interface is in search mode
   * (e.g. search and connect web wizards, or only web searches).
   *
   * @return <i>true</i> if the user interface is in edit mode, otherwise
   *         <i>false</i>
   */
  public boolean isSearchMode();

  /**
   * The instance method returns, if the user interface is in view mode
   * (e.g. web forms or web tables).
   *
   * @return <i>true</i> if the user interface is in view mode, otherwise
   *         <i>false</i>
   */
  public boolean isViewMode();

}
