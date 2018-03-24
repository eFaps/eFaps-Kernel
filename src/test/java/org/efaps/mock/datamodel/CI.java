package org.efaps.mock.datamodel;

import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;
import org.efaps.mock.Mocks;

public class CI
{


    public static final _SimpleType SimpleType = new _SimpleType(Mocks.SimpleType.getUuid().toString());

    public static class _SimpleType
        extends CIType
    {

        protected _SimpleType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute TestAttr = new CIAttribute(this, Mocks.TestAttribute.getName());
    }


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


    public static final _AllAttrType AllAttrType = new _AllAttrType(Mocks.AllAttrType.getUuid().toString());

    public static class _AllAttrType
        extends CIType
    {

        protected _AllAttrType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute StringAttribute = new CIAttribute(this, Mocks.AllAttrStringAttribute .getName());
        public final CIAttribute LinkAttribute = new CIAttribute(this, Mocks.AllAttrLinkAttribute .getName());
    }

}
