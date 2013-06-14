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


package org.efaps.admin;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface KernelSettings
{
    /**
     * Boolean (true/false): activate the BPM process mechanism. Default: false;
     */
    String ACTIVATE_BPM = "org.efaps.kernel.ActivateBPM";

    /**
     * Boolean (true/false): activate the Groups Access Mechanism. Default: false
     */
    String ACTIVATE_GROUPS = "org.efaps.kernel.ActivateGroups";

    /**
     * Boolean (true/false): activate the BPM process mechanism.
     */
    String SHOW_DBPROPERTIES_KEY = "org.efaps.kernel.ShowDBPropertiesKey";

    /**
     * Boolean (true/false): activate the BPM process mechanism.
     */
    String REQUIRE_PERSON_UUID = "org.efaps.kernel.RequirePersonUUID";

    /**
     * Integer:
     * Maximum number of tries to login with the wrong Password into eFaps,
     * before the User is going to be deactivated. To deactivate this mechanism
     * set the value to 0.
     */
    String LOGIN_MAX_TRIES = "org.efaps.kernel.LoginMaximumTries";

    /**
     * Integer:
     * This attribute defines the time in minutes which must elapse after trying
     * n-times to login with the wrong password, before the user has again the
     * possibility to try to login. To deactivate this mechanism set the
     * value to 0.
     */
    String LOGIN_TIME_RETRY = "org.efaps.kernel.LoginTimeElapseBeforeRetry";




}
