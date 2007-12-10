/*
 * Copyright 2003-2007 The eFaps Team
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.user.UserAttributesSet;
import org.efaps.admin.user.UserAttributesSet.UserAttributesDefinition;
import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.ui.wicket.behaviors.update.UpdateInterface;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class EFapsSession extends WebSession {

  private static final long serialVersionUID = 1884548064760514909L;

  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(EFapsSession.class);

  /**
   * This instance Map is a Cache for Components, wich must be able to be
   * accessed from various PageMaps.
   *
   * @see #getFromCache(String)
   * @see #putIntoCache(String, Component)
   * @see #removeFromCache(String)
   */
  private final Map<String, Component> componentcache =
      new HashMap<String, Component>();

  /**
   * This instance variable holds an IModel wich must be past from one Page in
   * one PageMap to another Page in an other PageMap
   *
   * @see #getOpenerModel()
   * @see #setOpenerModel(IModel)
   */
  private IModel openerModel;

  /**
   * This instance variable holds the Name of the logged in user. It is also
   * used to check if a user is logged in, by returning that a user is logged
   * in, if this variable is not null.
   *
   * @see #isLogedIn()
   * @see #checkin()
   * @see #checkout()
   */
  private String userName;

  /**
   * This instance map stores the Attributes wich are valid for the whole
   * session. It is passed on to the Context while opening it.
   *
   * @see #openContext()
   */
  private final Map<String, Object> sessionAttributes =
      new HashMap<String, Object>();

  /**
   * This instance map stores the Behaviors that will be called through the
   * UpdateInterface
   *
   * @see #addUpdateBehaviors(String, UpdateInterface)
   * @see #getUpdateBehavior(String)
   * @see #getUpdateBehaviors()
   */
  private final Map<String, List<UpdateInterface>> updateBehaviors =
      new HashMap<String, List<UpdateInterface>>();

  /**
   * Standart Constructor from Wicket
   *
   * @param _request
   */
  public EFapsSession(final Request _request) {
    super(_request);
  }

  /**
   * method to add a Behavior to the {@link #updateBehaviors}. The behavior
   * will only be added if no updatebehvior with the same Id is existing in the
   * List related to the given oid
   *
   * @param _oid
   *                Oid (used as key in the map)
   * @param _behavior
   *                (behavoir to be added)
   */
  public void addUpdateBehaviors(final String _oid,
                                 final UpdateInterface _behavior) {
    List<UpdateInterface> behaviors;

    if (this.updateBehaviors.containsKey(_oid)) {
      behaviors = this.updateBehaviors.get(_oid);
      for (int i = 0; i < behaviors.size(); i++) {
        if (behaviors.get(i).getId().equals(_behavior.getId())) {
          behaviors.remove(i);
          break;
        }
      }
    } else {
      behaviors = new ArrayList<UpdateInterface>();

    }
    behaviors.add(_behavior);
    this.updateBehaviors.put(_oid, behaviors);
  }

  /**
   * method that returns the behaviors as aList that rely to a specified oid
   *
   * @param _oid
   *                OID to get the List for
   * @return List with Behaviors
   */
  public List<UpdateInterface> getUpdateBehavior(final String _oid) {
    return this.updateBehaviors.get(_oid);
  }

  /**
   * This is the getter method for the instance variable
   * {@link #updateBehaviors}.
   *
   * @return value of instance variable {@link #updateBehaviors}
   */
  public Map<String, List<UpdateInterface>> getUpdateBehaviors() {
    return this.updateBehaviors;
  }

  /**
   * on attach a Context will be opened if a User is logged in
   *
   * @see #openContext()
   * @see org.apache.wicket.Session#attach()
   */
  @Override
  protected void attach() {
    super.attach();
    if (this.isLogedIn() && (RequestCycle.get() != null)) {
      openContext();
    }
  }

  /**
   * on detach the Context will be closed if open
   *
   * @see #closeContext()
   * @see org.apache.wicket.Session#detach()
   */
  @Override
  protected void detach() {
    super.detach();
    try {
      if (this.isLogedIn() && !Context.isTMNoTransaction()) {
        closeContext();
      }
    } catch (EFapsException e) {
      LOG.error("could not read the Status of the TransactionManager", e);
    }

  }

  /**
   * This Method stores a Component in the Cache
   *
   * @param _key
   *                Key the Component should be stored in
   * @param _component
   *                Component to be stored
   * @see #componentcache
   */
  public void putIntoCache(final String _key, final Component _component) {
    this.componentcache.remove(_key);
    this.componentcache.put(_key, _component);
  }

  /**
   * Retriev a Component from the ComponentCache
   *
   * @param _key
   *                Key of the Component to be retrieved
   * @return Component if found, else null
   * @see #componentcache
   */
  public Component getFromCache(final String _key) {
    return this.componentcache.get(_key);
  }

  /**
   * Remove a Component from the ComponentCache
   *
   * @param _key
   *                Key to the Component to be removed
   * @see #componentcache
   */
  public void removeFromCache(final String _key) {
    this.componentcache.remove(_key);
  }

  /**
   * This is the getter method for the instance variable {@link #openerModel}.
   *
   * @return value of instance variable {@link #openerModel}
   */

  public IModel getOpenerModel() {
    return this.openerModel;
  }

  /**
   * This is the setter method for the instance variable {@link #openerModel}.
   *
   * @param openerModel
   *                the openerModel to set
   */
  public void setOpenerModel(final IModel openerModel) {
    this.openerModel = openerModel;
  }

  /**
   * Method to check ia a user is checked in
   *
   * @return true if a user is checked in, else false
   * @see #userName
   */
  public boolean isLogedIn() {
    boolean ret = false;
    if (this.userName != null) {
      ret = true;
    }
    return ret;

  }

  /**
   * method to log a user with the Parameters from the Request in
   *
   * @see #checkLogin(String, String)
   */
  public final void login() {
    final Map<?, ?> parameter =
        RequestCycle.get().getRequest().getParameterMap();
    final String[] name = (String[]) parameter.get("name");
    final String[] pwd = (String[]) parameter.get("password");
    if (checkLogin(name[0], pwd[0])) {
      this.userName = name[0];
    } else {
      this.userName = null;
      this.sessionAttributes.clear();
    }

  }

  /**
   * logs a user out and stores the UserAttribues in the eFapsDataBase
   */
  // TODO maybe we should store the UserAttributes elsewhere, because what
  // happens if the User does not log out?
  public final void logout() {
    this.userName = null;
    if (this.sessionAttributes.containsKey(UserAttributesSet.CONTEXTMAPKEY)) {
      ((UserAttributesSet) this.sessionAttributes
          .get(UserAttributesSet.CONTEXTMAPKEY)).storeInDb();
      this.sessionAttributes.clear();
    }
    closeContext();
  }

  /**
   * method to check the LoginInformation (Name and Password) against the
   * eFapsDatabase. To check the Information a Context is opened an afterwards
   * closed. It also puts a new Instance of UserAttributes into the instance map
   * {@link #sessionAttributes}
   *
   * @param _name
   *                Name of the User to be checked in
   * @param _passwd
   *                Password of the User to be checked in
   * @return true if LoginInformation was valid, else false
   */
  private boolean checkLogin(final String _name, final String _passwd) {

    boolean loginOk = false;
    try {
      Context context = null;

      if (Context.isTMActive()) {
        context = Context.getThreadContext();
      } else {
        context = Context.begin();
      }
      boolean ok = false;

      try {
        final LoginHandler loginHandler =
            new LoginHandler(super.getApplication().getApplicationKey());
        if (loginHandler.checkLogin(_name, _passwd) != null) {
          loginOk = true;

          this.sessionAttributes.put(UserAttributesSet.CONTEXTMAPKEY,
              new UserAttributesSet(_name));
        }
        ok = true;
      }
      finally {

        if (ok && context.allConnectionClosed() && Context.isTMActive()) {
          Context.commit();
        } else {
          if (Context.isTMMarkedRollback()) {
            LOG.error("transaction is marked to roll back");
          } else if (!context.allConnectionClosed()) {
            LOG.error("not all connection to database are closed");
          } else {
            LOG.error("transaction manager in undefined status");
          }
          Context.rollback();
        }
      }
    } catch (EFapsException e) {
      LOG.error("could not check name and password", e);
    }

    return loginOk;
  }

  /**
   * method that opens a new Context in eFaps, setting the User, the Locale, the
   * Attributes of this Session {@link #sessionAttributes} and the
   * RequestParameters for the Context.
   *
   * @see #attach()
   */
  @SuppressWarnings("unchecked")
  private void openContext() {
    try {
      if (!Context.isTMActive()) {
        final Map<String, String[]> parameter =
            RequestCycle.get().getRequest().getParameterMap();

        Context.begin(this.userName, super.getLocale(), this.sessionAttributes,
            parameter, null);

        if (Context.getThreadContext().containsUserAtribute(
            UserAttributesDefinition.LOCALE.name)) {
          final Locale locale =
              new Locale(Context.getThreadContext().getUserAttribute(
                  UserAttributesDefinition.LOCALE.name));
          Context.getThreadContext().setLocale(locale);
        }

      }
    } catch (EFapsException e) {
      LOG.error("could not initialise the context", e);
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * method that closes the opened Context {@link #openContext()}, by commiting
   * or rollback it
   *
   * @see #detach()
   */
  private void closeContext() {
    try {
      if (Context.isTMActive()) {
        Context.commit();
      } else {
        Context.rollback();
      }
    } catch (SecurityException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (IllegalStateException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

}
