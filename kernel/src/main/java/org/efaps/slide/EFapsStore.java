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

package org.efaps.slide;

import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.transaction.file.ResourceManager;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.util.xa.AbstractTransactionalResource;
import org.apache.commons.transaction.util.xa.TransactionalResource;
import org.apache.slide.authenticate.CredentialsToken;
import org.apache.slide.common.*;
import org.apache.slide.common.AbstractService;
import org.apache.slide.content.*;
import org.apache.slide.lock.*;
import org.apache.slide.security.*;
import org.apache.slide.store.*;
import org.apache.slide.structure.*;
import org.apache.slide.util.logger.Logger;
import org.apache.slide.util.Messages;

import org.efaps.db.Instance;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Checkin;

/**
 * Transactional descriptors file store.
 * Represents descriptor data as XML using {@link XMLResourceDescriptor}.
 *
 * <br><br>
 * <em>Note</em>: To achieve search performance on properties comparable to a RDBMS some sort of index will be needed.
 *
 * @see XMLResourceDescriptor
 */
//ContentStore, LockStore, NodeStore, RevisionDescriptorsStore, RevisionDescriptorStore, SecurityStore, , Service, Store, javax.transaction.xa.XAResource
//SequenceStore
public class EFapsStore extends AbstractXAServiceBase
    implements NodeStore, LockStore, RevisionDescriptorsStore, RevisionDescriptorStore, SecurityStore, ContentStore, Store {

    protected static final String LOG_CHANNEL = "file-meta-store";

    protected static final int DEBUG_LEVEL = Logger.DEBUG;

    protected static final String ENCODING_PARAMETER = "encoding";

    protected static final String DEFER_SAVING_PARAMETER = "defer-saving";

    protected String characterEncoding = "UTF-8"; // a save choice
    protected boolean deferSaving = false; // the save choice

    protected Map suspendedContexts = new HashMap();
    protected Map activeContexts = new HashMap();

private Map parameters = null;
    public void setParameters(Hashtable _parameters)
        throws ServiceParameterErrorException, ServiceParameterMissingException {

this.parameters = _parameters;

System.out.println("######## setParemters="+_parameters);
/*        super.setParameters(parameters);

        String encoding = (String) parameters.get(ENCODING_PARAMETER);
        if (encoding != null) {
            characterEncoding = encoding;
        }

        String deferSavingString = (String) parameters.get(DEFER_SAVING_PARAMETER);
        if (deferSavingString != null) {
            deferSaving = Boolean.valueOf(deferSavingString).booleanValue();
            if (deferSaving) {
                getLogger().log(
                    "Enabling deferred saving",
                    LOG_CHANNEL,
                    Logger.INFO);
            }
        }
*/
    }

    /*
     * --- public methods of interface NodeStore ---
     *
     *
     */

  public ObjectNode retrieveObject(Uri _uri) throws ServiceAccessException, ObjectNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>retrieveObject.uri="+_uri);
System.out.println("------------------>retrieveObject.uri.getNamespace()="+_uri.getNamespace());
ObjectNode ret = null;
enlist();
try  {
    TransactionId id = ((TransactionId)getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        ret = id.retrieveObject(_uri);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      ret = id.retrieveObject(_uri);
    }
} catch (ServiceAccessException e)  {
  delist(false);
  throw e;
} catch (ObjectNotFoundException e)  {
  delist(false);
  throw e;
} catch (Error e)  {
  delist(false);
  throw e;
} catch (Throwable e)  {
  delist (false);
  throw new ServiceAccessException(this, e);
}
delist(true);
    return ret;
  }






  public void storeObject(Uri _uri, ObjectNode _object) throws ServiceAccessException, ObjectNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>storeObject.uri="+_uri+":object="+_object);
enlist();
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        id.storeObject(_uri, _object);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      id.storeObject(_uri, _object);
    }
delist(true);
  }

  public void createObject(Uri _uri, ObjectNode _object) throws ServiceAccessException, ObjectAlreadyExistsException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>createObject.uri="+_uri+":object="+_object);
enlist();
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        id.createObject(_uri, _object);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      id.createObject(_uri, _object);
    }
delist(true);
  }

  public void removeObject(Uri _uri, ObjectNode _object) throws ServiceAccessException, ObjectNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>removeObject.uri="+_uri+":object="+_object);
enlist();
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        id.removeObject(_uri, _object);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      id.removeObject(_uri, _object);
    }
delist(true);
  }

    /*
     * --- public methods of interface SecurityStore ---
     *
     *
     */

    public void grantPermission(Uri uri, NodePermission permission) throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>grantPermission");
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.grantPermission(permission);
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

    public void revokePermission(Uri uri, NodePermission permission) throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>revokePermission");
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.revokePermission(permission);
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

    public void revokePermissions(Uri uri) throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>revokePermissions");
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.revokePermissions();
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

    public Enumeration enumeratePermissions(Uri uri) throws ServiceAccessException {
Enumeration ret = (new java.util.Vector()).elements();
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>enumeratePermissions.return="+ret);
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
return ret;
//        try {
//            XMLResourceDescriptor xfd = getFileDescriptor(uri);
//            return xfd.enumeratePermissions();
//        } catch (ObjectNotFoundException e) {
//            throwInternalError(e);
//            return null; // XXX fake (is never called)
//        }
    }

    /*
     * --- public methods of interface LockStore ---
     *
     *
     */
    public void putLock(Uri uri, NodeLock lock) throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>putLock.uri="+uri+":lock="+lock);
/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.putLock(lock);
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

    public void renewLock(Uri uri, NodeLock lock) throws ServiceAccessException, LockTokenNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>renewLock.uri="+uri+":lock="+lock);
/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.renewLock(lock);
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

    public void removeLock(Uri uri, NodeLock lock) throws ServiceAccessException, LockTokenNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>removeLock.uri="+uri+":lock="+lock);
/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.removeLock(lock);
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throw new LockTokenNotFoundException(lock);
        }
*/
    }

    public void killLock(Uri uri, NodeLock lock) throws ServiceAccessException, LockTokenNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>killLock.uri="+uri+":lock="+lock);
/*
System.out.println("------------------>killLock");
        removeLock(uri, lock);
*/
    }

  public Enumeration enumerateLocks(Uri _uri) throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>enumerateLocks.uri="+_uri);
Enumeration ret = null;
enlist();
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        ret = id.enumerateLocks(_uri);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      ret = id.enumerateLocks(_uri);
    }
delist(true);
return ret;
  }

    /*
     * --- public methods of interface RevisionDescriptorsStore ---
     *
     *
     */

  public NodeRevisionDescriptors retrieveRevisionDescriptors(Uri _uri) throws ServiceAccessException, RevisionDescriptorNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>retrieveRevisionDescriptors.uri="+_uri);
enlist();
NodeRevisionDescriptors ret = null;
try  {
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        ret = id.retrieveRevisionDescriptors(_uri);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      ret = id.retrieveRevisionDescriptors(_uri);
    }
} catch (ServiceAccessException e)  {
  delist(false);
  throw e;
} catch (RevisionDescriptorNotFoundException e)  {
  delist(false);
  throw e;
} catch (Error e)  {
  delist(false);
  throw e;
} catch (Throwable e)  {
  delist (false);
  throw new ServiceAccessException(this, e);
}
delist(true);
return ret;
  }

    public void createRevisionDescriptors(Uri uri, NodeRevisionDescriptors revisionDescriptors)
        throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>createRevisionDescriptors.uri="+uri+":revisionDescriptors="+revisionDescriptors);
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());

/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.createRevisionDescriptors(revisionDescriptors);
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

  public void storeRevisionDescriptors(Uri uri, NodeRevisionDescriptors revisionDescriptors)
      throws ServiceAccessException, RevisionDescriptorNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>storeRevisionDescriptors.uri="+uri+":revisionDescriptors="+revisionDescriptors);

  }

    public void removeRevisionDescriptors(Uri uri) throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>removeRevisionDescriptors.uri="+uri);
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());

