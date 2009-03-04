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

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.IClusterable;

/**
 * Class is used to create wizard like behavior for a webpage. Meaning that it
 * is possible to go forward and backward in the pages without loosing
 * parameters, which were already given from the user.<br>
 * It contains a list that stores the UIObjects hirachically so that they
 * can be accessed using methods like previous.
 *
 * @author jmox
 * @version $Id$
 */
public class UIWizardObject implements IClusterable {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Mapping between UIObject and parameters.
   */
  private final Map<AbstractUIObject, Map<String, String[]>> parameters
                      = new HashMap<AbstractUIObject, Map<String, String[]>>();

  /**
   * List of UIObjects in this wizard.
   */
  private final List<AbstractUIObject> uiObjects
                                            = new ArrayList<AbstractUIObject>();

  /**
   * Current UIObject.
   */
  private AbstractUIObject current = null;

  /**
   * Constructor setting the current UIObject.
   *
   * @param _uiObject  current UIOBject
   */
  public UIWizardObject(final AbstractUIObject _uiObject) {
    this.uiObjects.add(_uiObject);
    this.current = _uiObject;
  }

  /**
   * Add a UIOBject to the list of objects.
   * @param _uiObject Object to add
   */
  public void add(final AbstractUIObject _uiObject) {
    this.uiObjects.add(_uiObject);
  }

  /**
   * Insert a UIObject before the current one.
   * @param _uiObject Object to insert
   */
  public void insertBefore(final AbstractUIObject _uiObject) {
    int i = 0;
    for (final AbstractUIObject uiObject : this.uiObjects) {
      if (uiObject == this.current) {
        this.uiObjects.add(i, _uiObject);
        break;
      }
      i++;
    }
  }

  /**
   * Method to get the previous object.
   * @return previous object
   */
  public AbstractUIObject getPrevious() {
    AbstractUIObject ret = null;
    for (final AbstractUIObject uiObject : this.uiObjects) {
      if (uiObject == this.current) {
        break;
      } else {
        ret = uiObject;
      }
    }
    this.current = ret;
    return ret;
  }

  /**
   * Add parameters.
   * @param _uiObject object used as key
   * @param _parameters parameters
   */
  public void addParameters(final AbstractUIObject _uiObject,
      final Map<String, String[]> _parameters) {
    this.parameters.put(_uiObject, _parameters);
  }

  /**
   * Get a parameter map from the mapping.
   * @param _uiObject key fot the parameter map.
   * @return parameter map
   */
  public Map<String, String[]> getParameters(final AbstractUIObject _uiObject) {
    return this.parameters.get(_uiObject);
  }
}
