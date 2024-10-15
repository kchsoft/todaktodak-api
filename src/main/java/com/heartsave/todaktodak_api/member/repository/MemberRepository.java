package com.heartsave.todaktodak_api.member.repository;

import com.heartsave.todaktodak_api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