/*
        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.removeRevisionDescriptors();
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

    /*
     * --- public methods of interface RevisionDescriptorStore ---
     *
     *
     */

    public NodeRevisionDescriptor retrieveRevisionDescriptor(Uri _uri, NodeRevisionNumber _revisionNumber)
        throws ServiceAccessException, RevisionDescriptorNotFoundException {

System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>retrieveRevisionDescriptor.uri="+_uri+":revisionNumber="+_revisionNumber);
NodeRevisionDescriptor ret = null;
enlist();
      TransactionId id = ((TransactionId)getCurrentlyActiveTransactionalResource());
      if (id == null) {
        id = createTransactionResource(_uri);
        try {
          ret = id.retrieveRevisionDescriptor(_uri, _revisionNumber);
        } finally {
          try {
            id.commit();
          } catch (XAException e) {
            throw new ServiceAccessException(this, e);
          }
        }
      } else  {
        ret = id.retrieveRevisionDescriptor(_uri, _revisionNumber);
      }
delist(true);
      return ret;
    }

    public void createRevisionDescriptor(Uri uri, NodeRevisionDescriptor revisionDescriptor)
        throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>createRevisionDescriptor.uri="+uri+":"+revisionDescriptor);
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());

/*        try {
            XMLResourceDescriptor xfd = getFileDescriptor(uri);
            xfd.createRevisionDescriptor(revisionDescriptor);
            if (deferSaving) {
                xfd.registerForSaving();
            } else {
                xfd.save();
            }
        } catch (ObjectNotFoundException e) {
            throwInternalError(e);
        }
*/
    }

  public void storeRevisionDescriptor(Uri _uri, NodeRevisionDescriptor _revisionDescriptor) throws ServiceAccessException, RevisionDescriptorNotFoundException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>storeRevisionDescriptor.uri="+_uri+":revisionDescriptor="+_revisionDescriptor);
enlist();
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        id.storeRevisionDescriptor(_uri, _revisionDescriptor);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      id.storeRevisionDescriptor(_uri, _revisionDescriptor);
    }
delist(true);
  }

  public void removeRevisionDescriptor(Uri _uri, NodeRevisionNumber _revisionNumber) throws ServiceAccessException {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>removeRevisionDescriptor.uri="+_uri+":revisionNumber="+_revisionNumber);
enlist();
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        id.removeRevisionDescriptor(_uri, _revisionNumber);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      id.removeRevisionDescriptor(_uri, _revisionNumber);
    }
delist(true);
  }

  //////////////////////////////////////////////////////////////////////////////
  // interface ContentStore


  /**
   * Create revision content.
   */
  public void createRevisionContent(Uri uri, NodeRevisionDescriptor revisionDescriptor, NodeRevisionContent revisionContent)  {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>createRevisionContent.uri="+uri+":revisionDescriptor="+revisionDescriptor+":revisionContent="+revisionContent);
//TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());

  }

  /**
   * Remove revision content.
   */
  public void removeRevisionContent(Uri _uri, NodeRevisionDescriptor _revisionDescriptor) throws ServiceAccessException  {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>removeRevisionContent.uri="+_uri+":revisionDescriptor="+_revisionDescriptor);
enlist();
    TransactionId id = ((TransactionId) getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        id.removeRevisionContent(_uri, _revisionDescriptor);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      id.removeRevisionContent(_uri, _revisionDescriptor);
    }
delist(true);
  }

  /**
   * Retrieve revision content.
   */
  public NodeRevisionContent retrieveRevisionContent(Uri _uri, NodeRevisionDescriptor _revisionDescriptor) throws ServiceAccessException, RevisionNotFoundException  {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>retrieveRevisionContent.uri="+_uri+":revisionDescriptor="+_revisionDescriptor);
NodeRevisionContent ret = null;
enlist();
    TransactionId id = ((TransactionId)getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        ret = id.retrieveRevisionContent(_uri, _revisionDescriptor);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      ret = id.retrieveRevisionContent(_uri, _revisionDescriptor);
    }
delist(true);
    return ret;
  }

  /**
   * Modify revision content.
   */
  public void storeRevisionContent(Uri _uri, NodeRevisionDescriptor _revisionDescriptor, NodeRevisionContent _revisionContent) throws ServiceAccessException, RevisionNotFoundException  {
System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Thread.currentThread()"+Thread.currentThread().getId());
System.out.println("------------------>storeRevisionContent.uri="+_uri+":revisionDescriptor="+_revisionDescriptor+":revisionContent="+_revisionContent);
enlist();
try  {
    TransactionId id = ((TransactionId)getCurrentlyActiveTransactionalResource());
    if (id == null) {
      id = createTransactionResource(_uri);
      try {
        id.storeRevisionContent(_uri, _revisionDescriptor, _revisionContent);
      } finally {
        try {
          id.commit();
        } catch (XAException e) {
          throw new ServiceAccessException(this, e);
        }
      }
    } else  {
      id.storeRevisionContent(_uri, _revisionDescriptor, _revisionContent);
    }
} catch (ServiceAccessException e)  {
  delist(false);
  throw e;
} catch (RevisionNotFoundException e)  {
  delist(false);
  throw e;
} catch (Error e)  {
  delist(false);
  throw e;
} catch (Throwable e)  {
  delist (false);
  throw new ServiceAccessException(this, e);
}
delist(true);
  }



  //////////////////////////////////////////////////////////////////////////////

/*    public String toString() {
        return "TxXMLFileDescriptorsStore at " + storeDir + "  working on " + workDir;
    }
*/
/*
    public synchronized void commit(Xid xid, boolean onePhase) throws XAException {
System.out.println("------------------>commit");
        try {
            super.commit(xid, onePhase);
        } finally {
            Object txId = wrap(xid);
            activeContexts.remove(txId);
            suspendedContexts.remove(txId);
            activeTransactionBranch.set(null);
        }

    }
*/

