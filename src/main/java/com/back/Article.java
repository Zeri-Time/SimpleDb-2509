package com.back;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Article {
    private Long id;
    private String title;
    private String body;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private boolean isBlind;
}