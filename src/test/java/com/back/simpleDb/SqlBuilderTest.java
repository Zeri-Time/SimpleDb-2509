package com.back.simpleDb;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlBuilderTest {
    @Test
    void t1() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT * FROM article WHERE id = ?", 1);

        assertThat(builder.getSql()).isEqualTo("SELECT * FROM article WHERE id = ?");
        assertThat(builder.getParams()).containsExactly(1);
    }

    @Test
    void t2() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT * FROM article")
               .appendIn("WHERE id IN (?)", 1, 2, 3);

        assertThat(builder.getSql()).isEqualTo("SELECT * FROM article WHERE id IN (?, ?, ?)");
        assertThat(builder.getParams()).containsExactly(1, 2, 3);
    }

    @Test
    void t3() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT * FROM article")
               .append("WHERE title = ?", "제목1")
               .append("AND isBlind = ?", false);

        assertThat(builder.getSql()).isEqualTo("SELECT * FROM article WHERE title = ? AND isBlind = ?");
        assertThat(builder.getParams()).containsExactly("제목1", false);
    }

    @Test
    void t4() {
        SqlBuilder builder = new SqlBuilder();
        try {
            builder.appendIn("WHERE id IN (?)");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("IN 파라미터가 비어있습니다.");
        }
    }
}