/*
    public synchronized void rollback(Xid xid) throws XAException {
System.out.println("------------------>rollback");
        try {
            super.rollback(xid);
        } finally {
            Object txId = wrap(xid);
            activeContexts.remove(txId);
            suspendedContexts.remove(txId);
            activeTransactionBranch.set(null);
        }
    }
*/
/*
    public synchronized int prepare(Xid xid) throws XAException {
System.out.println("------------------>prepare");
        Object txId = wrap(xid);
        getLogger().log(
            "Thread " + Thread.currentThread() + " prepares transaction branch " + txId,
            LOG_CHANNEL,
            Logger.DEBUG);
        try {
            if (deferSaving) {
                // save all descriptors registered for saving
                TxContext txContext = (TxContext) activeContexts.get(txId);
                if (txContext == null) txContext = (TxContext) suspendedContexts.get(txId);
                // really should not, but only to be sure...
                if (txContext != null) {
                    try {
                        txContext.saveDescriptors();
                    } catch (ObjectNotFoundException onfe) {
                        getLogger().log(
                            "Thread " + Thread.currentThread() + " failed to prepare transaction branch " + txId,
                            onfe,
                            LOG_CHANNEL,
                            Logger.CRITICAL);
                        throw new XAException(onfe.toString());
                    } catch (ServiceAccessException sae) {
                        getLogger().log(
                            "Thread " + Thread.currentThread() + " failed to prepare transaction branch " + txId,
                            sae,
                            LOG_CHANNEL,
                            Logger.CRITICAL);
                        throw new XAException(sae.toString());
                    }
                } else {
                    getLogger().log(
                        "Thread " + Thread.currentThread() + " could prepare *unknown* transaction branch " + txId,
                        LOG_CHANNEL,
                        Logger.WARNING);
                }
            }
            int status = rm.prepareTransaction(txId);
            switch (status) {
                case ResourceManager.PREPARE_SUCCESS_READONLY :
                    return XA_RDONLY;
                case ResourceManager.PREPARE_SUCCESS :
                    return XA_OK;
                default :
                    throw new XAException(XAException.XA_RBROLLBACK);
            }
        } catch (ResourceManagerException e) {
            getLogger().log(
                "Thread " + Thread.currentThread() + " failed to prepare transaction branch " + txId,
                e,
                LOG_CHANNEL,
                Logger.CRITICAL);
            throw createXAException(e);
        }
return 0;
    }
*/
/*

    public synchronized void end(Xid xid, int flags) throws XAException {
System.out.println("------------------>end");
        if (getActiveTxId() == null) {
            throw new XAException(XAException.XAER_INVAL);
        }
        Object txId = wrap(xid);
        Thread currentThread = Thread.currentThread();
        getLogger().log(
            "Thread "
                + currentThread
                + (flags == TMSUSPEND ? " suspends" : flags == TMFAIL ? " fails" : " ends")
                + " work on behalf of transaction branch "
                + txId,
            LOG_CHANNEL,
            DEBUG_LEVEL);

        switch (flags) {
            case TMSUSPEND :
                suspendedContexts.put(txId, getActiveTxContext());
                activeContexts.remove(txId);
                activeTransactionBranch.set(null);
                break;
            case TMFAIL :
                try {
                    rm.markTransactionForRollback(wrap(xid));
                } catch (ResourceManagerException e) {
                    throw createXAException(e);
                }
                activeTransactionBranch.set(null);
                break;
            case TMSUCCESS :
                activeTransactionBranch.set(null);
                break;
        }
    }
*/
/*
    public synchronized void start(Xid xid, int flags) throws XAException {
System.out.println("------------------>start");
        Object txId = wrap(xid);
        Thread currentThread = Thread.currentThread();
        getLogger().log(
            "Thread "
                + currentThread
                + (flags == TMNOFLAGS ? " starts" : flags == TMJOIN ? " joins" : " resumes")
                + " work on behalf of transaction branch "
                + txId,
            LOG_CHANNEL,
            DEBUG_LEVEL);

        switch (flags) {
            // a new transaction
            case TMNOFLAGS :
                if (getActiveTxId() != null) {
                    throw new XAException(XAException.XAER_INVAL);
                }
                try {
                    rm.startTransaction(txId);
                    TxContext txContext = new TxContext(txId);
                    activeTransactionBranch.set(txContext);
                    activeContexts.put(txId, txContext);
                } catch (ResourceManagerException e) {
                    throw createXAException(e);
                }
                break;
            case TMJOIN :
                if (getActiveTxId() != null) {
                    throw new XAException(XAException.XAER_INVAL);
                }
                try {
                    if (rm.getTransactionState(txId) == STATUS_NO_TRANSACTION) {
                        throw new XAException(XAException.XAER_INVAL);
                    }
                } catch (ResourceManagerException e) {
                    throw createXAException(e);
                }
                TxContext txContext = new TxContext(txId);
                activeTransactionBranch.set(txContext);
                activeContexts.put(txId, txContext);
                break;
            case TMRESUME :
                if (getActiveTxId() != null) {
                    throw new XAException(XAException.XAER_INVAL);
                }
                txContext = (TxContext) suspendedContexts.remove(txId);
                if (txContext == null) {
                    throw new XAException(XAException.XAER_NOTA);
                }
                activeTransactionBranch.set(txContext);
                break;
        }

    }
*/

    /*
     * --- XMLFileDescriptor access and caching methods ---
     *
     *
     */

    /**
     * Either returns a cached file descriptor or loads it from DB
     */
/*    protected XMLResourceDescriptor getFileDescriptor(Uri uri) throws ServiceAccessException, ObjectNotFoundException {
System.out.println("------------------>getFileDescriptor"+uri);
        TxContext txContext = getActiveTxContext();
        XMLResourceDescriptor xfd;
        if (txContext != null) {
            xfd = txContext.lookup(uri);
            if (xfd == null) {
                Object txId = txContext.xid;
                xfd = new XMLResourceDescriptor(uri, this, rm, txId, characterEncoding);
                xfd.load();
                if (txId != null) {
                    txContext.register(uri, xfd);
                }
            }
        } else {
            xfd = new XMLResourceDescriptor(uri, this, rm, null, characterEncoding);
            xfd.load();
        }
        if (!xfd.getUri().equals(uri.toString())) {
           // this may happen with files systems that don't operate case sensitive
           // e.g. requested uri /files/test.doc but found /files/TEST.DOC
           throw new ObjectNotFoundException(uri);
        }
System.out.println("------------------>getFileDescriptor.return"+xfd);
        return xfd;
    }
*/
/*    protected TxContext getActiveTxContext() {
System.out.println("------------------>getActiveTxContext");
        TxContext context = (TxContext)activeTransactionBranch.get();
System.out.println("------------------>getActiveTxContext.return="+context);
        return context;
    }
*/
/*    protected Object getActiveTxId() {
System.out.println("------------------>getActiveTxId");
        TxContext context = (TxContext) activeTransactionBranch.get();
        return (context == null ? null : context.xid);
    }
*/
/*
    protected String getLogChannel() {
System.out.println("------------------>getLogChannel");
        return LOG_CHANNEL;
    }
*/

/*    private static class TxContext {
        public Object xid;
        public Map descriptors = new HashMap();

        public TxContext(Object xid) {
            this.xid = xid;
        }

        public void register(Uri uri, XMLResourceDescriptor xfd) {
            descriptors.put(uri.toString(), xfd);
        }

        public void deregister(Uri uri) {
            descriptors.remove(uri.toString());
        }

        public XMLResourceDescriptor lookup(Uri uri) {
            return (XMLResourceDescriptor) descriptors.get(uri.toString());
        }

        public Collection list() {
            return descriptors.values();
        }

        public void saveDescriptors() throws ObjectNotFoundException, ServiceAccessException {
            for (Iterator it = list().iterator(); it.hasNext();) {
                XMLResourceDescriptor xfd = (XMLResourceDescriptor) it.next();
                if (xfd.isRegisteredForSaving()) xfd.save();
            }
        }

    }
*/


  /////////////////////////////////////////////////////////////////////////////
  // transaction handling

