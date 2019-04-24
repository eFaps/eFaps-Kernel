/*
 * Copyright 2003 - 2019 The eFaps Team
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

package org.efaps.db;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author The eFaps Team
 *
 */
public interface ICacheDefinition
{
    /**
     *
     * @return lifespan of the entry. Negative values are interpreted as
     *         unlimited lifespan. 0 means do not apply
     */
    default long getLifespan() {
        return 0;
    }

    /**
     * @return time unit for lifespan
     */
    default TimeUnit getLifespanUnit() {
        return TimeUnit.MINUTES;
    }

    /**
     * @return the maximum amount of time this key is allowed to be idle for
     *         before it is considered as expired. 0 means do not apply
     */
    default long getMaxIdleTime() {
        return 0;
    }

    /**
     * @return time unit for max idle time
     */
    default TimeUnit getMaxIdleTimeUnit() {
        return TimeUnit.MINUTES;
    }
}
