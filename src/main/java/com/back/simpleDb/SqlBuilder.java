package com.back.simpleDb;

import java.util.*;
import lombok.Getter;

@Getter
public class SqlBuilder {
    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    public SqlBuilder append(String sqlPart, Object... args) {
        if (sqlBuilder.length() > 0) sqlBuilder.append(" ");
        sqlBuilder.append(sqlPart);
        if (args != null) Collections.addAll(params, args);
        return this;
    }

    public SqlBuilder appendIn(String sqlPart, Object... args) {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException("IN 파라미터가 비어있습니다.");
        String placeholders = String.join(", ", Collections.nCopies(args.length, "?"));
        sqlBuilder.append(" ").append(sqlPart.replace("?", placeholders));
        Collections.addAll(params, args);
        return this;
    }

    public String getSql() {
        return sqlBuilder.toString();
    }
}