//static private class StoreContext  {
//
//
//  public void finalize()  {
//    System.out.println("StoreContext......finalize");
//    disconnect();
//  }
//
//private String user = null;
//private org.efaps.db.Context context = null;
//
//  private void connect(final Service _service, final String _user) throws ServiceConnectionFailedException  {
//    if (this.user==null || this.context==null || !this.user.equals(_user))  {
//System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
//System.out.println("connect:"+_user+":old="+this.user);
//      if (context!=null)  {
//        this.context.close();
//      }
//try  {
//this.context = new org.efaps.db.Context();
//} catch (Exception e)  {
//e.printStackTrace();
//throw new ServiceConnectionFailedException(_service, e);
//}
//this.user = _user;
//    }
//  }
//
//  private void disconnect()  {
//System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
//System.out.println("disconnect");
//this.user = null;
//    if (context!=null)  {
//      this.context.close();
//    }
//this.context = null;
//  }
//
//  public org.efaps.db.Context getContext()  {
//System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
//System.out.println("user="+user+":getContext()="+this.context);
//    return this.context;
//  }
//}
//
//
//protected static ThreadLocal<StoreContext> storeContext = new ThreadLocal<StoreContext>() {
//  protected synchronized StoreContext initialValue() {
//    return new StoreContext();
//  }
//};
//
//  public void start(Xid _xid, int _flags) throws XAException  {
//System.out.println("###############################------------------>start.xid="+_xid+":flags="+_flags);
//    super.start(_xid, _flags);
//  }
//
//  public void end(Xid _xid, int _flags) throws XAException {
//System.out.println("###############################------------------>end.xid="+_xid+":flags="+_flags);
//    super.end(_xid, _flags);
//  }
//
//
//  public void connect(CredentialsToken _crdtoken) throws ServiceConnectionFailedException {
//System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
////System.out.println("###############################------------------>connect._crdtoken="+_crdtoken);
//System.out.println("###############################------------------>connect._crdtoken="+_crdtoken.getPrincipal());
////System.out.println("###############################------------------>connect._crdtoken="+_crdtoken.getPrivateCredentials());
////System.out.println("###############################------------------>connect._crdtoken="+_crdtoken.getPublicCredentials());
////System.out.println("###############################------------------>connect._crdtoken="+_crdtoken.isTrusted());
////super.connect(_crdtoken);
//storeContext.get().connect(this, _crdtoken.getPrincipal().getName());
//  }
//
////public boolean connectIfNeeded(CredentialsToken crdtoken) throws ServiceConnectionFailedException,ServiceAccessException  {
////System.out.println("###############################------------------>connectIfNeeded._crdtoken="+crdtoken.getPrincipal().getName());
////System.out.println("###############################------------------>connectIfNeeded."+storeContext.get().connected+":"+storeContext.get().user);
////System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
////return super.connectIfNeeded(crdtoken);
////}
//
//
//  public void disconnect() throws ServiceDisconnectionFailedException {
////System.out.println("###############################------------------>disconnect");
////System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
//storeContext.get().disconnect();
//  }
//
//  public void connect() throws ServiceConnectionFailedException {
////System.out.println("###############################------------------>connect");
////System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
////storeContext.get().connected=true;
//  }
//
//  public void reset() throws ServiceResetFailedException {
////System.out.println("###############################------------------>reset");
////System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
//storeContext.get().disconnect();
//  }
//
//  public boolean isConnected() throws ServiceAccessException {
//System.out.println("###############################------------------>Thread.currentThread()"+Thread.currentThread().getId());
//System.out.println("###############################------------------>isConnect"+storeContext.get().user);
////return storeContext.get().connected;
//return false;
//  }
////boolean connected = false;
//
  /**
   * Indicates whether or not the objects managed by this service should be
   * cached. Caching is disabled by default.<br/>
   * Overwrites orginal method because caching is not allowed!
   *
   * @return boolean false if results should be not cached!
   */
  public boolean cacheResults() {
    return false;
  }

  ///////////////////////////

  protected static ThreadLocal credentials = new ThreadLocal();

  public void connect(CredentialsToken _crdtoken) throws ServiceConnectionFailedException {
    credentials.set(_crdtoken);
  }

  public void connect() throws ServiceConnectionFailedException {
  }

  public void reset() throws ServiceResetFailedException {
    credentials.set(null);
  }

  public void disconnect() throws ServiceDisconnectionFailedException {
    credentials.set(null);
  }

  public boolean isConnected() throws ServiceAccessException {
    return credentials.get() != null;
  }

  // AbstractXAResource

  protected boolean includeBranchInXid() {
    return false;
  }

  public Xid[] recover(int arg0) throws XAException {
    return null;
  }

  public boolean isSameRM(XAResource rm) throws XAException {
    return false;
  }

  // XAResource
  public boolean setTransactionTimeout(int seconds) throws XAException {
    return false;
  }

  public int getTransactionTimeout() throws XAException {
    return 0;
  }


  protected TransactionalResource createTransactionResource(Xid xid) throws ServiceAccessException {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö createTransactionResource.xid="+xid);
    CredentialsToken token = (CredentialsToken)credentials.get();
    Principal principal = null;
    if (token != null)  {
      principal = token.getPrincipal();
    }
    return new TransactionId(xid, this, principal);
  }

  protected TransactionId createTransactionResource(Uri uri) throws ServiceAccessException {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö createTransactionResource.uri="+uri);
    return new TransactionId(null, this, uri.getToken().getCredentialsToken().getPrincipal());
  }

  /////////////////////////////////////////////////////////////////////////////

  protected static class TransactionId extends AbstractTransactionalResource {


protected boolean includeBranchInXid() {
  return false;
}


Principal principal = null;
org.efaps.db.Context context = null;
HashSet toBeCreated = new HashSet();

Service service = null;

    /**
     * @todo hardcoded user that insert / update works
     */
    TransactionId(final Xid _xid, final Service _service, final Principal _principal) throws ServiceAccessException  {
      super(_xid);
      principal = _principal;
try  {
// TODO: Hack, damit insert / update funkt!!
  this.context = new org.efaps.db.Context(org.efaps.admin.user.Person.get("Administrator"));
//{
//} catch (javax.naming.NamingException e)  {
//  throw new ServiceAccessException(_service, e);
//} catch (java.sql.SQLException e) {
//  throw new ServiceAccessException(_service, e);
//}
} catch (Exception e) {
  throw new ServiceAccessException(_service, e);
}

this.service = null;
    }


    public void commit() throws XAException {
System.out.println("------------------------------------------------------commit");
this.context.close();
this.context = null;
/*        try {
            store.commit();
        } catch (ServiceAccessException e) {
            getLogger().log("Could not commit store " + store, e, LOG_CHANNEL, Logger.ERROR);
            throw new XAException(XAException.XA_RBCOMMFAIL);
        } finally {
            closeConnection();
        }
*/
    }

    public void rollback() throws XAException {
this.context.close();
this.context = null;
/*        try {
            store.rollback();
        } catch (ServiceAccessException e) {
            getLogger().log("Could not rollback store " + store, e, LOG_CHANNEL, Logger.ERROR);
            throw new XAException(XAException.XA_RBCOMMFAIL);
        } finally {
            closeConnection();
        }
*/
    }

    public void begin() throws XAException {
    }

    public void suspend() throws XAException {
    }

    public void resume() throws XAException {
    }

    public int prepare() throws XAException {
return XA_OK;
//        return (readOnly ? XA_RDONLY : XA_OK);
    }


    public ObjectNode retrieveObject(Uri _uri) throws ServiceAccessException, ObjectNotFoundException {
      SubjectNode subject = new SubjectNode(_uri.toString());

System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);

      try  {

        if (_uri.toString().equals("/"))  {
          subject.addChild(new SubjectNode("files"));
          subject.addChild(new SubjectNode("user"));
        } else if (_uri.toString().startsWith("/user"))  {
        } else if (_uri.toString().equals("/files"))  {
          org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
          query.setQueryTypes(this.context, "TeamCenter_RootFolder");
          query.addSelect(this.context, "Name");
          query.execute(this.context);
          while (query.next())  {
            subject.addChild(new SubjectNode(query.get(this.context, "Name").toString()));
          }
          query.close();
        } else if (isObjectCreateScheduled(_uri))  {
// nothing to do......
        } else  {

          Instance instance = getFolderInstance(this.context, _uri);

if (instance == null)  {
  throw new ObjectNotFoundException(_uri);
}
          if (isFolder(instance))  {

            org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
            query.setExpand(this.context, instance, "TeamCenter_Folder\\ParentFolder");
            query.addSelect(this.context, "Name");
            query.addSelect(this.context, "OID");
            query.addSelect(this.context, "Created");
            query.addSelect(this.context, "Modified");
            query.execute(this.context);
            while (query.next())  {
              String name = query.get(this.context, "Name").toString();
              String oid  = query.get(this.context, "OID").toString();
              String uri  = _uri.toString()+"/"+name;
              Instance folderInstance = new Instance(this.context, oid);
              this.uriCache.put(_uri.toString()+"/"+name, folderInstance);
              InstanceProperty prop = new InstanceProperty(
                  instance,
                  (Date)query.get(this.context, "Created"),
                  (Date)query.get(this.context, "Modified")
              );
              this.uriCache.put(uri, folderInstance);
              this.uriPropertyCache.put(uri, prop);
              subject.addChild(new SubjectNode(name));
            }
            query.close();


            query = new org.efaps.db.SearchQuery();
            query.setExpand(this.context, instance, "TeamCenter_Document2Folder\\Folder.Document");
            query.addSelect(this.context, "FileName");
            query.addSelect(this.context, "FileLength");
            query.addSelect(this.context, "Created");
            query.addSelect(this.context, "Modified");
            query.addSelect(this.context, "OID");
            query.execute(this.context);
            while (query.next())  {
              String oid  = query.get(this.context, "OID").toString();
              String name = query.get(this.context, "FileName").toString();
              String uri  = _uri.toString()+"/"+name;
              Instance docInstance = new Instance(this.context, oid);
              InstanceProperty prop = new InstanceProperty(
                  instance,
                  (Long)query.get(this.context, "FileLength"),
                  (Date)query.get(this.context, "Created"),
                  (Date)query.get(this.context, "Modified")
              );
              this.uriCache.put(uri, docInstance);
              this.uriPropertyCache.put(uri, prop);
              subject.addChild(new SubjectNode(name));
            }
            query.close();
          }

        }

      } catch (ObjectNotFoundException e)  {
        throw e;
      } catch (Exception e)  {
e.printStackTrace();
        throw new ServiceAccessException(this.service, e);
      }

      return subject;
    }

    public void storeObject(Uri _uri, ObjectNode _object) throws ServiceAccessException, ObjectNotFoundException {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);
