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

package org.efaps.db;

import java.util.List;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.StatusLinkType;
import org.efaps.admin.lifecycle.Policy;
import org.efaps.admin.lifecycle.Status;
import org.efaps.util.EFapsException;

/**
 *
 */
public class LifeCycleAction extends Update {

  public final static int ACTION_PROMOTE = 130;
  public final static int ACTION_DEMOTE  = 131;


  /**
   * The constructor creates a new instance of Promote.
   *
   *
   */
  public LifeCycleAction(Context _context, Instance _instance) throws EFapsException  {
    super(_context, _instance);
  }


  public void execute(final int _action) throws Exception {
    Context context = Context.getThreadContext();
    try  {
      context.getConnection().setAutoCommit(false);

      Status status = getCurrentStatus(context, true);

      if (_action==ACTION_PROMOTE)  {
        if (!status.checkAccess(context, ACTION_PROMOTE))  {
throw new EFapsException(getClass(), "NoPromoteRights");
        }
        status = getNextStatus(status);
      } else if (_action==ACTION_DEMOTE)  {
        if (!status.checkAccess(context, ACTION_DEMOTE))  {
throw new EFapsException(getClass(), "NoDemoteRights");
        }
        status = getPrevStatus(status);
      }
      add(context, getStatusAttribute(), ""+status.getId());
      super.execute();
      context.getConnection().commit();

    } catch (EFapsException e)  {
      try  {context.getConnection().rollback();} catch (Exception e1)  {}
      throw e;
    } catch (Exception e)  {
      try  {context.getConnection().rollback();} catch (Exception e1)  {}
throw new EFapsException(getClass(), "execute.Exception", e);
//e.printStackTrace();
    } finally  {
      try  {
        context.getConnection().setAutoCommit(true);
      } catch (Exception e)  {
throw new EFapsException(getClass(), "execute.AutoCommitException", e);
//e.printStackTrace();
      }
    }




  }

  /**
   * Gives for the given status the next state return used for demote.
   *
   * @param _curStatus  status for which to get next status
   */
  protected Status getNextStatus(Status _curStatus) throws Exception  {
    Policy policy = _curStatus.getPolicy();
    List<Status> statusList = policy.getStatus();
    int next = -1;
    for (int i=0; i<statusList.size(); i++)  {
      Status polStatus = (Status)statusList.get(i);
      if (polStatus.getId()==_curStatus.getId())  {
        next = i+1;
        break;
      }
    }
    if (next == statusList.size() || next == -1)  {
throw new Exception("promote not possible!");
    }
    return (Status)statusList.get(next);
  }

  /**
   * Gives for the given status the previous state return used for demote.
   *
   * @param _curStatus  status for which to get previous status
   */
  protected Status getPrevStatus(Status _curStatus) throws Exception  {
    Policy policy = _curStatus.getPolicy();
    List<Status> statusList = policy.getStatus();
    int prev = -1;
    for (int i=0; i<statusList.size(); i++)  {
      Status polStatus = (Status)statusList.get(i);
      if (polStatus.getId()==_curStatus.getId())  {
        prev = i-1;
        break;
      }
    }
    if (prev < 0)  {
throw new Exception("demote not possible!");
    }
    return (Status)statusList.get(prev);
  }

  /**
   * The instance method returns for the current instance object the status
   * instance object.
   *
   * @param _context    context for this request
   * @param _update     <i>true</i> means, the selction is with
   *                    <b>for update</b>, <i>false</i> means, the seleection
   *                    is without <b>for update</b>.
   * @return current status
   * @see #getStatusAttribute
   */
  protected Status getCurrentStatus(Context _context, boolean _update) throws Exception  {
    Attribute statusAttr = getStatusAttribute();

    SearchQuery query = new SearchQuery();
    query.setObject(_context, getInstance());
    query.add(statusAttr);
    query.execute(_context);
    if (!query.next())  {
throw new Exception("object does not exists");
    }
    StatusLinkType statusValue = (StatusLinkType)query.get(_context, statusAttr);
    return statusValue.getStatus(_context);
  }

  /**
   * The instance method returns the attribute for the status of the current
   * instance object.
   *
   * @param _instance current instance
   * @return status attribute for the current instance object
   * @see #updateNewStatus
   * @see #getCurrentStatus
   */
  protected Attribute getStatusAttribute() throws Exception  {
    Set attrs = getInstance().getType().getAttributes(StatusLinkType.class);
    if (attrs.isEmpty())  {
throw new Exception("no status attribute defined");
    }
    if (attrs.size()>1)  {
throw new Exception("to much status attributes defined");
    }
    return (Attribute)attrs.toArray()[0];
  }
}