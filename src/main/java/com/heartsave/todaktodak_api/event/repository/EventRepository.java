package com.heartsave.todaktodak_api.event.repository;

import com.heartsave.todaktodak_api.event.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {}