//        checkAuthentication();
//        if (!objectExists(uri))
//            throw new ObjectNotFoundException(uri);
    }


    public void createObject(Uri _uri, ObjectNode _object) throws ServiceAccessException, ObjectAlreadyExistsException  {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);
System.out.println("????????????????????????????????????????????????????????????????????????????????????????????? "+_object.getClass().getName());
//if (_object instanceof LinkNode) {
//System.out.println("????????????????????????????????????????????????????????????????????????????????????????????? create folder");
//} else  {
//System.out.println("????????????????????????????????????????????????????????????????????????????????????????????? create document");
//}


/*
        checkAuthentication();
        try {
            if (store.objectExists(uri.toString()))
                throw new ObjectAlreadyExistsException(uri.toString());
        } catch (AccessDeniedException e) {
            throw new ServiceAccessException(service, e);
        } catch (ObjectLockedException e) {
            throw new ServiceAccessException(service, e);
        }
        // now, we do not have enough information, let's wait until we have
        // it...
        toBeCreated.add(uri.toString());
*/
this.toBeCreated.add(_uri.toString());
    }

    public void removeObject(Uri _uri, ObjectNode _object) throws ServiceAccessException, ObjectNotFoundException {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);

      try  {
        Instance instance = getFolderInstance(this.context, _uri);
        if (instance==null)  {
           throw new ObjectNotFoundException(_uri);
        }

// TODO
        // if not folder, delete links of document to folder!
        if (!isFolder(instance))  {
        }
        Delete delete = new Delete(this.context, instance);
        delete.execute(this.context);
//        delete.close();
      } catch (ObjectNotFoundException e)  {
        throw e;
      } catch (Exception e)  {
        throw new ServiceAccessException(this.service, e);
      }

// warum notwendig????
      toBeCreated.remove(_uri.toString());
    }

    public Enumeration enumerateLocks(Uri _uri) throws ServiceAccessException {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);
return (new Vector()).elements();
/*
        checkAuthentication();
        if (lockStore != null) {
            try {
                WebdavStoreLockExtension.Lock[] ids = lockStore.getLockInfo(uri.toString());
                if (ids == null)
                    return new Vector().elements();
                Vector locks = new Vector(ids.length);
                for (int i = 0; i < ids.length; i++) {
                    WebdavStoreLockExtension.Lock lockId = ids[i];
                    NodeLock lock = new NodeLock(lockId.getId(), uri.toString(), lockId.getSubject(),
                            "/actions/write", lockId.getExpirationDate(), lockId
                            .isInheritable(), lockId.isExclusive());
                    locks.add(lock);
                }
                return locks.elements();
            } catch (AccessDeniedException e) {
                throw new ServiceAccessException(service, e);
            }
        } else {
            return new Vector().elements();
        }
*/
    }

    public NodeRevisionDescriptors retrieveRevisionDescriptors(Uri _uri) throws ServiceAccessException, RevisionDescriptorNotFoundException {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);

      boolean exists = false;
      if (_uri.toString().equals("/") || _uri.toString().equals("/user") || _uri.toString().equals("/files"))  {
        exists = true;
      } else if (isObjectCreateScheduled(_uri))  {
        exists = true;
      } else  {
        try  {
          Instance instance = getFolderInstance(this.context, _uri);
          exists = (instance != null);
        } catch (Exception e)  {
          throw new ServiceAccessException(this.service, e);
        }
      }

      if (!exists)  {
        throw new RevisionDescriptorNotFoundException(_uri.toString());
      } else {
        NodeRevisionNumber rev = new NodeRevisionNumber(1, 0);

        Hashtable workingRevisions = new Hashtable();
        workingRevisions.put("main", rev);

        Hashtable latestRevisionNumbers = new Hashtable();
        latestRevisionNumbers.put("main", rev);

        Hashtable branches = new Hashtable();
        branches.put(rev, new Vector());

        return new NodeRevisionDescriptors(_uri.toString(), rev, workingRevisions, latestRevisionNumbers, branches, false);
      }
    }

    public NodeRevisionDescriptor retrieveRevisionDescriptor(Uri _uri, NodeRevisionNumber _revisionNumber)
        throws ServiceAccessException, RevisionDescriptorNotFoundException {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);

      NodeRevisionDescriptor descriptor = new NodeRevisionDescriptor(new NodeRevisionNumber(1, 0),
          NodeRevisionDescriptors.MAIN_BRANCH, new java.util.Vector(), new java.util.Hashtable());

      try  {
        if (_uri.toString().equals("/") || _uri.toString().startsWith("/user") || _uri.toString().equals("/files"))  {
          descriptor.setResourceType(NodeRevisionDescriptor.COLLECTION_TYPE);
          descriptor.setContentLength(0);

        // retrieve only from db is not scheduled to create
        } else if (!isObjectCreateScheduled(_uri))  {


InstanceProperty prop = this.uriPropertyCache.get(_uri.toString());

if (prop==null)  {

          Instance instance = getFolderInstance(this.context, _uri);

System.out.println("retrieveRevisionDescriptor.foundInstance="+instance);

          org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
          query.setObject(this.context, instance);
          query.addSelect(this.context, "Created");
          query.addSelect(this.context, "Modified");
          if (!isFolder(instance))  {
            query.addSelect(this.context, "FileLength");
          }
          query.execute(this.context);
          query.next();

          if (isFolder(instance))  {
            prop = new InstanceProperty(
                instance,
                (Date)query.get(this.context, "Created"),
                (Date)query.get(this.context, "Modified")
            );
          } else {
            prop = new InstanceProperty(
                instance,
                (Long)query.get(this.context, "FileLength"),
                (Date)query.get(this.context, "Created"),
                (Date)query.get(this.context, "Modified")
            );
          }
          this.uriPropertyCache.put(_uri.toString(), prop);

          query.close();
}
if (prop.created!=null)  {
  descriptor.setCreationDate(prop.created);
}
if (prop.modified!=null) {
  descriptor.setLastModified(prop.modified);
}
descriptor.setContentLength(prop.fileLength);
if (prop.isFolder)  {
  descriptor.setResourceType(NodeRevisionDescriptor.COLLECTION_TYPE);
} else  {
  descriptor.removeProperty(NodeRevisionDescriptor.RESOURCE_TYPE);
}

        }
        descriptor.resetRemovedProperties();
        descriptor.resetUpdatedProperties();

      } catch (Exception e)  {
e.printStackTrace();
        throw new ServiceAccessException(this.service, e);
      }

      return descriptor;
    }

        // already done in removeObject
    public void removeRevisionDescriptor(Uri _uri, NodeRevisionNumber _revisionNumber) throws ServiceAccessException {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);
//        checkAuthentication();
    }

    public void storeRevisionDescriptor(Uri _uri, NodeRevisionDescriptor revisionDescriptor)
           throws ServiceAccessException, RevisionDescriptorNotFoundException {

System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);

