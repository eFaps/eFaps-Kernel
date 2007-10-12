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

package org.efaps.webapp;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.slide.transaction.SlideTransactionManager;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;
import org.efaps.webapp.pages.ErrorPage;

/**
 * @author jmo
 * @version $Id$
 */
public class EFapsSession extends WebSession {

  private static final long serialVersionUID = 1884548064760514909L;

  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(EFapsSession.class);

  /**
   * The static variable holds the transaction manager which is used within the
   * eFaps web application.
   */
  final public static TransactionManager TRANSACTIONSMANAGER =
      new SlideTransactionManager();

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
  private String username;

  /**
   * Standart Constructor from Wicket
   *
   * @param _request
   */
  public EFapsSession(final Request _request) {
    super(_request);
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
    if (this.isLogedIn() && RequestCycle.get() != null) {
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
      if (this.isLogedIn()
          && TRANSACTIONSMANAGER.getStatus() != Status.STATUS_NO_TRANSACTION) {
        closeContext();
      }
    } catch (SystemException e) {
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
  public void setOpenerModel(IModel openerModel) {
    this.openerModel = openerModel;
  }

  /**
   * Method to check ia a user is checked in
   *
   * @return true if a user is checked in, else false
   * @see #username
   */
  public boolean isLogedIn() {
    if (this.username != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * method to check a user with the Parameters from the Request in
   *
   * @see #checkLogin(String, String)
   */
  public final void checkin() {
    Map<?, ?> parameter = RequestCycle.get().getRequest().getParameterMap();
    String[] name = (String[]) parameter.get("name");
    String[] pwd = (String[]) parameter.get("password");
    if (checkLogin(name[0], pwd[0])) {
      this.username = name[0];
    } else {
      this.username = null;
    }

  }

  /**
   * checks a user out
   */
  public final void checkout() {
    this.username = null;
    closeContext();
  }

  /**
   * method to check the LoginInformation (Name and Password) against the
   * eFapsDatabase. To check the Information a Context is opened an afterwards
   * closed.
   *
   * @param _name
   *                Name of the User to be checked in
   * @param _passwd
   *                Password of the User to be checked in
   * @return true if LoginInformation was valid, else false
   */
  private boolean checkLogin(final String _name, final String _passwd) {

    boolean loginOk = false;

    Context context = null;
    try {
      TRANSACTIONSMANAGER.begin();
      context =
          Context.newThreadContext(TRANSACTIONSMANAGER.getTransaction(), null,
              super.getLocale());
      Context.setTransactionManager(TRANSACTIONSMANAGER);
      boolean ok = false;

      try {
        LoginHandler loginHandler =
            new LoginHandler(super.getApplication().getApplicationKey());
        if (loginHandler.checkLogin(_name, _passwd) != null) {
          loginOk = true;
        }
        ok = true;
      }
      finally {

        if (ok
            && context.allConnectionClosed()
            && (TRANSACTIONSMANAGER.getStatus() == Status.STATUS_ACTIVE)) {

          TRANSACTIONSMANAGER.commit();

        } else {
          if (TRANSACTIONSMANAGER.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
            LOG.error("transaction is marked to roll back");
          } else if (!context.allConnectionClosed()) {
            LOG.error("not all connection to database are closed");
          } else {
            LOG.error("transaction manager in undefined status");
          }
          TRANSACTIONSMANAGER.rollback();
        }
      }
    } catch (EFapsException e) {
      LOG.error("could not check name and password", e);
    } catch (NotSupportedException e) {
      LOG.error("could not initialise the context", e);
    } catch (RollbackException e) {
      LOG.error("", e);
    } catch (HeuristicRollbackException e) {
      LOG.error("", e);
    } catch (HeuristicMixedException e) {
      LOG.error("", e);
    } catch (javax.transaction.SystemException e) {
      LOG.error("", e);
    }
    finally {
      context.close();
    }

    return loginOk;
  }

  /**
   * method that opens a new Context in eFaps, setting the User, Locale an the
   * RequessParamters
   *
   * @see #attach()
   */
  @SuppressWarnings("unchecked")
  private void openContext() {
    try {
      if (TRANSACTIONSMANAGER.getStatus() != Status.STATUS_ACTIVE) {
        Map<String, String[]> parameter =
            RequestCycle.get().getRequest().getParameterMap();

        TRANSACTIONSMANAGER.begin();
        Context.newThreadContext(TRANSACTIONSMANAGER.getTransaction(),
            this.username, super.getLocale(), null, parameter, null);
      }
    } catch (EFapsException e) {
      LOG.error("could not initialise the context", e);
      throw new RestartResponseException(new ErrorPage(e));
    } catch (NotSupportedException e) {
      LOG.error("could not initialise the context", e);
      throw new RestartResponseException(new ErrorPage(e));
    } catch (SystemException e) {
      LOG.error("could not initialise the context", e);
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * method to close the opened Context, and commit or rollback it
   */
  private void closeContext() {
    try {
      if (TRANSACTIONSMANAGER.getStatus() == Status.STATUS_ACTIVE) {
        Context.commit();
      } else {
        if (TRANSACTIONSMANAGER.getStatus() == Status.STATUS_MARKED_ROLLBACK) {

        }
        Context.rollback();
      }
    } catch (SecurityException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (IllegalStateException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (RollbackException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (HeuristicMixedException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (HeuristicRollbackException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (SystemException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }

  }
}
