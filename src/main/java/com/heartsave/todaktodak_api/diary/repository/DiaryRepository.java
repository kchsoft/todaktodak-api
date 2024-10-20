package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM diary WHERE memberId == id and diaryDate::date == diary_created_at::date)", nativeQuery = true)
    boolean existsByDate(@Param("memberId") Long memberId,
            @Param("diaryDate") LocalDateTime diaryDate);
}
