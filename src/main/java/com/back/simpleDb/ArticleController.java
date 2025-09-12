package com.back.simpleDb;

import com.back.Article;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    private final SimpleDb simpleDb;

    public ArticleController() {
        this.simpleDb = new SimpleDb("localhost", "root", "root123414", "simpleDb__test");
    }

    @GetMapping
    public List<Article> getArticles() {
        Sql sql = simpleDb.genSql();
        sql.append("SELECT * FROM article ORDER BY id ASC");
        return sql.selectRows(Article.class);
    }
}
