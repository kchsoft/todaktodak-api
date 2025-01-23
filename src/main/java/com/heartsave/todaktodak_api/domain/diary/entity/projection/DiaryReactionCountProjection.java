package com.heartsave.todaktodak_api.domain.diary.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일기 반응 수 정보")
public interface DiaryReactionCountProjection {

  @Schema(description = "좋아요 수", example = "42")
  @JsonProperty("like")
  Long getLikes();

  @Schema(description = "놀라워요 수", example = "15")
  Long getSurprised();

  @Schema(description = "공감해요 수", example = "28")
  Long getEmpathize();

  @Schema(description = "응원해요 수", example = "35")
  Long getCheering();
}
