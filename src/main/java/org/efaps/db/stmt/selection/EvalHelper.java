/*
 * Copyright 2003 - 2020 The eFaps Team
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
package org.efaps.db.stmt.selection;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.common.MsgPhrase;
import org.efaps.eql.builder.Print;

public class EvalHelper
{

    private final Map<String, MsgPhrase> msgPhrases = new HashMap<>();

    public void addMsgPhrase(final MsgPhrase _msgPhrase)
    {
        msgPhrases.put(Print.getMsgPhraseAlias(_msgPhrase.getId()), _msgPhrase);
    }

    public void setMsgPhraseAlias(final Long _msgPhraseId, final String _alias)
    {
        final String key = Print.getMsgPhraseAlias(_msgPhraseId);
        if (msgPhrases.containsKey(key)) {
            msgPhrases.put(_alias, msgPhrases.get(key));
            msgPhrases.remove(key);
        }
    }

    public Map<String, MsgPhrase> getMsgPhrases()
    {
        return msgPhrases;
    }
}
