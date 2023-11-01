package me.whizvox.infiniplots.util;

import java.sql.SQLException;

public interface SQLRunnable<T> {

  T run() throws SQLException;

}
