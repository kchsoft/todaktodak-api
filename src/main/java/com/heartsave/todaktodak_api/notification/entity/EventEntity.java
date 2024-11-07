package com.heartsave.todaktodak_api.notification.entity;

import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Table(name = "event")
public class EventEntity {
  @Id private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private MemberEntity memberEntity;

  @Column(updatable = false)
  private String eventName;

  @Column(updatable = false)
  private String eventData;

  @CreatedDate
  @Column(updatable = false, columnDefinition = "TIMESTAMP(3)")
  private LocalDateTime createdTime;

  @PrePersist
  public void generateId() {
    if (this.id == null) this.id = memberEntity.getId() + "_" + System.currentTimeMillis();
  }
}
