package com.back;

import java.time.LocalDateTime;

// record 사용 이유: 불변(value object에 적합) / 간결한 코드 / 보일러플레이트 줄어듬
public record Article(
        Long id,
        String title,
        String body,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        boolean isBlind
) {
}