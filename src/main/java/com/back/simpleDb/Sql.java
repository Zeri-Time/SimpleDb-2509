package com.back.simpleDb;

import com.back.Article;
import lombok.Getter;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Sql {
    private final Connection connection;
    private final SqlBuilder builder;

    public Sql(Connection connection) {
        this.connection = connection;
        this.builder = new SqlBuilder();
    }

    public Sql append(String sqlPart, Object... args) {
        builder.append(sqlPart, args);
        return this;
    }

    public Sql appendIn(String sqlPart, Object... args) {
        builder.appendIn(sqlPart, args);
        return this;
    }

    //예시로 남겨둔 코드(삭제예정)
//    public long insert() {
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        try {
//            ps = getConnection().prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS);
//            setParams(ps);
//            ps.executeUpdate();
//            rs = ps.getGeneratedKeys();
//            if (rs.next()) return rs.getLong(1);
//            return 0;
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } finally {
//            close(rs, ps);
//        }
//    }

    public long insert() {
        return execute((ps) -> {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                return 0L;
            }
        }, true);
    }

    public int update() {
        return execute(PreparedStatement::executeUpdate, false);
    }

    public int delete() {
        return update();
    }

    public List<Map<String, Object>> selectRows() {
        return execute((ps) -> {
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        }, false);
    }

    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Long selectLong() {
        Object value = getFirstValue(selectRow());
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String selectString() {
        Object value = getFirstValue(selectRow());
        return value == null ? null : value.toString();
    }

    public Boolean selectBoolean() {
        Object value = getFirstValue(selectRow());
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        String s = value.toString().trim().toLowerCase();
        if ("1".equals(s) || "true".equals(s)) return true;
        if ("0".equals(s) || "false".equals(s)) return false;
        return Boolean.parseBoolean(s);
    }

    public List<Long> selectLongs() {
        return execute((ps) -> {
            List<Long> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getLong(1));
                }
            }
            return result;
        }, false);
    }

    public List<Article> selectRows(Class<Article> clazz) {
        return execute((ps) -> {
            List<Article> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowToArticle(rs));
                }
            }
            return result;
        }, false);
    }

    public Article selectRow(Class<Article> clazz) {
        List<Article> articles = selectRows(clazz);
        return articles.isEmpty() ? null : articles.get(0);
    }

    public LocalDateTime selectDatetime() {
        return execute((ps) -> {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp(1).toLocalDateTime();
                }
                return null;
            }
        }, false);
    }

    public String getSql() {
        return builder.getSql();
    }

    public List<Object> getParams() {
        return builder.getParams();
    }

    private Connection getConnection() {
        return connection;
    }

    private void setParams(PreparedStatement ps) throws SQLException {
        List<Object> params = getParams();
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) try {
                r.close();
            } catch (Exception ignored) {
            }
        }
    }

    private List<Map<String, Object>> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }

    private Object getFirstValue(Map<String, Object> row) {
        if (row == null) return null;
        return row.values().stream().findFirst().orElse(null);
    }

    private Article mapRowToArticle(ResultSet rs) throws SQLException {
        Article article = new Article();
        article.setId(rs.getLong("id"));
        article.setTitle(rs.getString("title"));
        article.setBody(rs.getString("body"));
        article.setCreatedDate(rs.getTimestamp("createdDate").toLocalDateTime());
        article.setModifiedDate(rs.getTimestamp("modifiedDate").toLocalDateTime());
        article.setBlind(rs.getBoolean("isBlind"));
        return article;
    }

    //  --execute before
    // execute 헬퍼 메서드와 함수형 인터페이스 추가
    // db에 연결하고 에러처리하는 반복적인 코드룰 여기서 처리해서 중복 x
//    private <T> T execute(SqlAction<T> action, boolean returnGeneratedKeys) {
//        Connection conn = null;
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//            conn = getConnection();
//            if (returnGeneratedKeys) {
//                ps = conn.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS);
//            } else {
//                ps = conn.prepareStatement(getSql());
//            }
//            setParams(ps);
//            return action.apply(ps);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (ps != null) ps.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    //  --execute edited
//    rs와 ps를 try-with-resources를 이용해서 닫기
//    ps가 닫힐때 알아서 연결된 rs도 자동으로 닫힘
    private <T> T execute(SqlAction<T> action, boolean returnGeneratedKeys) {
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = returnGeneratedKeys ?
                    conn.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS) :
                    conn.prepareStatement(getSql())) {

                setParams(ps);
                return action.apply(ps);
            } // ps는 여기서 자동으로 close() 호출
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface SqlAction<T> {
        T apply(PreparedStatement ps) throws SQLException;
    }
}
