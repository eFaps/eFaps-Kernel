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

package org.efaps.admin.datamodel;

import java.util.Locale;

import org.efaps.admin.ui.Field;

/**
 * The interface describes the needed methods for using a file store.
 */
public interface AttributeDefinitionUIInterface  {

  /**
   * @param _locale   locale object
   */
  public String getViewHtml(Locale _locale, Field _field);

  /**
   * @param _locale   locale object
   */
  public String getEditHtml(Locale _locale, Field _field);

  /**
   * @param _locale   locale object
   */
  public String getCreateHtml(Locale _locale, Field _field);

  /**
   * @param _locale   locale object
   */
  public String getSearchHtml(Locale _locale, Field _field);
}
