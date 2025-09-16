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
}