Enumeration updated = revisionDescriptor.enumerateUpdatedProperties();
while (updated.hasMoreElements()) {
  NodeProperty property = (NodeProperty) updated.nextElement();
  String name = property.getName();
  String ns = property.getNamespace();
//  String effectiveName = getNamespacedPropertyName(ns, name);
  String value = property.getValue().toString();
  String type = property.getType();
System.out.println("...name="+name);
System.out.println("...ns="+ns);
//System.out.println("...effectiveName="+effectiveName);
System.out.println("...value="+value);
System.out.println("...type="+type);
}

      if (toBeCreated.remove(_uri.toString())) {
        if (revisionDescriptor.getResourceType().equals(NodeRevisionDescriptor.COLLECTION_TYPE)) {
          String[] path = _uri.toString().split("/");

System.out.println("createFolder("+_uri.toString()+")");

          try  {
// TODO: test if root folder must be created (not allowed!)
            Instance parentInstance = getFolderInstance(this.context, _uri.getParentUri());

            Insert insert = new Insert(this.context, "TeamCenter_Folder");
            insert.add(this.context, "ParentFolder", ""+parentInstance.getId());
            insert.add(this.context, "Name", path[path.length-1]);
            insert.execute();
            insert.close();
          } catch (Exception e)  {
            throw new ServiceAccessException(this.service, e);
          }


//          store.createFolder(uri.toString());
        } else {
          String[] path = _uri.toString().split("/");
System.out.println("createResource("+_uri.toString()+")");
          try  {
            Instance parentInstance = getFolderInstance(this.context, _uri.getParentUri());

            Insert insert = new Insert(this.context, "TeamCenter_Document");
            insert.add(this.context, "Name", path[path.length-1]);
            insert.add(this.context, "FileName", path[path.length-1]);
            insert.execute();
Instance docInstance = insert.getInstance();
            insert.close();

            insert = new Insert(this.context, "TeamCenter_Document2Folder");
            insert.add(this.context, "Document", ""+docInstance.getId());
            insert.add(this.context, "Folder", ""+parentInstance.getId());
            insert.execute();
            insert.close();
          } catch (Exception e)  {
            throw new ServiceAccessException(this.service, e);
          }
//          store.createResource(uri.toString());
        }
      }
/*
        checkAuthentication();
        try {
            if (toBeCreated.remove(uri.toString())) {
                if (revisionDescriptor.getResourceType().equals(NodeRevisionDescriptor.COLLECTION_TYPE)) {
                    store.createFolder(uri.toString());
                } else {
                    store.createResource(uri.toString());
                }
            }


            // in initialzation phase there might be no other way to tell
            // this actually is a collection
            // if it turns out to be so we need to revoke our decission and
            // remove the resource and create a folder
            // instead
            if (tentativeResourceCreated.remove(uri.toString())
                    && revisionDescriptor.getResourceType().equals(NodeRevisionDescriptor.COLLECTION_TYPE)) {
                store.removeObject(uri.toString());
                store.createFolder(uri.toString());
            }

            if (singlePropStore != null) {
                Enumeration updated = revisionDescriptor.enumerateUpdatedProperties();
                while (updated.hasMoreElements()) {
                    NodeProperty property = (NodeProperty) updated.nextElement();
                    String name = property.getName();
                    String ns = property.getNamespace();
                    String effectiveName = getNamespacedPropertyName(ns, name);
                    String value = property.getValue().toString();
                    String type = property.getType();
                    // XXX we do not use the type as it rarely contains
                    // anything sensible
                    String effectiveValue = value;
                    singlePropStore.addOrUpdateProperty(uri.toString(), effectiveName, effectiveValue);
                }
                Enumeration removed = revisionDescriptor.enumerateRemovedProperties();
                while (removed.hasMoreElements()) {
                    NodeProperty property = (NodeProperty) removed.nextElement();
                    String name = property.getName();

                    // we might have set that before
                    if (name.equals(NodeRevisionDescriptor.RESOURCE_TYPE)) continue;

                    String ns = property.getNamespace();
                    String effectiveName = getNamespacedPropertyName(ns, name);
                    singlePropStore.removeProperty(uri.toString(), name);
                }
            } else if (bulkPropStore != null) {
                Map properties = new HashMap();
                Enumeration enum1 = revisionDescriptor.enumerateProperties();
                while (enum1.hasMoreElements()) {
                    NodeProperty property = (NodeProperty) enum1.nextElement();
                    String name = property.getName();
                    String ns = property.getNamespace();
                    String effectiveName = getNamespacedPropertyName(ns, name);
                    String value = property.getValue().toString();
                    String type = property.getType();
                    // XXX we do not use the type as it rarely contains
                    // anything sensible
                    String effectiveValue = value;
                    properties.put(effectiveName, effectiveValue);
                }
                bulkPropStore.setProperties(uri.toString(), properties);
            }

        } catch (ObjectAlreadyExistsException e) {
            throw new ServiceAccessException(service, e);
        } catch (ObjectNotFoundException e) {
            throw new RevisionDescriptorNotFoundException(uri.toString());
        } catch (AccessDeniedException e) {
            throw new ServiceAccessException(service, e);
        } catch (ObjectLockedException e) {
            throw new ServiceAccessException(service, e);
        }
*/
    }

// already done in removeObject
    public void removeRevisionContent(Uri _uri, NodeRevisionDescriptor _revisionDescriptor) throws ServiceAccessException {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);
//        checkAuthentication();

    }

    /**
     * Retrieve revision content.
     */
    public NodeRevisionContent retrieveRevisionContent(Uri uri, NodeRevisionDescriptor revisionDescriptor) throws ServiceAccessException, RevisionNotFoundException  {
      NodeRevisionContent nrc = new NodeRevisionContent();
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);

      try  {
        if (!uri.toString().equals("/files"))  {
          Instance instance = getFolderInstance(this.context, uri);

          if (isFolder(instance))  {
throw new RevisionNotFoundException(uri.toString(), revisionDescriptor.getRevisionNumber());
          } else  {
System.out.println("retrieveRevisionContent.found instance="+instance);
            org.efaps.db.Checkout checkout = new org.efaps.db.Checkout(context, instance);
// 20051126 geht nicht mehr!!!!
//            nrc.setContent(checkout.getInputStream(context));
          }

/*            checkAuthentication();
            if (!objectExists(uri)) {
                throw new RevisionNotFoundException(uri.toString(), revisionDescriptor.getRevisionNumber());
            } else {
                try {
                    NodeRevisionContent nrc = new NodeRevisionContent();
                    if (!objectExistsScheduled(uri)) {
                        InputStream in = store.getResourceContent(uri.toString());
                        nrc.setContent(in);
                    } else {
                        nrc.setContent(new byte[0]);
                    }
                    return nrc;
                } catch (ObjectNotFoundException e) {
                    throw new RevisionNotFoundException(uri.toString(), revisionDescriptor.getRevisionNumber());
                } catch (AccessDeniedException e) {
                    throw new ServiceAccessException(service, e);
                } catch (ObjectLockedException e) {
                    throw new ServiceAccessException(service, e);
                }
            }
*/
        } else  {
          throw new RevisionNotFoundException(uri.toString(), revisionDescriptor.getRevisionNumber());
        }
      } catch (RevisionNotFoundException e)  {
        throw e;
      } catch (Exception e)  {
e.printStackTrace();
        throw new ServiceAccessException(this.service, e);
      }
      return nrc;
    }

    public void storeRevisionContent(Uri _uri, NodeRevisionDescriptor _revisionDescriptor, NodeRevisionContent _revisionContent) throws ServiceAccessException, RevisionNotFoundException  {
System.out.println("principal="+principal);
System.out.println("toBeCreated="+toBeCreated);

      try  {
        String[] path = _uri.toString().split("/");
        String fileName = path[path.length-1];

        Instance instance = null;
        if (toBeCreated.remove(_uri.toString())) {
    System.out.println("createResource("+_uri.toString()+")");
          try  {
            Instance parentInstance = getFolderInstance(this.context, _uri.getParentUri());

            Insert insert = new Insert(this.context, "TeamCenter_Document");
            insert.add(this.context, "Name",      fileName);
            insert.add(this.context, "FileName",  fileName);
            insert.execute();
            instance = insert.getInstance();
            insert.close();

            insert = new Insert(this.context, "TeamCenter_Document2Folder");
            insert.add(this.context, "Document", ""+instance.getId());
            insert.add(this.context, "Folder", ""+parentInstance.getId());
            insert.execute();
            insert.close();
          } catch (Exception e)  {
            throw new ServiceAccessException(this.service, e);
          }
        } else  {
          instance = getFolderInstance(this.context, _uri);
        }

        if (instance==null)  {
throw new RevisionNotFoundException(_uri.toString(), _revisionDescriptor.getRevisionNumber());
        }
System.out.println("_revisionContent.streamContent().available()="+_revisionContent.streamContent().available());

        Checkin checkin = new Checkin(this.context, instance);
        checkin.execute(this.context, fileName,
            _revisionContent.streamContent(), -1);
//checkin.close(this.context);

      } catch (RevisionNotFoundException e)  {
        throw e;
      } catch (Exception e)  {
        throw new ServiceAccessException(this.service, e);
      }
    }

    /**
     * Test if the defined object (folder or document) is scheduled to create
     * in the future.
     */
    protected boolean isObjectCreateScheduled(Uri uri)  {
      return this.toBeCreated.contains(uri.toString());
    }

    /**
     * Checks, if the instance is from type 'TeamCenter_Folder' or
     * 'TeamCenter_RootFolder'.
     *
     * @param _instance instance to test
     * @return <i>true</i> if instance is from type 'TeamCenter_Folder' or
     *         'TeamCenter_RootFolder'
     */
    private boolean isFolder(Instance _instance)  {
      boolean isFolder = false;
      if (_instance.getType().getName().equals("TeamCenter_Folder")
          || _instance.getType().getName().equals("TeamCenter_RootFolder"))  {

        isFolder = true;
      }
      return isFolder;
    }

