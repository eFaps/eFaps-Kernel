importClass(Packages.org.efaps.db.Context);

/**
 * This is the Derby specific implementation of an all deletion.
 */
function _deleteAllDerby()  {
  var context = new Context();

  try  {

    var con = context.getConnection();

    var stmtSel = con.createStatement();
    var stmtExec = con.createStatement();

    // remove all foreign keys
    print("Remove Foreign Keys");
    var rs = stmtSel.executeQuery("select t.TABLENAME, c.CONSTRAINTNAME from SYS.SYSSCHEMAS s, SYS.SYSTABLES t, SYS.SYSCONSTRAINTS c where s.AUTHORIZATIONID<>'DBA' and s.SCHEMAID=t.SCHEMAID and t.TABLEID=c.TABLEID and c.TYPE='F'");
    var hasNext = rs.next();
    while (hasNext)  {
      var table = rs.getString(1);
      var constr = rs.getString(2);
      print("  - Table '"+table+"' Constraint '"+constr+"'");
      stmtExec.execute("alter table "+table+" drop constraint "+constr);
      hasNext = rs.next();
    }
    rs.close();

    // remove all views
    print("Remove Views");
    var rs = stmtSel.executeQuery("select t.TABLENAME from SYS.SYSSCHEMAS s, SYS.SYSTABLES t where s.AUTHORIZATIONID<>'DBA' and s.SCHEMAID=t.SCHEMAID and t.TABLETYPE='V'");
    var hasNext = rs.next();
    while (hasNext)  {
      var table = rs.getString(1);
      print("  - View '"+table+"'");
      stmtExec.execute("drop view "+table);
      hasNext = rs.next();
    }
    rs.close();

    // remove all tables
    print("Remove Tables");
    var rs = stmtSel.executeQuery("select t.TABLENAME from SYS.SYSSCHEMAS s, SYS.SYSTABLES t where s.AUTHORIZATIONID<>'DBA' and s.SCHEMAID=t.SCHEMAID and t.TABLETYPE='T'");
    var hasNext = rs.next();
    while (hasNext)  {
      var table = rs.getString(1);
      print("  - Table '"+table+"'");
      stmtExec.execute("drop table "+table);
      hasNext = rs.next();
    }
    rs.close();

    stmtSel.close();
    stmtExec.close();
  } catch (e)  {
print(e);
  } finally  {
    context.close();
  }
}

/**
 * This is the Oracle specific implementation of an all deletion.
 */
function _deleteAllOracle()  {
  var context = new Context();

  try  {

    var con = context.getConnection();

    var stmtSel = con.createStatement();
    var stmtExec = con.createStatement();

    // remove all views
    print("Remove Views");
    var rs = stmtSel.executeQuery("select VIEW_NAME from ALL_VIEWS where OWNER='" + Context.getDbUser() + "'");
    var hasNext = rs.next();
    while (hasNext)  {
      var table = rs.getString(1);
      print("  - View '"+table+"'");
      stmtExec.execute("drop view "+table);
      hasNext = rs.next();
    }
    rs.close();

    // remove all tables
    print("Remove Tables");
    var rs = stmtSel.executeQuery("select TABLE_NAME from ALL_TABLES where OWNER='" + Context.getDbUser() + "'");
    var hasNext = rs.next();
    while (hasNext)  {
      var table = rs.getString(1);
      print("  - Table '"+table+"'");
      stmtExec.execute("drop table " + table + " cascade constraints");
      hasNext = rs.next();
    }
    rs.close();

    stmtSel.close();
    stmtExec.close();
  } catch (e)  {
print(e);
  } finally  {
    context.close();
  }
}

// IBM's Derby
if (Context.getDbType()==Context.DbType.Derby)  {
  deleteAll = _deleteAllDerby;
// Oracle
} else if (Context.getDbType()==Context.DbType.Oracle)  {
  deleteAll = _deleteAllOracle;
} else  {
  print("wrong database type '"+Context.getDbType()+"' defined! Delete function not known!");
}