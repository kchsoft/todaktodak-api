package com.heartsave.todaktodak_api.diary.entity;

import com.heartsave.todaktodak_api.common.entity.BaseEntity;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "DIARY_REACTION_SEQ_GENERATOR",
    sequenceName = "DIARY_REACTION_SEQ",
    initialValue = 1,
    allocationSize = 100)
@Table(
    name = "diary_reaction",
    uniqueConstraints = {
      @UniqueConstraint(
          columnNames = {"member_id", "public_diary_id", "reaction_type"},
          name = "uk_member_diary_reaction")
    })
@ToString
public class DiaryReactionEntity extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DIARY_REACTION_SEQ_GENERATOR")
  @Column(updatable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity memberEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "public_diary_id", nullable = false)
  private PublicDiaryEntity publicDiaryEntity;

  @Column(name = "reaction_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private DiaryReactionType reactionType;
}
