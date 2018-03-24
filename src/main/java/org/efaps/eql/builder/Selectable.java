package org.efaps.eql.builder;

import org.efaps.ci.CIAttribute;

public interface Selectable
{

    public static class Linkto
        implements Selectable
    {

        private final String linktoAttr;
        private String attr;

        public Linkto(final CIAttribute _attr)
        {
            this.linktoAttr = _attr.name;
        }

        public Linkto attr(final CIAttribute _attr)
        {
            return attribute(_attr);
        }

        public Linkto attribute(final CIAttribute _attr)
        {
            attr = _attr.name;
            return this;
        }

        protected String getLinktoAttr()
        {
            return linktoAttr;
        }

        protected String getAttr()
        {
            return attr;
        }

    }
}
