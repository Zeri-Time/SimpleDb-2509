package com.back.simpleDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleDb {
    private String url;
    private String username;
    private String password;
    private boolean devMode = false;

    private final ThreadLocal<Connection> threadLocalCon = new ThreadLocal<>();

    public SimpleDb(String host, String username, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":3306/" + dbName + "?serverTimezone=UTC";
        this.username = username;
        this.password = password;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    private Connection getConnection() {
        Connection con = threadLocalCon.get();
        if (con == null) {
            try {
                con = DriverManager.getConnection(url, username, password);
                threadLocalCon.set(con);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return con;
    }

    public void run(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParams(pstmt, params);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Sql genSql() {
        return new Sql(getConnection());
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    public void close() {
        Connection con = threadLocalCon.get();
        if (con != null) {
            try { con.close(); } catch (Exception ignored) {}
            threadLocalCon.remove();
        }
    }
}
