/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.bpm.runtime;

import java.util.HashMap;
import java.util.Map;

import org.efaps.bpm.workitem.EsjpWorkItemHandler;
import org.efaps.bpm.workitem.HumanTaskWorkItemHandler;
import org.efaps.bpm.workitem.SignallingHandlerWrapper;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.RuntimeEngineImpl;
import org.jbpm.services.task.wih.ExternalTaskEventListener;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.internal.runtime.manager.Disposable;
import org.kie.internal.runtime.manager.DisposeListener;
import org.kie.internal.task.api.EventService;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class RegisterableItemsFactoryImpl
    extends DefaultRegisterableItemsFactory
{

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected WorkItemHandler getHTWorkItemHandler(final RuntimeEngine _runtime)
    {
        final ExternalTaskEventListener listener = new ExternalTaskEventListener();

        final HumanTaskWorkItemHandler humanTaskHandler = new HumanTaskWorkItemHandler();
        humanTaskHandler.setRuntimeManager(((RuntimeEngineImpl) _runtime).getManager());

        if (_runtime.getTaskService() instanceof EventService) {
            ((EventService) _runtime.getTaskService()).registerTaskEventListener(listener);
        }

        if (_runtime instanceof Disposable) {
            ((Disposable) _runtime).addDisposeListener(new DisposeListener()
            {
                @Override
                public void onDispose(final RuntimeEngine _runtime)
                {
                    if (_runtime.getTaskService() instanceof EventService) {
                        ((EventService) _runtime.getTaskService()).clearTaskEventListeners();
                    }
                }
            });
        }
        return humanTaskHandler;
    }

    @Override
    public Map<String, WorkItemHandler> getWorkItemHandlers(final RuntimeEngine _runtime)
    {
        final Map<String, WorkItemHandler> ret = new HashMap<String, WorkItemHandler>();
        ret.put("ESJPNode", new SignallingHandlerWrapper(new EsjpWorkItemHandler()));
        ret.putAll(super.getWorkItemHandlers(_runtime));
        return ret;
    }
}
