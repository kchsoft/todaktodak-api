package com.heartsave.todaktodak_api.domain.member.repository;

import com.heartsave.todaktodak_api.domain.member.entity.CharacterEntity;
import org.springframework.data.repository.CrudRepository;

// TODO: String으로 ID를 지정해야할 수도 있음
public interface CharacterCache extends CrudRepository<CharacterEntity, Long> {}
