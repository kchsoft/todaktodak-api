package com.heartsave.todaktodak_api.member.repository;

import com.heartsave.todaktodak_api.member.domain.CharacterCache;
import org.springframework.data.repository.CrudRepository;

// TODO: String으로 ID를 지정해야할 수도 있음
public interface CharacterCacheRepository extends CrudRepository<CharacterCache, Long> {}
