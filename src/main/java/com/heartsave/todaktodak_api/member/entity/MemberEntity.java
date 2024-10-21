package com.heartsave.todaktodak_api.member.entity;

import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.member.domain.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@Table(name = "member")
public class MemberEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member")
  @SequenceGenerator(name = "member", sequenceName = "member_seq", allocationSize = 1)
  private Long id;

  @Column(length = 50)
  private String email;

  @Column(length = 50)
  private String nickname;

  @Column(length = 30)
  private String loginId;

  @Column(length = 30)
  private String password;

  private String characterImageUrl;

  // AI 컨텐츠 사전 정보
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private Object characterInfo;

  @Enumerated(EnumType.STRING)
  private AuthType authType;

  @Enumerated(EnumType.STRING)
  private MemberRole role;
}
