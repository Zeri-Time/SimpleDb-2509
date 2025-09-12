package com.back.simpleDb.initData;

import com.back.simpleDb.SimpleDb;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Component
public class InitData {
    private final SimpleDb simpleDb;

    public InitData() {
        simpleDb = new SimpleDb("localhost", "root", "root123414", "simpleDb__test");
        simpleDb.setDevMode(true);
    }

    @PostConstruct
    public void init() {
        simpleDb.run("DROP TABLE IF EXISTS article");
        simpleDb.run("""
                    CREATE TABLE article (
                        id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                        PRIMARY KEY(id),
                        createdDate DATETIME NOT NULL,
                        modifiedDate DATETIME NOT NULL,
                        title VARCHAR(100) NOT NULL,
                        `body` TEXT NOT NULL,
                        isBlind BIT(1) NOT NULL DEFAULT 0
                    )
                """);
        truncateArticleTable();
        makeArticleTestData();
    }
    private void truncateArticleTable() {
        simpleDb.run("TRUNCATE article");
    }

    private void makeArticleTestData() {
        IntStream.rangeClosed(1, 6).forEach(no -> {
            boolean isBlind = no > 3;
            String title = "제목%d".formatted(no);
            String body = "내용%d".formatted(no);

            simpleDb.run("""
                INSERT INTO article
                SET createdDate = NOW(),
                    modifiedDate = NOW(),
                    title = ?,
                    `body` = ?,
                    isBlind = ?
            """, title, body, isBlind);
        });
    }
}