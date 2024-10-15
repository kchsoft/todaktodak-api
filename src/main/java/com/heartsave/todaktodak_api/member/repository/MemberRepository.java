package com.heartsave.todaktodak_api.member.repository;

import com.heartsave.todaktodak_api.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findMemberByLoginId(String loginId);

  Optional<Member> findMemberByNickname(String nickname);
}
