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
package org.efaps.eql.builder;

import java.io.StringReader;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.common.MsgPhrase;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIMsgPhrase;
import org.efaps.ci.CIType;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.selection.EvalHelper;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.ISelect;
import org.efaps.eql2.ISelectElement;
import org.efaps.eql2.ISelection;
import org.efaps.eql2.bldr.AbstractPrintEQLBuilder;
import org.efaps.eql2.bldr.ISelectable;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Print.
 */
public class Print
    extends AbstractPrintEQLBuilder<Print>
{

    private static final Logger LOG = LoggerFactory.getLogger(Print.class);

    private EvalHelper helper;

    private Long currentMsgPhraseId;

    private int phraseCounter = 0;

    private EvalHelper getHelper()
    {
        if (helper == null) {
            helper = new EvalHelper();
        }
        return helper;
    }

    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    public PrintStmt stmt()
    {
        LOG.debug("Stmt: {}", getStmt().eqlStmt());
        return PrintStmt.get((IPrintStatement<?>) getStmt(), helper);
    }

    public PrintStmt execute()
        throws EFapsException
    {
        return stmt().execute();
    }

    public Evaluator evaluate()
        throws EFapsException
    {
        return stmt().evaluate();
    }

    @Override
    protected Print getThis()
    {
        return this;
    }

    @Override
    public Print select(final ISelectable... _selects)
    {
        for (final ISelectable select : _selects) {
            switch (select.getKey()) {
                case "CIAttribute":
                    attribute(((CIAttribute) select).name);
                    break;
                default:
                    super.select(select);
                    break;
            }
        }
        return getThis();
    }

    public Print attribute(final CIAttribute... _ciAttrs)
    {
        for (final CIAttribute ciAttr : _ciAttrs) {
            attribute(ciAttr.name);
            as(getCIAlias(ciAttr));
        }
        return getThis();
    }

    public Print attributeSet(final CIAttribute... _ciAttrs)
    {
        for (final CIAttribute ciAttr : _ciAttrs) {
            attributeSet(ciAttr.name);
            as(getCIAlias(ciAttr));
        }
        return getThis();
    }

    public Print linkto(final CIAttribute _ciAttr)
    {
        linkto(_ciAttr.name);
        return getThis();
    }

    public Print linkfrom(final CIAttribute _ciAttr)
    {
        linkfrom(_ciAttr.ciType.getType().getName(), _ciAttr.name);
        return getThis();
    }

    public Print orderBy(final CIAttribute _ciAttr)
    {
        super.orderBy(_ciAttr.name);
        return getThis();
    }

    public Print orderBy(final CIAttribute _ciAttr,
                         final boolean desc)
    {
        super.orderBy(getCIAlias(_ciAttr), desc);
        return getThis();
    }

    public Print msgPhrase(final CIMsgPhrase... _msgPhrase)
        throws EFapsException
    {
        return msgPhrase(Arrays.stream(_msgPhrase)
                        .map(ciMsgPhrase -> {
                            try {
                                return MsgPhrase.get(ciMsgPhrase.uuid);
                            } catch (final EFapsException e) {
                                LOG.error("Catched", e);
                            }
                            return null;
                        })
                        .toArray(MsgPhrase[]::new));
    }

    public Print msgPhrase(final MsgPhrase... _msgPhrase)
        throws EFapsException
    {
        return msgPhrase(null, _msgPhrase);
    }

    public Print msgPhrase(final CharSequence _baseSelect,
                           final MsgPhrase... _msgPhrase)
        throws EFapsException
    {
        final String baseSel;
        if (_baseSelect == null || _baseSelect.isEmpty()) {
            baseSel = "";
        } else {
            baseSel = _baseSelect.toString() + ".";
        }
        for (final MsgPhrase phrase : _msgPhrase) {
            int idx = 0;
            for (final String selectStmt : phrase.getArguments()) {
                select(baseSel + selectStmt);
                as(getMsgPhraseAlias(phrase.getId()) + "_" + idx);
                idx++;
            }
            getHelper().addMsgPhrase(phrase);
            currentMsgPhraseId = phrase.getId();
        }
        return this;
    }

    public Print phrase(final CharSequence _phrase) {
        try {
            phraseCounter++;
            final ValueList list = new ValueParser(new StringReader(_phrase.toString())).ExpressionString();
            getHelper().registerPhrase(phraseCounter, _phrase.toString());
            String baseSelect = "";
            // check if this is used in a subselect e.g. a linkto
            final var selection = ((IPrintStatement<?>) getStmt()).getSelection();
            final ISelect select = selection.getSelects(selection.getSelectsLength() - 1);
            if (ArrayUtils.isNotEmpty(select.getElements())) {
                final ISelectElement lastElement = select.getElements(select.getElementsLength() - 1);
                if (connectable(lastElement)) {
                    baseSelect = select.eqlStmt() + ".";
                    selection.getSelectsList().remove(selection.getSelectsLength() - 1);
                }
            }
            int idx = 0;
            for (final String expr : list.getExpressions()) {
                select(baseSelect + expr);
                as(getPhraseAlias(phraseCounter) + "_" + idx);
                idx++;
            }
        } catch (final ParseException e) {
            LOG.error("Catched", e);
        }
        return this;
    }

    @Override
    public Print as(final String _alias)
    {
        if (currentMsgPhraseId != null) {
            getHelper().setMsgPhraseAlias(currentMsgPhraseId, _alias);
            currentMsgPhraseId = null;
        } else {
            final ISelection selection = ((IPrintStatement<?>) getStmt()).getSelection();
            final ISelect select = selection.getSelects(selection.getSelectsLength() - 1);
            if (!StringUtils.isEmpty(select.getAlias()) && select.getAlias()
                            .startsWith(getPhraseAlias(phraseCounter) + "_")) {
                getHelper().setPhraseAlias(phraseCounter, _alias);
            } else {
                super.as(_alias);
            }
        }
        return this;
    }

    @Override
    public Query query(final String... _types)
    {
        return (Query) super.query(_types);
    }

    public Query query(final CIType... _types)
    {
        return query(Arrays.stream(_types)
                        .map(ciType -> String.valueOf(ciType.uuid))
                        .toArray(String[]::new));
    }

    public Print clazz(final CIType _ciType)
    {
        return clazz(String.valueOf(_ciType.uuid));
    }

    public static String getCIAlias(final CIAttribute _ciAttr)
    {
        return "CIALIAS_" + _ciAttr.name;
    }

    public static String getMsgPhraseAlias(final Long _id)
    {
        return "MSGPHRASE_" + _id;
    }

    public static String getPhraseAlias(final int _idx)
    {
        return "PHRASE_" + _idx;
    }
}
