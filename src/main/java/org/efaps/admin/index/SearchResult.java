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
package org.efaps.admin.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The Class SearchResult.
 *
 * @author The eFaps Team
 */
public class SearchResult
{

    /** The elements. */
    private final List<Element> elements = new ArrayList<>();

    /**
     * Gets the elements.
     *
     * @return the elements
     */
    public List<Element> getElements()
    {
        return this.elements;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * The Class Element.
     */
    public static class Element
    {

        /** The oid. */
        private String oid;

        /** The text. */
        private String text;

        /**
         * Gets the oid.
         *
         * @return the oid
         */
        public String getOid()
        {
            return this.oid;
        }

        /**
         * Sets the oid.
         *
         * @param _oid the oid
         * @return the element
         */
        public Element setOid(final String _oid)
        {
            this.oid = _oid;
            return this;
        }

        /**
         * Gets the text.
         *
         * @return the text
         */
        public String getText()
        {
            return this.text;
        }

        /**
         * Sets the text.
         *
         * @param _text the text
         * @return the element
         */
        public Element setText(final String _text)
        {
            this.text = _text;
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
