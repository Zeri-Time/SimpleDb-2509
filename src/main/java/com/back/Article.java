package com.back;

import java.time.LocalDateTime;

public record Article(
        Long id,
        String title,
        String body,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        boolean isBlind
) {
//    public Article(com.back.Article article) {
//        this(
//                article.id(),
//                article.title(),
//                article.body(),
//                article.createdDate(),
//                article.modifiedDate(),
//                article.isBlind()
//        );
//    }
}