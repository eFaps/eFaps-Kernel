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

package org.efaps.ui.wicket.models.cell;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.IClusterable;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id$
 */
public class XYValue implements IClusterable{

  private static final long serialVersionUID = 1L;
  private final int y;

  public int getY() {
    return this.y;
  }
  public int getX() {
    return this.xrow.size();
  }


  public List<Integer> getXrow() {
    return this.xrow;
  }

  private final List<Integer> xrow = new ArrayList<Integer>();
  public XYValue (final int _y){
    this.y = _y;
  }

  /**
   * @param value
   */
  public void addX(final int _x) {
    this.xrow.add(_x);
  }


}
