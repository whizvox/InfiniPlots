package me.whizvox.infiniplots.db;

import me.whizvox.infiniplots.util.SQLFunction;
import me.whizvox.infiniplots.util.SQLNull;
import me.whizvox.infiniplots.util.SQLRunnable;

import java.sql.*;
import java.util.List;

public abstract class Repository {

  protected final Connection conn;

  public Repository(Connection conn) {
    this.conn = conn;
  }

  private <T> T handle(SQLRunnable<T> runnable) {
    try {
      return runnable.run();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected boolean execute(String sql) {
    return handle(() -> {
      try (Statement stmt = conn.createStatement()) {
        return stmt.execute(sql);
      }
    });
  }

  protected int executeUpdate(String sql) {
    return handle(() -> {
      try (Statement stmt = conn.createStatement()) {
        return stmt.executeUpdate(sql);
      }
    });
  }

  private <R> R executePrepared(String sql, List<Object> args, SQLFunction<PreparedStatement, R> consumer) {
    return handle(() -> {
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        int index = 1;
        for (Object arg : args) {
          if (arg instanceof Boolean value) {
            stmt.setBoolean(index, value);
          } else if (arg instanceof Integer value) {
            stmt.setInt(index, value);
          } else if (arg instanceof Enum<?> value) {
            stmt.setByte(index, (byte) value.ordinal());
          } else if (arg instanceof SQLNull value) {
            stmt.setNull(index, value.sqlType());
          } else {
            stmt.setString(index, String.valueOf(arg));
          }
          index++;
        }
        return consumer.accept(stmt);
      }
    });
  }

  protected <R> R executeQuery(String sql, List<Object> args, SQLFunction<ResultSet, R> consumer) {
    return executePrepared(sql,args, stmt -> {
      ResultSet rs = stmt.executeQuery();
      return consumer.accept(rs);
    });
  }

  protected int executeUpdate(String sql, List<Object> args) {
    return executePrepared(sql, args, PreparedStatement::executeUpdate);
  }

  public abstract void initialize();

}
