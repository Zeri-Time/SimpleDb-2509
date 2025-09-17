package com.back.simpleDb;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class SimpleDb {
    private final String url;
    private final String username;
    private final String password;
    private final ThreadLocal<Connection> threadLocalCon = new ThreadLocal<>();
    private boolean devMode = false;

    public SimpleDb(String host, String username, String password, String dbName) {
        this.url = "jdbc:mysql://" + host + ":3306/" + dbName + "?serverTimezone=UTC";
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() {
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
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Sql genSql() {
        return new Sql(getConnection());
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    // 테스트 코드 마지막에 @AfterAll 어노테이션을 이용해서 임시로 사용중
    public void closeConnection() {
        Connection con = threadLocalCon.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                threadLocalCon.remove();
            }
        }
    }
}
