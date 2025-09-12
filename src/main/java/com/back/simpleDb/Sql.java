package com.back.simpleDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Sql {
    private Connection con;
    private StringBuilder sqlBuilder = new StringBuilder();
    private List<Object> params = new ArrayList<>();

    public Sql(Connection con) {
        this.con = con;
    }

    private void setParams(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

// t1
    public Sql append(String sqlPart, Object... args) {
        if (sqlBuilder.length() > 0) {
            sqlBuilder.append(" ");
        }
        sqlBuilder.append(sqlPart);
        for (Object arg : args) {
            params.add(arg);
        }
        return this;
    }

    public long insert() {
        try {
            PreparedStatement ps = con.prepareStatement(sqlBuilder.toString(), Statement.RETURN_GENERATED_KEYS);
            setParams(ps);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
// t2
    public int update() {
        try {
            PreparedStatement ps = con.prepareStatement(sqlBuilder.toString());
            setParams(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
// t3
    public int delete() {
        return update();
    }

    public List<Map<String, Object>> selectRows() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement(sqlBuilder.toString());
            setParams(ps);
            ResultSet rs = ps.executeQuery();
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
// t4 t5
    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        if (rows.size() == 0) {
            return null;
        }
        return rows.get(0);
    }
// t6
    public LocalDateTime selectDatetime() {
        Map<String, Object> row = selectRow();
        if (row == null) return null;
        Object value = row.values().iterator().next();
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate().atStartOfDay();
        }
        return null;
    }
// t7
    public Long selectLong() {
        Map<String, Object> row = selectRow();
        if (row == null) return null;
        Object value = row.values().iterator().next();
        if (value == null) return null;
        return ((Number) value).longValue();
    }
// t8
    public String selectString() {
        Map<String, Object> row = selectRow();
        if (row == null) return null;
        Object value = row.values().iterator().next();
        if (value == null) return null;
        return value.toString();
    }
// t9 t10 t11
    public Boolean selectBoolean() {
        Map<String, Object> row = selectRow();
        if (row == null) return null;
        Object value = row.values().iterator().next();
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return Boolean.parseBoolean(value.toString());
    }




}
