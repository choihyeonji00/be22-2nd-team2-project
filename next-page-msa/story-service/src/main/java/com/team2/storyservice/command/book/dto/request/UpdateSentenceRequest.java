package com.team2.storyservice.command.book.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

/**
 * 문장 수정 요청 DTO
 *
 * @author 정진호
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSentenceRequest {
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
}
