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

package org.efaps.bpm.process;

import java.util.List;

import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ProcessAdmin
{
    /**
     * @return list of all active processes for the given processId.
     */
    public List<ProcessInstanceLog> getActiveProcessInstances(final String _processId)
    {
        return JPAProcessInstanceDbLog.findActiveProcessInstances(_processId);
    }

    /**
     * @return list of all NodeInstance logs for the given processInstanceId.
     */
    public List<NodeInstanceLog> getNodeInstances(final Long _processInstanceId)
    {
        return JPAProcessInstanceDbLog.findNodeInstances(_processInstanceId);
    }

    /**
     * @return list of all NodeInstance logs for the given processInstanceId and NodeId.
     */
    public List<NodeInstanceLog> getNodeInstances(final Long _processInstanceId,
                                                  final String _nodeId)
    {
        return JPAProcessInstanceDbLog.findNodeInstances(_processInstanceId, _nodeId);
    }

    /**
     * @return processes for the given processId.
     */
    public ProcessInstanceLog getProcessInstance(final Long _processInstanceId)
    {
        return JPAProcessInstanceDbLog.findProcessInstance(_processInstanceId);
    }

    /**
     * @return list of all found process Instances.
     */
    public List<ProcessInstanceLog> getProcessInstances()
    {
        return JPAProcessInstanceDbLog.findProcessInstances();
    }

    /**
     * @return list of all processes for the given processId.
     */
    public List<ProcessInstanceLog> getProcessInstances(final String _processId)
    {
        return JPAProcessInstanceDbLog.findProcessInstances(_processId);
    }

    /**
     * @return list of all variable logs for the given processinstanceId.
     */
    public List<VariableInstanceLog> getVariableInstances(final Long _processInstanceId)
    {
        return JPAProcessInstanceDbLog.findVariableInstances(_processInstanceId);
    }

    /**
     * @return list of all variable logs for the given processinstanceId and variable id.
     */
    public List<VariableInstanceLog> getVariableInstances(final Long _processInstanceId,
                                                          final String _variableId)
    {
        return JPAProcessInstanceDbLog.findVariableInstances(_processInstanceId, _variableId);
    }
}
