package com.heartsave.todaktodak_api.diary.entity;

import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.DIARY_CONTENT_MAX_SIZE;
import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.DIARY_CONTENT_MIN_SIZE;
import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.DIARY_PUBLIC_CONTENT_MAX_SIZE;

import com.heartsave.todaktodak_api.common.entity.BaseEntity;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "DIARY_SEQ_GENERATOR", // jpa name
        sequenceName = "DIARY_SEQ", // DB name
        initialValue = 1,
        allocationSize = 50)
@Table(name = "diary")
public class DiaryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DIARY_SEQ_GENERATOR")
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity memberEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaryEmotion emotion;

    @Size(min = DIARY_CONTENT_MIN_SIZE, max = DIARY_CONTENT_MAX_SIZE)
    @Column(nullable = false)
    private String content;

    @Size(max = DIARY_PUBLIC_CONTENT_MAX_SIZE)
    @Column(name = "public_content", nullable = true)
    private String publicContent;

    @Column(name = "ai_comment", nullable = true)
    private String aiComment;

    @Column(name = "webtoon_image_url", nullable = true)
    private String webtoonImageUrl;

    @Column(name = "bgm_url", nullable = true)
    private String bgmUrl;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(
            name = "diary_created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP")
    private LocalDateTime diaryCreatedAt;
}
