package com.team2.storyservice.command.book.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

/**
 * 소설 수정 요청 DTO
 *
 * @author 정진호
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
}