private Instance getFolderInstance(org.efaps.db.Context _context, Uri _uri) throws Exception  {
  String[] path = _uri.toString().split("/");
  Instance instance = this.uriCache.get(_uri.toString());

if (instance==null)  {
  if (_uri.toString().equals("/files") || _uri.toString().equals("/"))  {
  } else if (_uri.getParentUri().toString().equals("/files"))  {

    org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
    query.setQueryTypes(_context, "TeamCenter_RootFolder");
    query.addWhereExprEqValue(_context, "Name", path[2]);
    query.addSelect(_context, "OID");
    query.execute(_context);
// TODO: was passiert wenn nicht gefunden?
    query.next();
    instance = new Instance(_context, query.get(_context,"OID").toString());
    query.close();
  } else  {
    Instance parentInstance = getFolderInstance(_context, _uri.getParentUri());

    org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
    query.setQueryTypes(_context, "TeamCenter_Folder");
    query.addWhereExprEqValue(_context, "Name", path[path.length-1]);
    query.addWhereExprEqValue(_context, "ParentFolder", ""+parentInstance.getId());
    query.addSelect(_context, "OID");
    query.execute(_context);
    if (query.next())  {
      instance = new Instance(_context, query.get(_context,"OID").toString());
    }
    query.close();

    if (instance==null)  {
      query = new org.efaps.db.SearchQuery();
      query.setQueryTypes(_context, "TeamCenter_Document2Folder");
      query.addWhereExprEqValue(_context, "Folder", ""+parentInstance.getId());
      query.addSelect(_context, "Document.OID");
      query.addSelect(_context, "Document.FileName");
      query.execute(_context);
      while (query.next())  {
        String docName = query.get(_context, "Document.FileName").toString();
        if (path[path.length-1].equals(docName))  {
          instance = new Instance(_context, query.get(_context,"Document.OID").toString());
        } else  {
          this.uriCache.put(_uri.getParentUri().toString()+"/"+docName,
              new Instance(_context, query.get(_context,"Document.OID").toString()));
        }
      }
      query.close();
    }
/*
org.efaps.db.SearchQuery query = new org.efaps.db.SearchQuery();
query.setQueryTypes(_context, "TeamCenter_Uri");
query.setExpandChildTypes(true);
query.addWhereExprEqValue(_context, "UriName", _uri.toString().substring(6));
query.addSelect(_context, "OID");
query.execute(_context);
if (query.next())  {
  instance = new Instance(_context, query.get(_context,"OID").toString());
}
query.close();
*/
  }
//System.out.println("- getFolderInstance.ret="+instance+" for _uri="+_uri);
  if (instance!=null)  {
    this.uriCache.put(_uri.toString(), instance);
  }
}
  return instance;
}

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Cache to store found instances from the database (that they must not
     * retrieved again within one transaction!)
     */
    private Map<String,Instance> uriCache = new HashMap<String,Instance>();
    private Map<String,InstanceProperty> uriPropertyCache = new HashMap<String,InstanceProperty>();

    class InstanceProperty  {
      InstanceProperty(Instance _instance, long _fileLength, Date _created, Date _modified)  {
        this.instance = _instance;
        this.fileLength = _fileLength;
        this.created = _created;
        this.modified = _modified;
      }

      InstanceProperty(Instance _instance, Date _created, Date _modified)  {
        this.instance = _instance;
        this.created = _created;
        this.modified = _modified;
        this.isFolder = true;
      }

      Instance instance = null;
      long fileLength = 0;
      Date created = null;
      Date modified = null;
      boolean isFolder = false;
    }



  }


// sequence store

  /**
   * Checks if this store instance actually supports sequences. It may seem clear
   * this store supports sequences as it implements this interface, but a request to the
   * underlying persistence store might be needed to dynamically find out.
   *
   * @return <code>true</code> if the store supports sequences, <code>false</code> otherwise
   */
  public boolean isSequenceSupported() {
    return false;
  }

  /**
   * Checks if the sequence already exists.
   *
   * @param sequenceName the name of the sequence you want to check
   * @return <code>true</code> if the sequence already exists, <code>false</code> otherwise
   * @throws ServiceAccessException if anything goes wrong while accessing the sequence
   */
  public boolean sequenceExists(String sequenceName) throws ServiceAccessException {
    return false;
  }

  /**
   * Creates a sequence if it does not already exist.
   *
   * @param sequenceName the name of the sequence you want to create
   * @return <code>true</code> if the sequence has been created, <code>false</code> if it already existed
   * @throws ServiceAccessException if anything goes wrong while accessing the sequence
   */
  public boolean createSequence(String sequenceName) throws ServiceAccessException  {
    return false;
  }

  /**
   * Gets the next value of the sequence. Note that the sequence may not deliver consecutive
   * or continuous values. The only thing that is assured is the value will be unique
   * in the scope of the sequence, i.e. this method will never return the
   * same value for the same sequence. A sequence of valid values <em>might</em> be
   * <pre>1,2,3,4,5,...</pre>, but it might just as well be
   * <pre>10,787875845,1,2,434,...</pre>.
   * However, it may not be
   * <pre>1,2,1,3,...</pre>.
   * as a sequence must never return the same value twice or more times.
   *
   * @param sequenceNamethe name of the sequence you want the next value for
   * @return the next value of the sequence
   * @throws ServiceAccessException if anything goes wrong while accessing the sequence
   */
  public long nextSequenceValue(String sequenceName) throws ServiceAccessException {
    return 0;
  }


