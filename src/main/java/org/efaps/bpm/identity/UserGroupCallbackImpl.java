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

import java.util.ArrayList;
import java.util.List;

import org.jbpm.task.identity.UserGroupCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UserGroupCallbackImpl
    implements UserGroupCallback
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserGroupCallbackImpl.class);

    /**
     * Resolves existence of user id.
     *
     * By default, jBPM registers a special user with userId "Administrator" as the
     * administrator of each task. You should therefore make sure that you always
     * define at least a user "Administrator" when registering the list of valid
     * users at the task service.
     *
     * @param _userId    the user id assigned to the task
     * @return true if userId exists, false otherwise.
     */
    @Override
    public boolean existsUser(final String _userId)
    {
        UserGroupCallbackImpl.LOG.info("checking for existence of User: '{}'", _userId);
        boolean ret = true;
        if ("Administrator".equals(_userId)) {
            ret = true;
        }
        UserGroupCallbackImpl.LOG.info("result for existence check for User: '{}' is: {}", _userId, ret);
        return ret;
    }

    /**
     * Resolves existence of group id.
     *
     * @param _groupId   the group id assigned to the task
     * @return true if groupId exists, false otherwise.
     */
    @Override
    public boolean existsGroup(final String _groupId)
    {
        UserGroupCallbackImpl.LOG.info("checking for existence of Group: {}", _groupId);
        return true;
    }

    /**
     * Returns list of group ids for specified user id.
     *
     * @param _userId    the user id assigned to the task
     * @param _groupIds  list of group ids assigned to the task
     * @param _allExistingGroupIds   list of all currently known group ids
     * @return List of group ids.
     */
    @Override
    public List<String> getGroupsForUser(final String _userId,
                                         final List<String> _groupIds,
                                         final List<String> _allExistingGroupIds)
    {
        UserGroupCallbackImpl.LOG.info(
                        "getting Groups for User: '{}'. Assigned Groups: {}. Currently known Groups: {}.", _userId,
                        _groupIds, _allExistingGroupIds);
        final List<String> ret = new ArrayList<String>();
        ret.add("sales");
        UserGroupCallbackImpl.LOG.info("found Groups for User: '{}': {}", _userId, ret);
        return ret;
    }

}