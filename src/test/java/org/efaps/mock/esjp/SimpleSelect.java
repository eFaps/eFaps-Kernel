/*
 * Copyright 2003 - 2017 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.mock.esjp;

import java.util.List;

import org.efaps.db.Instance;
import org.efaps.eql.IEsjpSelect;
import org.efaps.util.EFapsException;

public class SimpleSelect
    implements IEsjpSelect
{

    @Override
    public void initialize(final List<Instance> _instances, final String... _parameters)
        throws EFapsException
    {

    }

    @Override
    public Object getValue(final Instance _instance)
        throws EFapsException
    {
        return _instance;
    }
}
