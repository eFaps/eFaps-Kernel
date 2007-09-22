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
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;

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

  private final Map<String, Component> componentcache =
      new HashMap<String, Component>();

  private IModel model;

  private String username;

  public EFapsSession(final Request _request) {
    super(_request);
  }

  @Override
  protected void attach() {
    super.attach();
    if (this.isLogedIn()) {
      openContext();
    }
  }

  @Override
  protected void detach() {
    super.detach();

    try {
      if (this.isLogedIn()
          && TRANSACTIONSMANAGER.getStatus() != Status.STATUS_NO_TRANSACTION) {
        closeContext();
      }
    } catch (SystemException e) {
      e.printStackTrace();
    }

  }

  public void setIntoCache(final String _key, final Component _component) {
    this.componentcache.remove(_key);
    this.componentcache.put(_key, _component);
  }

  public Component getFromCache(final String _key) {
    return this.componentcache.get(_key);
  }

  public void removeFromCache(final String _key) {
    this.componentcache.remove(_key);
  }

  public void setOpenerModel(final IModel _model) {
    this.model = _model;
  }

  public IModel getOpenerModel() {
    return this.model;
  }

  public boolean isLogedIn() {
    if (this.username != null) {
      return true;
    } else {
      return false;
    }
  }

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

  public final void checkout() {
    this.username = null;
    closeContext();
  }

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
      e.printStackTrace();
    } catch (NotSupportedException e) {
      e.printStackTrace();
    } catch (SystemException e) {
      e.printStackTrace();
    }
  }

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
      e.printStackTrace();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    } catch (EFapsException e) {
      e.printStackTrace();
    } catch (RollbackException e) {
      e.printStackTrace();
    } catch (HeuristicMixedException e) {
      e.printStackTrace();
    } catch (HeuristicRollbackException e) {
      e.printStackTrace();
    } catch (SystemException e) {
      e.printStackTrace();
    }

  }
}
