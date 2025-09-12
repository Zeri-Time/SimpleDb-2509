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

    public SimpleDb(String host, String username, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":3306/" + dbName + "?serverTimezone=UTC";
        this.username = username;
        this.password = password;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
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
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            return new Sql(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
