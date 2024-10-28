package com.heartsave.todaktodak_api.diary.entity;

import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.DIARY_CONTENT_MAX_SIZE;
import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.DIARY_CONTENT_MIN_SIZE;

import com.heartsave.todaktodak_api.ai.dto.AiContentResponse;
import com.heartsave.todaktodak_api.common.entity.BaseEntity;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

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
  @Column(nullable = false, length = 30)
  private DiaryEmotion emotion;

  @Size(min = DIARY_CONTENT_MIN_SIZE, max = DIARY_CONTENT_MAX_SIZE)
  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "ai_comment", nullable = true, columnDefinition = "text")
  private String aiComment;

  @Column(name = "webtoon_image_url", nullable = true, length = 255)
  private String webtoonImageUrl;

  @Column(name = "bgm_url", nullable = true, length = 255)
  private String bgmUrl;

  @Column(
      name = "diary_created_time",
      nullable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP(3)")
  @DateTimeFormat(iso = ISO.DATE_TIME)
  private LocalDateTime diaryCreatedTime;

  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "diaryEntity",
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Builder.Default
  @Setter
  private List<DiaryReactionEntity> reactions = new ArrayList<>();

  public void addAiContent(AiContentResponse response) {
    this.aiComment = response.getAiComment();
  }

  public void addReaction(DiaryReactionEntity reactionType) {
    reactions.add(reactionType);
  }
}
