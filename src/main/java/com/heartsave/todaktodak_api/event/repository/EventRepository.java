package com.heartsave.todaktodak_api.event.repository;

import com.heartsave.todaktodak_api.event.entity.EventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {
  @Query(
      "SELECT e FROM EventEntity e "
          + "WHERE e.memberEntity.id = :memberId "
          + "ORDER BY e.createdTime ASC")
  List<EventEntity> findAllEventsByMemberId(@Param("memberId") Long memberId);
}
