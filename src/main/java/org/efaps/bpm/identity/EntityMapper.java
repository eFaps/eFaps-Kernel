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


package org.efaps.bpm.identity;

import java.util.UUID;

import org.efaps.admin.KernelSettings;


/**
 * Class used to map the eFaps Administrators to the jBPM Adminstrators.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class EntityMapper
{
    /**
     * Helper Class.
     */
    private EntityMapper()
    {
    }

    /**
     * @param _uuid UUID of the User the mapping is wanted for
     * @return String for jBPM entity management
     */
    public static String getUserId(final UUID _uuid)
    {
        String ret;
        if (KernelSettings.USER_PERSON_ADMINISTRATOR.equals(_uuid)) {
            ret = "Administrator";
        } else  if (KernelSettings.USER_ROLE_ADMINISTRATION.equals(_uuid)) {
            ret = "Administration";
        } else {
            ret = _uuid.toString();
        }
        return ret;
    }

    /**
     * @param _targetEntityId id of a userid
     * @return String for jBPM entity management
     */
    public static String getEntityId(final String _targetEntityId)
    {
        String ret;
        if (KernelSettings.USER_PERSON_ADMINISTRATOR.toString().equals(_targetEntityId)) {
            ret = "Administrator";
        } else  if (KernelSettings.USER_ROLE_ADMINISTRATION.toString().equals(_targetEntityId)) {
            ret = "Administration";
        } else {
            ret = _targetEntityId;
        }
        return ret;
    }

    /**
     * @param _userId id of a user as managed by jBPM
     * @return UUID for eFaps UserManagement
     */
    public static UUID getUUID(final String _userId)
    {
        UUID ret;
        if ("Administrator".equals(_userId)) {
            ret = KernelSettings.USER_PERSON_ADMINISTRATOR;
        } else if ("Administration".equals(_userId)) {
            ret = KernelSettings.USER_ROLE_ADMINISTRATION;
        } else {
            ret = UUID.fromString(_userId);
        }
        return ret;
    }
}
