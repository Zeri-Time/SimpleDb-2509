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

    public long insert() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS);
            setParams(ps);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, ps);
        }
    }
    public int update() {
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(getSql());
            setParams(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ps);
        }
    }
    public int delete() { return update(); }

    public List<Map<String, Object>> selectRows() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(getSql());
            setParams(ps);
            rs = ps.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, ps);
        }
    }
    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.get(0);
    }
    public Long selectLong() {
        Object value = getFirstValue(selectRow());
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString().trim()); } catch (NumberFormatException e) { return null; }
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
        List<Long> result = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(getSql());
            setParams(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, ps);
        }
    }
    public List<Article> selectRows(Class<Article> clazz) {
        List<Article> result = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(getSql());
            setParams(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRowToArticle(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, ps);
        }
    }
    public Article selectRow(Class<Article> clazz) {
        List<Article> articles = selectRows(clazz);
        return articles.isEmpty() ? null : articles.get(0);
    }
    public LocalDateTime selectDatetime() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(getSql());
            setParams(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, ps);
        }
    }

    public String getSql() { return builder.getSql(); }
    public List<Object> getParams() { return builder.getParams(); }
    private Connection getConnection() { return connection; }
    private void setParams(PreparedStatement ps) throws SQLException {
        List<Object> params = getParams();
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
    private void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) try { r.close(); } catch (Exception ignored) {}
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
}
