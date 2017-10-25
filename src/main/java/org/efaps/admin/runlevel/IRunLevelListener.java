/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.admin.runlevel;

import org.efaps.admin.program.esjp.IEsjpListener;
import org.efaps.util.EFapsException;

/**
 * The listener interface for receiving IRunLevel events.
 * The class that is interested in processing a IRunLevel
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addIRunLevelListener</code> method. When
 * the IRunLevel event occurs, that object's appropriate
 * method is invoked.
 *
 * @see IRunLevelEvent
 */
public interface IRunLevelListener
    extends IEsjpListener
{

    /**
     * On execute.
     * @param _levelName   name of the run level
     * @throws EFapsException the eFaps exception
     */
    void onExecute(String _levelName) throws EFapsException;
}
