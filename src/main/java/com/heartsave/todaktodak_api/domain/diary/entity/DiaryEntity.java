package com.heartsave.todaktodak_api.domain.diary.entity;

import static com.heartsave.todaktodak_api.common.constant.TodakConstant.URL.DEFAULT_URL;

import com.heartsave.todaktodak_api.common.constant.TodakConstraintConstant.Diary;
import com.heartsave.todaktodak_api.common.entity.BaseEntity;
import com.heartsave.todaktodak_api.domain.ai.client.dto.response.AiClientCommentResponse;
import com.heartsave.todaktodak_api.domain.diary.constant.DiaryBgmGenre;
import com.heartsave.todaktodak_api.domain.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.domain.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "diary",
    indexes = {
      @Index(
          name = "idx_member_id_diary_created_time",
          columnList = "member_id DESC,diary_created_time DESC")
    })
public class DiaryEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity memberEntity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DiaryEmotion emotion;

  @Size(min = Diary.DIARY_CONTENT_MIN_SIZE, max = Diary.DIARY_CONTENT_MAX_SIZE)
  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "ai_comment", nullable = true, columnDefinition = "text")
  private String aiComment;

  @Column(name = "webtoon_image_url", nullable = false, length = 255)
  private String webtoonImageUrl;

  @Column(name = "bgm_url", nullable = false, length = 255)
  private String bgmUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "bgm_genre", nullable = false)
  private DiaryBgmGenre bgmGenre;

  @Column(
      name = "diary_created_time",
      nullable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP(3)")
  private Instant diaryCreatedTime;

  public void addAiContent(AiClientCommentResponse response) {
    this.aiComment = response.getAiComment();
  }

  private DiaryEntity(DiaryWriteRequest request, MemberEntity member) {
    this.memberEntity = member;
    this.emotion = request.getEmotion();
    this.content = request.getContent();
    this.diaryCreatedTime = request.getCreatedTime();
    this.webtoonImageUrl = DEFAULT_URL;
    this.bgmUrl = DEFAULT_URL;
    this.bgmGenre = request.getBgmGenre();
  }

  public static DiaryEntity createDefault(DiaryWriteRequest request, MemberEntity member) {
    return new DiaryEntity(request, member);
  }
}
