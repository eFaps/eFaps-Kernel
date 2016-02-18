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

package org.efaps.bpm.workitem;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class WorkItemException
    extends RuntimeException
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _errorCode errorCode from BPMN
     */
    public WorkItemException(final String _errorCode)
    {
        super(_errorCode);
    }

    /**
     * @param _errorCode errorCode from BPMN
     * @param _cause cause
     */
    public WorkItemException(final String _errorCode,
                             final Throwable _cause)
    {
        super(_errorCode, _cause);
    }
}
