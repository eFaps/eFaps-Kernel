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

package org.efaps.db.stmt.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.efaps.db.stmt.selection.Select.SortedListValuedMap;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.ISquash;
import org.efaps.db.stmt.selection.elements.InstanceElement;
import org.efaps.util.EFapsException;

public class Squashing
{

    private final Select select;

    private boolean init = false;

    private final Map<String, Map<Integer, Integer>> directions = new HashMap<>();

    public Squashing(final Select _select)
    {
        this.select = _select;
        _select.setSquash(this);
    }

    public void execute(final String _path, final Select _select)
        throws EFapsException
    {
        Integer idx = 0;
        final ListValuedMap<Integer, Object> map = new SortedListValuedMap<>();
        final Map<Integer, Integer> baseDirections = getBaseDirections();
        for (final Object object : _select.getObjects()) {
            // negative means it gets at the same position
            if (baseDirections.get(idx) < 0) {
                map.put(idx, object);
            } else if (_select.isSquash()) {
                map.put(baseDirections.get(idx), object);
            }
            idx++;
        }
        final List<Object> tmpObjects = new ArrayList<>();
        for (final Collection<Object> col : map.asMap().values()) {
            if (!_select.isSquash()) {
                tmpObjects.add(col.iterator().next());
            } else {
                if (col.size() > 1) {
                    if (_select.getElements().get(_select.getElements().size() - 1) instanceof InstanceElement) {
                        final String key = tmpObjects.size() + ": " + _path;
                        final Map<Integer, Integer> subDirection = evalDirection(col);
                        this.directions.put(key, subDirection);
                        tmpObjects.add(reduce(subDirection, col));
                    } else {
                        // start from the last
                        AbstractElement<?> current = _select.getElements().get(_select.getElements().size() - 1);
                        while (current != null && !(current instanceof ISquash)) {
                            current = current.getPrevious();
                        }
                        final String key = tmpObjects.size() + ": " + current.getPath();
                        final Map<Integer, Integer> subDirection = this.directions.get(key);
                        tmpObjects.add(reduce(subDirection, col));
                    }
                } else {
                    tmpObjects.add(col);
                }
            }
        }
        _select.getObjects().clear();
        _select.getObjects().addAll(tmpObjects);
    }

    private void init()
        throws EFapsException
    {
        if (!this.init) {
            this.init = true;
            final List<Object> instances = this.select.getObjects(null);
            this.directions.put(Selection.BASEPATH, evalDirection(instances));
        }
    }

    private Map<Integer, Integer> getDirections(final String _path)
        throws EFapsException
    {
        init();
        return this.directions.get(_path);
    }

    private Map<Integer, Integer> evalDirection(final Collection<Object> _instances)
    {
        Integer idx = 0;
        // create a mapping of main instance to row numbers
        // e.g Instance1 = [0], Instance2 = [1,3]...
        final ListValuedMap<Object, Integer> map = MultiMapUtils.newListValuedHashMap();
        for (final Object instance : _instances) {
            map.put(instance, idx);
            idx++;
        }
        // create address: 0=-1, 1=-1, 2=1,.... -1 means stay where it is,
        // every other number says where to put it
        final Map<Integer, Integer> tmpDirections = new HashMap<>();
        for (final Collection<Integer> set : map.asMap().values()) {
            Integer current = -1;
            for (final Integer ele : set) {
                if (current < 0) {
                    tmpDirections.put(ele, -1);
                    current = ele;
                } else {
                    tmpDirections.put(ele, current);
                }
            }
        }
        return tmpDirections;
    }

    private Map<Integer, Integer> getBaseDirections()
        throws EFapsException
    {
        return getDirections(Selection.BASEPATH);
    }

    private Collection<Object> reduce(final Map<Integer, Integer> _directions, final Collection<Object> _objects)
    {
        Integer idx = 0;
        final Map<Integer, Object> map = new TreeMap<>();
        for (final Object object : _objects) {
            // negative means it gets at the same position
            if (_directions.get(idx) < 0) {
                map.put(idx, object);
            } else {
                map.put(_directions.get(idx), object);
            }
            idx++;
        }
        return new ArrayList<>(map.values());
    }
}