// Store interface

    /**
     * Returns true if binding is supported an enabled for this store
     */
    public boolean useBinding()  {
return true;
    }


    /**
     * Acquires an exclusive access lock to a resource. This lock is transient, i.e. it will
     * automatically be released when your transaction terminates.
     *
     * @param uri the URI of the resource you want to have exclusive access to
     * @throws ServiceAccessException thrown if anything goes wrong, including the lock can not be acquired
     */
    public void exclusiveTransientLock(String uri) throws ServiceAccessException  {
    }

    /**
     * Sets the sequence store associated with this store.
     */
    public void setSequenceStore(SequenceStore sequenceStore)  {
    }

    /**
     * Set the contentIndex store associated with this store.
     */
    public void setContentIndexer(IndexStore contentStore)  {
    }

    /**
     * Set the node store associated with this store.
     */
    public void setNodeStore(NodeStore nodeStore)  {
    }

    /**
     * Set the security store associated with this store.
     */
    public void setSecurityStore(SecurityStore securityStore) {
    }

    /**
     * Set the lock store associated with this store.
     */
    public void setLockStore(LockStore lockStore)  {
    }

    /**
     * Set the revision descriptors store associated with this store.
     */
    public void setRevisionDescriptorsStore(RevisionDescriptorsStore revisionDescriptorsStore)  {
    }


    /**
     * Set the revision descriptor store associated with this store.
     */
    public void setRevisionDescriptorStore(RevisionDescriptorStore revisionDescriptorStore)  {
    }


    /**
     * Set the content store associated with this store.
     */
    public void setContentStore(ContentStore contentStore)  {
    }

    /**
     * Set the descriptorIndex store associated with this store.
     */
    public void setPropertiesIndexer(IndexStore contentStore) {
    }

    /**
     * Get parameter value for specified key
     *
     * @param    key                 an Object
     * @return   an Object
     */
    public Object getParameter( Object key )  {
      return this.parameters.get(key);
    }

private String name = null;
    /**
     * Return the name of the store as specified in domain.xml.
     */
    public String getName()  {
      return this.name;
    }

    /**
     * Set the name of the store as specified in domain.xml.
     */
    public void setName(String _name)  {
      this.name = _name;
    }






  /////////////////////////////////////////////////////////////////////////////

  /**
   * Enlist the resource manager in the current transaction.
   */
  protected void enlist() throws ServiceAccessException {
System.out.println("ööööööööööööööööööööööööööööööööööööööööö enlist");
    // Note: No exception is thrown
    boolean enlisted = false;
    // FIXME : Enhance retries.
    int nbTry = 0;
    while ((!enlisted) && (nbTry++ < 20)) {
      Transaction transaction = null;
      try {
        transaction = namespace.getTransactionManager().getTransaction();
      } catch (Exception e) {
      }
      if (transaction == null) {
        getLogger().log("WARNING: No active transaction", Logger.WARNING);
        return;
      }
      try {
        enlisted = transaction.enlistResource(this);
      } catch (Exception e) {
        // Something went wrong.
        setRollbackOnly();
        throw new ServiceAccessException(this, e);
      }
      if (!enlisted) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          // Then go on.
        }
      }
    }
    if (!enlisted) {
      String exMessage = Messages.format
          (AbstractStore.class.getName() + ".enlistFail", this);
      setRollbackOnly();
      throw new ServiceAccessException(this, exMessage);
    }
    if (getLogger().isEnabled(LOG_CHANNEL, Logger.DEBUG)) {
      String logMessage = Messages.format
          (AbstractStore.class.getName() + ".enlist", this);
      getLogger().log(logMessage, LOG_CHANNEL, Logger.DEBUG);
    }
  }

  /**
   * Delist (suspend) the resource manager in the current transaction.
   */
  protected void delist(boolean success)
      throws ServiceAccessException {
System.out.println("ööööööööööööööööööööööööööööööööööööööööö delist="+success);
      try {
        Transaction transaction = namespace.getTransactionManager().getTransaction();
        if (transaction == null)  {
          return;
        }
//        if (success) {
          transaction.delistResource(this, TMSUSPEND);
          if (getLogger().isEnabled(LOG_CHANNEL, Logger.DEBUG)) {
              String logMessage = Messages.format
                  (AbstractStore.class.getName() + ".delist", this);
              getLogger().log(logMessage, LOG_CHANNEL, Logger.DEBUG);
          }
//        } else {
//          transaction.delistResource(this, TMFAIL);
//          String logMessage = Messages.format
//              (AbstractStore.class.getName() + ".delistFail", this);
//          getLogger().log(logMessage, LOG_CHANNEL, Logger.DEBUG);
//        }
      } catch (Exception e) {
          // Something went wrong.
          throw new ServiceAccessException(this, e);
      }
  }

  /**
   * Mark transaction as rollback in case of enlistment failure.
   */
  protected void setRollbackOnly() {
    try {
      Transaction transaction = namespace.getTransactionManager().getTransaction();
      if (transaction == null)  {
        return;
      }
      transaction.setRollbackOnly();
    } catch (Exception e) {
    }
  }











//public abstract boolean isSameRM(javax.transaction.xa.XAResource)   throws javax.transaction.xa.XAException;

//public abstract javax.transaction.xa.Xid[] recover(int)   throws javax.transaction.xa.XAException;

//protected abstract org.apache.commons.transaction.util.LoggerFacade getLoggerFacade();

//protected abstract boolean includeBranchInXid();


    public synchronized void commit(Xid _xid, boolean onePhase) throws XAException {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö commit.xid="+_xid);
super.commit(_xid,onePhase);
    }
    public synchronized void rollback(Xid _xid) throws XAException {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö rollback.xid="+_xid);
super.rollback(_xid);
    }

    public synchronized int prepare(Xid _xid) throws XAException {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö prepare.xid="+_xid);
return super.prepare(_xid);
    }


public void forget(javax.transaction.xa.Xid _xid)   throws javax.transaction.xa.XAException {
  super.forget(_xid);
}
//public void commit(javax.transaction.xa.Xid, boolean)   throws javax.transaction.xa.XAException;
//public void rollback(javax.transaction.xa.Xid)   throws javax.transaction.xa.XAException;
//public int prepare(javax.transaction.xa.Xid)   throws javax.transaction.xa.XAException;
//public void end(javax.transaction.xa.Xid, int)   throws javax.transaction.xa.XAException;
//public void start(javax.transaction.xa.Xid, int)   throws javax.transaction.xa.XAException;
//protected abstract org.apache.commons.transaction.util.xa.TransactionalResource createTransactionResource(javax.transaction.xa.Xid)   throws java.lang.Exception;
//protected org.apache.commons.transaction.util.xa.TransactionalResource getCurrentlyActiveTransactionalResource();
//protected void setCurrentlyActiveTransactionalResource(org.apache.commons.transaction.util.xa.TransactionalResource);
protected org.apache.commons.transaction.util.xa.TransactionalResource getTransactionalResource(javax.transaction.xa.Xid _xid) {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö getTransactionalResource.xid="+_xid);
return super.getTransactionalResource(_xid);
}

protected org.apache.commons.transaction.util.xa.TransactionalResource getActiveTransactionalResource(javax.transaction.xa.Xid _xid)  {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö getActiveTransactionalResource.xid="+_xid);
return super.getActiveTransactionalResource(_xid);
}

//protected org.apache.commons.transaction.util.xa.TransactionalResource getSuspendedTransactionalResource(javax.transaction.xa.Xid);

protected void addAcitveTransactionalResource(javax.transaction.xa.Xid _xid, org.apache.commons.transaction.util.xa.TransactionalResource _tr)  {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö addAcitveTransactionalResource.xid="+_xid);
super.addAcitveTransactionalResource(_xid,_tr);
}

//protected void addSuspendedTransactionalResource(javax.transaction.xa.Xid _xid, org.apache.commons.transaction.util.xa.TransactionalResource)  {
//}

protected void removeActiveTransactionalResource(javax.transaction.xa.Xid _xid)  {
System.out.println("ööööööööööööööööööööööööööööööööööööööööööööööööööööö removeActiveTransactionalResource.xid="+_xid);
  super.removeActiveTransactionalResource(_xid);
}
//protected void removeSuspendedTransactionalResource(javax.transaction.xa.Xid);


}
