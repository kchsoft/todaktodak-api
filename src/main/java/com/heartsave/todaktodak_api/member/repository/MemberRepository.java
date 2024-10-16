package com.heartsave.todaktodak_api.member.repository;

import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
  Optional<MemberEntity> findMemberByLoginId(String loginId);

  Optional<MemberEntity> findMemberByNickname(String nickname);
}
