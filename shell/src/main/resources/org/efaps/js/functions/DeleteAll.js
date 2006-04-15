importClass(Packages.org.efaps.db.Context);

function deleteAll()  {
  var context = new Context();

  try  {
    context.getDbType().deleteAll(context.getConnection());
  } catch (e)  {
print(e);
  } finally  {
    context.close();
  }
}