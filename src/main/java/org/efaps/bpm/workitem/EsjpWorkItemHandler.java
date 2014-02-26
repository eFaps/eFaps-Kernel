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
import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EsjpWorkItemHandler
    implements WorkItemHandler
{
    /**
     * Name for the parameter containing the classname.
     */
    public static final String PARAMETERNAME_ESJP = "esjp";

    /**
     * Name for the parameter containing the methodname.
     */
    public static final String PARAMETERNAME_METHOD = "method";

    /**
     * Name for the parameter containing the error signal.
     */
    public static final String PARAMETERNAME_ERRORSIGNAL = "errorSignal";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EsjpWorkItemHandler.class);

    /**
     * The given work item should be executed.
     *
     * @param _workItem the work item that should be executed
     * @param _manager the manager that requested the work item to be executed
     */
    @SuppressWarnings("unchecked")
    @Override
    public void executeWorkItem(final WorkItem _workItem,
                                final WorkItemManager _manager)

    {
        final Map<String, Object> results = new HashMap<String, Object>();
        final String esjpName = (String) _workItem.getParameter(EsjpWorkItemHandler.PARAMETERNAME_ESJP);
        if (esjpName != null) {
            final String methodName = (String) _workItem.getParameter(EsjpWorkItemHandler.PARAMETERNAME_METHOD);
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.BPM_VALUES, _workItem.getParameters());
            try {
                final Class<?> esjp = Class.forName(esjpName.trim(), true,
                                EFapsClassLoader.getInstance());
                final Method method = esjp.getMethod(methodName.trim(), new Class[] { Parameter.class });
                final Return ret = (Return) method.invoke(esjp.newInstance(), parameter);
                if (ret != null) {
                    final Object values = ret.get(ReturnValues.VALUES);
                    if (values instanceof Map) {
                        results.putAll((Map<? extends String, ? extends Object>) values);
                    }
                }
            } catch (final ClassNotFoundException e) {
                EsjpWorkItemHandler.LOG.error("Class could not be found.", e);
            } catch (final InstantiationException e) {
                EsjpWorkItemHandler.LOG.error("Class could not be instantiation.", e);
            } catch (final IllegalAccessException e) {
                EsjpWorkItemHandler.LOG.error("Class could not be accessed.", e);
            } catch (final IllegalArgumentException e) {
                EsjpWorkItemHandler.LOG.error("Illegal Argument.", e);
            } catch (final InvocationTargetException e) {
                EsjpWorkItemHandler.LOG.error("Invocation Target.", e);
                if (e.getCause() instanceof WorkItemException) {
                    throw (WorkItemException) e.getCause();
                }
            } catch (final SecurityException e) {
                EsjpWorkItemHandler.LOG.error("Security.", e);
            } catch (final NoSuchMethodException e) {
                EsjpWorkItemHandler.LOG.error("Method could not be found.", e);
            }
        }
        _manager.completeWorkItem(_workItem.getId(), results);
    }

    /**
     * The given work item should be aborted.
     *
     * @param _workItem the work item that should be aborted
     * @param _manager the manager that requested the work item to be aborted
     */
    @Override
    public void abortWorkItem(final WorkItem _workItem,
                              final WorkItemManager _manager)
    {
        _manager.abortWorkItem(_workItem.getId());
    }

}
