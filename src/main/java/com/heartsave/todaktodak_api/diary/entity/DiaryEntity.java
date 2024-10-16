package com.heartsave.todaktodak_api.diary.entity;

import static com.heartsave.todaktodak_api.diary.common.DiaryContentConstraintConstant.DIARY_CONTENT_MAX_SIZE;
import static com.heartsave.todaktodak_api.diary.common.DiaryContentConstraintConstant.DIARY_CONTENT_MIN_SIZE;
import static com.heartsave.todaktodak_api.diary.common.DiaryContentConstraintConstant.DIARY_PUBLIC_CONTENT_MAX_SIZE;

import com.heartsave.todaktodak_api.common.entity.BaseEntity;
import com.heartsave.todaktodak_api.diary.common.DiaryEmotion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
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
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiaryEmotion emotion;

  @Size(min = DIARY_CONTENT_MIN_SIZE, max = DIARY_CONTENT_MAX_SIZE)
  @Column(nullable = false)
  private String content;

  @Size(max = DIARY_PUBLIC_CONTENT_MAX_SIZE)
  @Column(nullable = true)
  private String publicContent;

  @Column(nullable = true)
  private String aiComment;

  @Column(nullable = true)
  private String webtoonImageUrl;

  @Column(nullable = true)
  private String bgmUrl;

  @Column(nullable = false)
  private Boolean isPublic;
}
