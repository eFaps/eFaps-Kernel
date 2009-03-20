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

package org.efaps.ui.wicket;

import java.util.Random;
import java.util.UUID;

import org.apache.wicket.model.IModel;

/**
 * Class is used to store the information about the opener of a window, e.g. a
 * pop up window, so that the information needed about the parent can be
 * accessed from the pop up window. Each instance of this class is stored with a
 * unique key in the EFapSession. This enables that no information like OID or
 * an UUID must be passed as parameters from the Browser and therefore could
 * be altered.
 *
 * @author jmox
 * @version $Id$
 */
public class Opener {

  /**
   * Key used to pass the id as parameter inside the session of a page.
   */
  public static final String OPENER_PARAKEY = "openerId";
  /**
   * Id of this opener. It is generated as a random number in  the constructor.
   */
  private final String id;

  /**
   * Stores the model of the opener.
   */
  private final IModel<?> model;

  /**
   * The name of the page map of this opener.
   */
  private final String pageMapName;

  /**
   * The key for the related menuTree of the opener.
   */
  private String menuTreeKey;

  /**
   * This UUID can be used to store a command in the opener. This is needed
   * if e.g. the model is not existing because it was opened by the main window.
   */
  private UUID commandUUID;

  /**
   * This String can be used to store a oid in the opener. This is needed
   * if e.g. the model is not existing because it was opened by the main window.
   */
  private String instanceKey;

  /**
   * This variable stores if this opener can be removed from the session on the
   * next possibility.
   */
  private boolean marked4Remove = false;

  /**
   * Constructor.
   *
   * @param _model        model of the openr
   * @param _pageMapName  pagemap of the opener
   */
  public Opener(final IModel<?> _model, final String _pageMapName) {
    this.id = ((Long) (new Random().nextLong())).toString();
    this.model = _model;
    this.pageMapName = _pageMapName;
  }

  /**
   * Getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public String getId() {
    return this.id;
  }

  /**
   * Getter method for instance variable {@link #model}.
   *
   * @return value of instance variable {@link #model}
   */
  public IModel<?> getModel() {
    return this.model;
  }

  /**
   * Getter method for instance variable {@link #pageMapName}.
   *
   * @return value of instance variable {@link #pageMapName}
   */
  public String getPageMapName() {
    return this.pageMapName;
  }

  /**
   * Getter method for instance variable {@link #menuTreeKey}.
   *
   * @return value of instance variable {@link #menuTreeKey}
   */
  public String getMenuTreeKey() {
    return this.menuTreeKey;
  }

  /**
   * Setter method for instance variable {@link #menuTreeKey}.
   *
   * @param _menuTreeKey value for instance variable {@link #menuTreeKey}
   */
  public void setMenuTreeKey(final String _menuTreeKey) {
    this.menuTreeKey = _menuTreeKey;
  }

  /**
   * Getter method for instance variable {@link #commandUUID}.
   *
   * @return value of instance variable {@link #commandUUID}
   */
  public UUID getCommandUUID() {
    return this.commandUUID;
  }

  /**
   * Setter method for instance variable {@link #commandUUID}.
   *
   * @param _commandUUID value for instance variable {@link #commandUUID}
   */
  public void setCommandUUID(final UUID _commandUUID) {
    this.commandUUID = _commandUUID;
  }

  /**
   * Getter method for instance variable {@link #instanceKey}.
   *
   * @return value of instance variable {@link #instanceKey}
   */
  public String getInstanceKey() {
    return this.instanceKey;
  }

  /**
   * Setter method for instance variable {@link #instanceKey}.
   *
   * @param _instanceId value for instance variable {@link #instanceKey}
   */
  public void setInstanceKey(final String _instanceId) {
    this.instanceKey = _instanceId;
  }

  /**
   * Getter method for instance variable {@link #marked4Remove}.
   *
   * @return value of instance variable {@link #marked4Remove}
   */
  public boolean isMarked4Remove() {
    return this.marked4Remove;
  }

  /**
   * Setter method for instance variable {@link #marked4Remove}.
   *
   * @param _marked4Remove value for instance variable {@link #marked4Remove}
   */
  public void setMarked4Remove(final boolean _marked4Remove) {
    this.marked4Remove = _marked4Remove;
  }
}
