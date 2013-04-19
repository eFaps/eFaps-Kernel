/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.bpm.workitem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EsjpWorkItemHandler
    implements WorkItemHandler
{

    public final static String PARAMETERNAME_ESJP = "esjp";

    public final static String PARAMETERNAME_METHOD = "method";

    /*
     * (non-Javadoc)
     * @see
     * org.drools.runtime.process.WorkItemHandler#executeWorkItem(org.drools
     * .runtime.process.WorkItem, org.drools.runtime.process.WorkItemManager)
     */

    @SuppressWarnings("unchecked")
    @Override
    public void executeWorkItem(final WorkItem _workItem,
                                final WorkItemManager _manager)
    {

        final String esjpName = (String) _workItem.getParameter(EsjpWorkItemHandler.PARAMETERNAME_ESJP);
        if (esjpName != null) {
            final String methodName = (String) _workItem.getParameter(EsjpWorkItemHandler.PARAMETERNAME_METHOD);
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.BPM_VALUES, _workItem.getParameters());
            try {
                final Class<?> esjp = Class.forName(esjpName);
                final Method method = esjp.getMethod(methodName, new Class[] { Parameter.class });
                final Return ret = (Return) method.invoke(esjp.newInstance(), parameter);
                if (ret != null) {
                    final Object values = ret.get(ReturnValues.VALUES);
                    if (values instanceof Map) {
                        _workItem.getResults().putAll((Map<? extends String, ? extends Object>) values);
                    }
                }
            } catch (final ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
     * (non-Javadoc)
     * @see
     * org.drools.runtime.process.WorkItemHandler#abortWorkItem(org.drools.runtime
     * .process.WorkItem, org.drools.runtime.process.WorkItemManager)
     */
    @Override
    public void abortWorkItem(final WorkItem _workItem,
                              final WorkItemManager _manager)
    {
        System.out.println("asdad");
    }

}
