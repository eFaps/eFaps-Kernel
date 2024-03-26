/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.stmt.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.efaps.admin.common.MsgPhrase;
import org.efaps.eql.builder.Print;

public class EvalHelper
{

    private final Map<String, MsgPhrase> msgPhrases = new HashMap<>();
    private final List<PhraseEntry> phrases = new ArrayList<>();

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

    public void registerPhrase(final int _phraseIdx, final String _phrase)
    {
        final PhraseEntry entry = new PhraseEntry();
        phrases.add(entry);
        entry.setPhraseIdx(_phraseIdx);
        entry.setPhrase(_phrase);
    }

    public void setPhraseAlias(final Integer _phraseIdx, final String _alias)
    {
        final Optional<PhraseEntry> entryOpt = phrases.stream()
                        .filter(entry -> {
                            return entry.getPhraseIdx().equals(_phraseIdx);
                        }).findFirst();
        if (entryOpt.isPresent()) {
            entryOpt.get().setAlias(_alias);
        }
    }

    public List<PhraseEntry> getPhrases()
    {
        return phrases;
    }

    public static class PhraseEntry
    {

        private Integer phraseIdx;
        private String phrase;
        private String alias;

        public Integer getPhraseIdx()
        {
            return phraseIdx;
        }

        public void setPhraseIdx(final Integer phraseIdx)
        {
            this.phraseIdx = phraseIdx;
        }

        public String getPhrase()
        {
            return phrase;
        }

        public void setPhrase(final String phrase)
        {
            this.phrase = phrase;
        }

        public String getAlias()
        {
            return alias;
        }

        public void setAlias(final String alias)
        {
            this.alias = alias;
        }
    }

}
