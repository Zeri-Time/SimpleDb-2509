package com.back.simpleDb;

import java.sql.*;
import java.util.*;

public class Sql {
    private final Connection con;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    //    DB 커넥션 받아 Sql객체 생성
    public Sql(Connection con) {
        this.con = con;
    }

    //    파라미터 바인딩
    public Sql append(String sqlPart, Object... args) {
        if (sqlBuilder.length() > 0) sqlBuilder.append(" ");
        sqlBuilder.append(sqlPart);
        if (args != null) Collections.addAll(params, args);
        return this;
    }

    // SQL 쿼리에서 IN 조건 쓸때 사용. 쿼리, params 추가
    public Sql appendIn(String sqlPart, Object... args) {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("IN 파라미터가 비어있습니다.");
        String placeholders = String.join(", ", Collections.nCopies(args.length, "?"));
        sqlBuilder.append(" ").append(sqlPart.replace("?", placeholders));
        Collections.addAll(params, args);
        return this;
    }

    //    ps에 params 바인딩
    private void setParams(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    //    자원을 안전하게 닫음
    private void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) try {
                r.close();
            } catch (Exception ignored) {
            }
        }
    }

    //    INSERT
    public long insert() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sqlBuilder.toString(), Statement.RETURN_GENERATED_KEYS);
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

    //    UPDATE / 영향받은 행 수를 반환
    public int update() {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sqlBuilder.toString());
            setParams(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ps);
        }
    }

    // DELETE
    public int delete() {
        return update();
    }

//    SELECT / list로 return
    public List<Map<String, Object>> selectRows() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sqlBuilder.toString());
            setParams(ps);
            rs = ps.executeQuery();
            return mapResultSetToList(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(rs, ps);
        }
    }

    // SELECT 쿼리 결과에서 첫번째 행만 반환
    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = selectRows();
        return rows.isEmpty() ? null : rows.get(0);
    }

//    ResultSet을 List/<Map/> 형태로 변환
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

//    Map의 첫번째 값 반환
    private Object getFirstValue(Map<String, Object> row) {
        if (row == null) return null;
        return row.values().stream().findFirst().orElse(null);
    }

//    SELECT 결과의 첫번째 값을 Long으로 반환
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

//    SELECT 결과의 첫번째 값을 String으로 반환
    public String selectString() {
        Object value = getFirstValue(selectRow());
        return value == null ? null : value.toString();
    }

    //    SELECT 결과의 첫번째 값을 Boolean으로 반환
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
}