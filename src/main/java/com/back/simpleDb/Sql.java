package com.back.simpleDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Sql {
    private final Connection conn;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    public Sql(Connection conn) {
        this.conn = conn;
    }

    public Sql append(String sqlPart, Object... args) {
        if (sqlBuilder.length() > 0) sqlBuilder.append(" ");
        sqlBuilder.append(sqlPart);
        Collections.addAll(params, args);
        return this;
    }

    public Sql appendIn(String sqlPart, Object... inParams) {
        if (inParams == null || inParams.length == 0) throw new IllegalArgumentException("IN 파라미터가 필요합니다.");
        String inClause = String.join(", ", Collections.nCopies(inParams.length, "?"));
        sqlBuilder.append(" ").append(sqlPart.replace("?", inClause));
        Collections.addAll(params, inParams);
        return this;
    }

    public long insert() {
        try (PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString(), Statement.RETURN_GENERATED_KEYS)) {
            setParams(pstmt);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int update() {
        try (PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            setParams(pstmt);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int delete() {
        return update();
    }

    public List<Map<String, Object>> selectRows() {
        try (PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            setParams(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return toList(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.get(0);
    }

    public LocalDateTime selectDatetime() {
        Map<String, Object> row = selectRow();
        Object value = row.values().iterator().next();
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (value instanceof java.sql.Date date) {
            return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    public Long selectLong() {
        Map<String, Object> row = selectRow();
        Object value = row.values().iterator().next();
        return value == null ? null : ((Number) value).longValue();
    }

    public List<Long> selectLongs() {
        List<Map<String, Object>> rows = selectRows();
        List<Long> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object value = row.values().iterator().next();
            result.add(value == null ? null : ((Number) value).longValue());
        }
        return result;
    }

    public String selectString() {
        Map<String, Object> row = selectRow();
        Object value = row.values().iterator().next();
        return value == null ? null : value.toString();
    }

    public Boolean selectBoolean() {
        Map<String, Object> row = selectRow();
        Object value = row.values().iterator().next();
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(value.toString());
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        List<Map<String, Object>> rows = selectRows();
        List<T> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(mapToClass(row, clazz));
        }
        return result;
    }

    public <T> T selectRow(Class<T> clazz) {
        Map<String, Object> row = selectRow();
        return row == null ? null : mapToClass(row, clazz);
    }

    private void setParams(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }
    }

    private List<Map<String, Object>> toList(ResultSet rs) throws SQLException {
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

    private <T> T mapToClass(Map<String, Object> row, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (var entry : row.entrySet()) {
                try {
                    var field = clazz.getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(obj, entry.getValue());
                } catch (NoSuchFieldException ignored) {}
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
