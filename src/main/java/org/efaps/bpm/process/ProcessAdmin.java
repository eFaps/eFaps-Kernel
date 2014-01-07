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

import org.jbpm.process.audit.JPAAuditLogService;
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
     * Underlying audit service used for administration.
     */
    private final JPAAuditLogService jpaAuditLogService;

    /**
     * @param _jpaAuditLogService audit service to be used
     */
    public ProcessAdmin(final JPAAuditLogService _jpaAuditLogService)
    {
        this.jpaAuditLogService = _jpaAuditLogService;
    }

    /**
     * @param _processId processId
     * @return list of all active processes for the given processId.
     */
    public List<ProcessInstanceLog> getActiveProcessInstances(final String _processId)
    {
        return this.jpaAuditLogService.findActiveProcessInstances(_processId);
    }

    /**
     * @param _processInstanceId processInstanceId
     * @return list of all NodeInstance logs for the given processInstanceId.
     */
    public List<NodeInstanceLog> getNodeInstances(final Long _processInstanceId)
    {
        return this.jpaAuditLogService.findNodeInstances(_processInstanceId);
    }

    /**
     * @param _processInstanceId processInstanceId
     * @param _nodeId NodeId
     * @return list of all NodeInstance logs for the given processInstanceId and NodeId.
     */
    public List<NodeInstanceLog> getNodeInstances(final Long _processInstanceId,
                                                  final String _nodeId)
    {
        return this.jpaAuditLogService.findNodeInstances(_processInstanceId, _nodeId);
    }

    /**
     * @param _processInstanceId processInstanceId
     * @return processes for the given processId.
     */
    public ProcessInstanceLog getProcessInstance(final Long _processInstanceId)
    {
        return this.jpaAuditLogService .findProcessInstance(_processInstanceId);
    }

    /**
     * @return list of all found process Instances.
     */
    public List<ProcessInstanceLog> getProcessInstances()
    {
        return this.jpaAuditLogService.findProcessInstances();
    }

    /**
     * @param _processId processId
     * @return list of all processes for the given processId.
     */
    public List<ProcessInstanceLog> getProcessInstances(final String _processId)
    {
        return this.jpaAuditLogService.findProcessInstances(_processId);
    }

    /**
     * @param _processInstanceId processInstanceId
     * @return list of all variable logs for the given processinstanceId.
     */
    public List<VariableInstanceLog> getVariableInstances(final Long _processInstanceId)
    {
        return this.jpaAuditLogService.findVariableInstances(_processInstanceId);
    }

    /**
     * @param _processInstanceId processInstanceId
     * @param _variableId variable id
     * @return list of all variable logs for the given processinstanceId and variable id.
     */
    public List<VariableInstanceLog> getVariableInstances(final Long _processInstanceId,
                                                          final String _variableId)
    {
        return this.jpaAuditLogService.findVariableInstances(_processInstanceId, _variableId);
    }
}
