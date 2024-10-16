package com.heartsave.todaktodak_api.member.entity;

import com.heartsave.todaktodak_api.common.type.AuthType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Member {
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

  @Enumerated(EnumType.STRING)
  private AuthType authType;
}
