package com.heartsave.todaktodak_api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
public class BaseEntity {

    @CreatedDate
    @Column(name = "created_time", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdTime;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_time", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedTime;

    @LastModifiedBy
    @Column(name = "updated_By")
    private Long updatedBy;
}
