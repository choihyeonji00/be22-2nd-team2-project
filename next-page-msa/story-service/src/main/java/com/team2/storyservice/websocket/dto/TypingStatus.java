package com.team2.storyservice.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @com.fasterxml.jackson.annotation.JsonProperty("bookId")
    private Long bookId;
    @com.fasterxml.jackson.annotation.JsonProperty("userNickname")
    private String userNickname;
    @com.fasterxml.jackson.annotation.JsonProperty("isTyping")
    private boolean isTyping;

    public static TypingStatus startTyping(Long bookId, String userNickname) {
        return new TypingStatus(bookId, userNickname, true);
    }

    public static TypingStatus stopTyping(Long bookId, String userNickname) {
        return new TypingStatus(bookId, userNickname, false);
    }
}
