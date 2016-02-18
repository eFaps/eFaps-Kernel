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

package org.efaps.db.print;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.beans.ValueList;
import org.efaps.beans.ValueList.Token;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * Class is used as a wraper for a series of OneSelects as part of one phrase.
 *
 * @author The eFaps Team
 *
 */
public class Phrase
{

    /**
     * Key for this Phrase.
     */
    private final String key;

    /**
     * Phrase statement for this Phrase.
     */
    private final String phraseStmt;

    /**
     * Mapping of Select statements to OneSelect.
     */
    private final Map<String, OneSelect> selectStmt2OneSelect = new HashMap<String, OneSelect>();

    /**
     * ValueList to access the parser.
     */
    private final ValueList valueList;

    /**
     * @param _key Key for this Phrase
     * @param _phraseStmt Phrase statement for this Phrase
     * @param _valueList ValueList to access the parser.
     */
    public Phrase(final String _key,
                  final String _phraseStmt,
                  final ValueList _valueList)
    {
        this.key = _key;
        this.phraseStmt = _phraseStmt;
        this.valueList = _valueList;
    }

    /**
     * Method to get the parsed value for this phrase.
     *
     * @param _instance Instance the phrase is build on
     * @return parsed value
     * @throws EFapsException on error
     */
    public String getPhraseValue(final Instance _instance)
        throws EFapsException
    {
        final StringBuilder buf = new StringBuilder();

        for (final Token token : this.valueList.getTokens()) {
            switch (token.getType()) {
                case EXPRESSION:
                    final OneSelect oneselect = this.selectStmt2OneSelect.get(token.getValue());
                    final Object value = oneselect.getObject();
                    if (oneselect.getAttribute() != null) {
                        buf.append(new FieldValue(null, oneselect.getAttribute(), value, _instance, null)
                                        .getStringValue(TargetMode.VIEW));
                    } else if (value != null) {
                        buf.append(value);
                    }
                    break;
                case TEXT:
                    buf.append(token.getValue());
                    break;
                default:
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * Add a oneselect to this Phrase.
     *
     * @param _oneselect OneSelect to add
     */
    public void addSelect(final OneSelect _oneselect)
    {
        this.selectStmt2OneSelect.put(_oneselect.getSelectStmt(), _oneselect);
    }

    /**
     * Getter method for instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * Getter method for instance variable {@link #phraseStmt}.
     *
     * @return value of instance variable {@link #phraseStmt}
     */
    public String getPhraseStmt()
    {
        return this.phraseStmt;
    }
}
