package com.team2.storyservice.websocket.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 입력 상태 메시지 DTO
 *
 * @author 정진호
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatus {

    @JsonProperty("bookId")
    private Long bookId;
    @JsonProperty("userNickname")
    private String userNickname;
    @JsonProperty("isTyping")
    private boolean isTyping;

    public static TypingStatus startTyping(Long bookId, String userNickname) {
        return new TypingStatus(bookId, userNickname, true);
    }

    public static TypingStatus stopTyping(Long bookId, String userNickname) {
        return new TypingStatus(bookId, userNickname, false);
    }
}
