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


package org.efaps.update;

import java.util.HashMap;
import java.util.Map;


/**
 * Represent an Install or Update Profile.
 * Implemented to have the change to make this
 * thing bigger in the future.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Profile
{
    /**
     * Default Profile.
     */
    private static final Profile DEFAULT = new Profile("eFaps");

    /**
     * The mapping of all names to their Profiles.
     */
    private static final Map<String, Profile> NAME2PROFILE = new  HashMap<String, Profile>();

    /**
     * Name of the Profile.
     */
    private final String name;

    /**
     * @param _name the name of the Profile
     */
    private Profile(final String _name)
    {
        this.name = _name;
    }

    /**
     * Getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object _object)
    {
        boolean ret;
        if (_object instanceof Profile) {
            ret = this.name.equals(((Profile) _object).getName());
        } else {
            ret = false;
        }
        return ret;
    }

    /**
     * @return default Profile
     */
    public static Profile getDefaultProfile()
    {
        return Profile.DEFAULT;
    }

    /**
     * @param _name name of a profile
     * @return the profile for the given name
     */
    public static Profile getProfile(final String _name)
    {
        final Profile ret;
        if (Profile.NAME2PROFILE.containsKey(_name)) {
            ret = Profile.NAME2PROFILE.get(_name);
        } else {
            ret = new Profile(_name);
        }
        return ret;
    }
}
