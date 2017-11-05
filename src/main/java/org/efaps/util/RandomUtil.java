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

package org.efaps.util;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

/**
 * Util class for standard Random generation.
 */
public final class RandomUtil
{

    /** The alphanumeric. */
    private static RandomStringGenerator ALPHANUMERIC = new RandomStringGenerator.Builder().filteredBy(
                    CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build();

    /** The Alphabetic. */
    private static RandomStringGenerator ALPHABETIC = new RandomStringGenerator.Builder().filteredBy(
                    CharacterPredicates.LETTERS).build();

    /** The Alphabetic. */
    private static RandomStringGenerator NUMERIC = new RandomStringGenerator.Builder().filteredBy(
                    CharacterPredicates.DIGITS).build();

    /** The Alphabetic. */
    private static RandomStringGenerator ALL = new RandomStringGenerator.Builder().build();

    /**
     * Instantiates a new random.
     */
    private RandomUtil()
    {
        // singelton
    }

    /**
     * Random alphanumeric.
     *
     * @param _lenght the lenght
     * @return the string
     */
    public static String randomAlphanumeric(final int _lenght)
    {
        return ALPHANUMERIC.generate(_lenght);
    }

    /**
     * Random alphabetic.
     *
     * @param _lenght the lenght
     * @return the string
     */
    public static String randomAlphabetic(final int _lenght)
    {
        return ALPHABETIC.generate(_lenght);
    }

    /**
     * Random alphabetic.
     *
     * @param _lenght the lenght
     * @return the string
     */
    public static String randomNumeric(final int _lenght)
    {
        return NUMERIC.generate(_lenght);
    }

    /**
     * Random alphabetic.
     *
     * @param _lenght the lenght
     * @return the string
     */
    public static String random(final int _lenght)
    {
        return ALL.generate(_lenght);
    }
}
