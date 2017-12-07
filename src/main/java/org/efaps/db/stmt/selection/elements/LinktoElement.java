package org.efaps.db.stmt.selection.elements;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.Tableidx;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class LinktoElement.
 */
public class LinktoElement
    extends AbstractElement<LinktoElement>
{

    private Attribute attribute;

    public Attribute getAttribute()
    {
        return this.attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute the attribute
     * @return the attribute element
     */
    public LinktoElement setAttribute(final Attribute attribute)
    {
        this.attribute = attribute;
        this.setDBTable(attribute.getTable());
        return this;
    }

    @Override
    public LinktoElement getThis()
    {
        return this;
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws CacheReloadException
    {
        if (this.getTable() instanceof SQLTable) {
            final String tableName = ((SQLTable) this.getTable()).getSqlTable();
            final String key;
            if (this.getPrevious() != null && this.getPrevious() instanceof LinktoElement) {
                key = ((SQLTable) ((LinktoElement) this.getPrevious()).getTable()).getSqlTable() + "--" + tableName;
            } else {
                key = tableName;
            }
            final Tableidx tableidx = _sqlSelect.getIndexer().getTableIdx(tableName, key);

            final Attribute joinAttr = this.attribute.getLink().getAttribute("ID");
            final String joinTableName = joinAttr.getTable().getSqlTable();
            final Tableidx joinTableidx = _sqlSelect.getIndexer().getTableIdx(joinTableName, tableName + "--"
                            + joinTableName);

            if (joinTableidx.isCreated()) {
                _sqlSelect.leftJoin(joinTableName, joinTableidx.getIdx(), "ID",
                                tableidx.getIdx(), this.attribute.getSqlColNames().get(0));
            }
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        return this.getNext().getObject(_row);
    }
}
