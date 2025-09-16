package com.back.simpleDb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    private final SimpleDb simpleDb;

    public ArticleController() {
        this.simpleDb = new SimpleDb("localhost", "root", "root123414", "simpleDb__test");
    }

    @GetMapping
    public List<Map<String, Object>> getArticles() {
        Sql sql = simpleDb.genSql();
        sql.append("SELECT * FROM article ORDER BY id ASC");
        return sql.selectRows();
    }
}
