package org.efaps.mock.datamodel;

import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;
import org.efaps.mock.Mocks;

public class CI
{
    public static final _TypedType TypedType = new _TypedType(Mocks.TypedType.getUuid().toString());

    public static class _TypedType
        extends CIType
    {

        protected _TypedType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute TestAttr = new CIAttribute(this, Mocks.TypedTypeTestAttr.getName());
    }
}
