package com.heartsave.todaktodak_api.member.repository;

import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.entity.projection.MemberProfileProjection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
  Optional<MemberEntity> findMemberEntityByLoginId(String loginId);

  Optional<MemberEntity> findMemberEntityByNickname(String nickname);

  Optional<MemberEntity> findMemberEntityByEmail(String email);

  boolean existsByEmail(String email);

  Optional<MemberProfileProjection> findProjectedById(Long id);
}
