package com.heartsave.todaktodak_api.ai.webhook.repository;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDiaryRepository extends JpaRepository<DiaryEntity, Long> {}
