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

package org.efaps.ui.wicket.models;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.IClusterable;
import org.apache.wicket.RequestCycle;

import org.efaps.db.Instance;
import org.efaps.ui.wicket.EFapsWebRequestCycle;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractInstanceObject implements IClusterable {

  public static String INSTANCE_CACHEKEY  = "InstancesInRequest";

    /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  private String instanceKey;

  public AbstractInstanceObject() {

  }

  public AbstractInstanceObject(final String _instanceKey) {
    this.instanceKey = _instanceKey;
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
   * @see org.efaps.ui.wicket.models.AbstractInstanceObject#getInstance()
   * @return Instance of the object
   * @throws EFapsException
   */
  @SuppressWarnings("unchecked")
  public Instance getInstance() throws EFapsException {
    Instance ret = null;
    Map<String, Instance> map;

    final EFapsWebRequestCycle cycle
                                    = (EFapsWebRequestCycle) RequestCycle.get();

    map = (Map<String, Instance>) cycle.getFromCache(INSTANCE_CACHEKEY);
    if (map == null) {
      map =  new HashMap<String, Instance>();
      cycle.putIntoCache(INSTANCE_CACHEKEY, map);
    }
    if (map.containsKey(this.instanceKey)) {
      ret = map.get(this.instanceKey);
    } else {
      if (hasInstanceManager()) {
        ret = getInstanceFromManager();
      } else if ((this.instanceKey != null)
            && (this.instanceKey.length() > 0)) {
        ret = Instance.get(getInstanceKey());
      }
      map.put(this.instanceKey, ret);
    }
    return ret;
  }

  /**
   * Setter method for instance variable {@link #instanceKey}.
   *
   * @param _instanceKey value for instance variable {@link #instanceKey}
   */
  public void setInstanceKey(final String _instanceKey) {
    this.instanceKey = _instanceKey;
  }

  public abstract Instance getInstanceFromManager() throws EFapsException;

  public abstract boolean hasInstanceManager();
}
