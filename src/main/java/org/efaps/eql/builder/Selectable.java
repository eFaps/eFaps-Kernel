package org.efaps.eql.builder;

import org.efaps.ci.CIAttribute;

public interface Selectable
{
    public static class Linkto implements Selectable{
        public Linkto(final CIAttribute _attr) {
        }
        public void attr(final CIAttribute _attr) {
        }
    }
}
