package com.heartsave.todaktodak_api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {

  @CreatedDate
  @Column(name = "created_time", updatable = false, columnDefinition = "TIMESTAMP(3)")
  private Instant createdTime;

  @CreatedBy
  @Column(name = "created_by")
  private Long createdBy;

  @LastModifiedDate
  @Column(name = "updated_time", columnDefinition = "TIMESTAMP(3)")
  private Instant updatedTime;

  @LastModifiedBy
  @Column(name = "updated_By")
  private Long updatedBy;
}
