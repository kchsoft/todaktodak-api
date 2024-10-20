package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.ai.dto.AiContentResponse;
import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.type.ErrorSpec;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {

    private final AiService aiService;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public DiaryWriteResponse write(OAuth2User auth, DiaryWriteRequest request) {
        DiaryEntity diary = createDiaryEntity(auth, request);
        Long memberId = diary.getMemberEntity().getId();
        LocalDateTime diaryCreatedDate = diary.getDiaryCreatedAt();

        if (diaryRepository.existsByDate(memberId, diaryCreatedDate)) {
            log.error("하루 일기 작성량을 초과하였습니다. memberId = {}", diary.getMemberEntity().getId());
            throw new BaseException(ErrorSpec.DIARY_DAILY_WRITING_LIMIT_EXCEPTION);
        }

        log.info("AI 컨텐츠 생성 요청을 시작합니다.");
        AiContentResponse response = aiService.callAiContent(diary);
        log.info("AI 컨텐츠 생성 요청을 마쳤습니다.");

        log.info("DB에 일기 저장을 요청합니다.");
        diary.addAiContent(response);
        diaryRepository.save(diary);
        log.info("DB에 일기를 저장하였습니다.");
        return DiaryWriteResponse.builder().aiComment(response.getAiComment()).build();
    }

    private DiaryEntity createDiaryEntity(OAuth2User auth, DiaryWriteRequest request) {
        MemberEntity member = memberRepository.findMemberByLoginId(auth.getName()).get();
        return DiaryEntity.builder()
                .memberEntity(member)
                .emotion(request.getEmotion())
                .content(request.getContent())
                .publicContent(request.getPublicContent())
                .isPublic(request.getIsPublic())
                .diaryCreatedAt((request.getDate()))
                .build();
    }
}
