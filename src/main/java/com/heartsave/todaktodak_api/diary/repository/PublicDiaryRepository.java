package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicDiaryRepository extends JpaRepository<PublicDiaryEntity, Long> {}
