package com.heartsave.todaktodak_api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
public class BaseEntity {

  @CreatedDate
  @CreationTimestamp
  @Column(name = "created_time", updatable = false, columnDefinition = "TIMESTAMPTZ")
  private Instant createdTime;

  @CreatedBy private String createdBy;

  @LastModifiedDate
  @Column(name = "updated_time", columnDefinition = "TIMESTAMPTZ")
  private Instant updatedTime;

  @LastModifiedBy private String updatedBy;
}
