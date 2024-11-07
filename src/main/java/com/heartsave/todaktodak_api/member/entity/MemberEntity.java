package com.heartsave.todaktodak_api.member.entity;

import static com.heartsave.todaktodak_api.common.constant.ConstraintConstant.Member.*;

import com.heartsave.todaktodak_api.common.entity.BaseEntity;
import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
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
public class MemberEntity extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member")
  @SequenceGenerator(name = "member", sequenceName = "member_seq", allocationSize = 1)
  private Long id;

  @Column(length = EMAIL_MAX_SIZE)
  private String email;

  @Column(length = NICKNAME_MAX_SIZE)
  private String nickname;

  @Column(length = LOGIN_ID_MAX_SIZE)
  private String loginId;

  @Column(length = PASSWORD_MAX_SIZE)
  private String password;

  private String characterImageUrl;

  // AI 컨텐츠 사전 정보
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private Object characterInfo;

  @Enumerated(EnumType.STRING)
  private AuthType authType;

  @Enumerated(EnumType.STRING)
  private TodakRole role;

  private Integer characterSeed;

  private String characterStyle;

  public static MemberEntity createById(Long id) {
    return MemberEntity.builder().id(id).build();
  }

  public void updateNickname(String newNickName) {
    nickname = newNickName;
  }

  public void updateRole(String role) {
    this.role = TodakRole.valueOf(role);
  }

  public void updateCharacterInfo(
      String characterInfo, String characterStyle, Integer characterSeed) {
    this.characterInfo = characterInfo;
    this.characterStyle = characterStyle;
    this.characterSeed = characterSeed;
  }
}
