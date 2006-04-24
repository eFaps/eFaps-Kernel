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

package org.efaps.beans;

import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * <p>
 *   The class could be used to load the resource bundles automatically. The
 *   class owns methods to translate texts.
 * </p>
 * Example of an Usage:
 * <ul>
 * <li>add the resource bundle bean to the faces configuration:<br/>
 *   <b><code>
 *     &lt;!-- resource bundle bean used for translation --&gt;<br/>
 *     &lt;managed-bean&gt;<br/>
 *     &nbsp;&nbsp;&lt;managed-bean-name&gt;i18n&lt;/managed-bean-name&gt;<br/>
 *     &nbsp;&nbsp;&lt;managed-bean-class&gt;org.efaps.beans.ResourceBundleBean&lt;/managed-bean-class&gt;<br/>
 *     &nbsp;&nbsp;&lt;managed-bean-scope&gt;application&lt;/managed-bean-scope&gt;<br/>
 *     &nbsp;&nbsp;&lt;!-- set bundle (base) name --&gt;<br/>
 *     &nbsp;&nbsp;&lt;managed-property&gt;<br/>
 *     &nbsp;&nbsp;&nbsp;&nbsp;&lt;property-name&gt;bundleName&lt;/property-name&gt;<br/>
 *     &nbsp;&nbsp;&nbsp;&nbsp;&lt;value&gt;StringResource&lt;/value&gt;<br/>
 *     &nbsp;&nbsp;&lt;/managed-property&gt;<br/>
 *     &lt;/managed-bean&gt;<br/>
 *   </code></b>
 * </li>
 * <li>
 *   add to your bean the instance variable <b><code>i18nBean</code></b> from
 *   class <b><code>ResourceBundleBean</code></b>
 * </li>
 * <li>add method <b><code>setI18nBean(ResourceBundleBean _i18nBean)</code></b> to your bean</li>
 * <li>define the managed property for your bean:<br/>
 *   <b><code>
 *     &lt;managed-property&gt;<br/>
 *     &nbsp;&nbsp;&lt;property-name&gt;i18nBean&lt;/property-name&gt;<br/>
 *     &nbsp;&nbsp;&lt;value&gt;#{i18n}&lt;/value&gt;<br/>
 *     &lt;/managed-property&gt;<br/>
 *   </code></b>
 * </li>
 * <li>use with<br/>
 *    <b><code>String translated = this.i18nBean.translate("Admin_UI_Command.Label");</code></b><br/>
 *    the resource bundle bean in your bean
 * </ul>
 *
 * @author tmo
 * @version $Rev$
 */
public class ResourceBundleBean  {

  /**
   * The map stores a resource bundle depending on the language / country as
   * key.
   */
  private final Map < String, ResourceBundle > bundles = new HashMap < String, ResourceBundle > ();

  /**
   * The string stores the bundle name in which the strings are stored.
   */
  private String bundleName = null;

  /**
   *
   */
  public void setBundleName(final String _bundleName)  {
    this.bundleName = _bundleName;
  }


  public String translate(final String _id)  {
    Context context = null;
    try  {
      context = Context.getThreadContext();
    } catch (EFapsException e)  {
    }
    return translate(context, _id);
  }

  public String translate(final Context _context, final String _id)  {
    return translate(_context != null ? _context.getLocale() : null, _id);
  }

  public String translate(final Locale _locale, final String _id)  {
    String key = _locale != null ? _locale.getLanguage() + _locale.getCountry() : "";

    ResourceBundle bundle = bundles.get(key);
    if (bundle == null)  {
      bundle = ResourceBundle.getBundle(this.bundleName, _locale);
      bundles.put(key, bundle);
    }
    return bundle.getString(_id);
  }


  public ResourceBundle getResourceBundle() throws EFapsException  {
    return getResourceBundle(Context.getThreadContext());
  }

  public ResourceBundle getResourceBundle(final Context _context)  {
    return getResourceBundle(_context != null ? _context.getLocale() : null);
  }

  public ResourceBundle getResourceBundle(final Locale _locale)  {
    String key = _locale != null ? _locale.getLanguage() + _locale.getCountry() : "";

    ResourceBundle bundle = bundles.get(key);
    if (bundle == null)  {
      bundle = ResourceBundle.getBundle(this.bundleName, _locale);
      bundles.put(key, bundle);
    }
    return bundle;
  }

}

