package com.team2.nextpage.command.member;

import com.team2.nextpage.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 회원 엔티티 (Soft Delete 적용)
 *
 * @author 김태형
 */
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String userEmail;

    // other fields...
    private String userStatus; // ACTIVE, DELETED

    /**
     * 회원 탈퇴 처리
     */
    public void deactivate() {
        this.userStatus = "DELETED";
    }
}
