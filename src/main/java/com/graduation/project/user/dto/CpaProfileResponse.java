package com.graduation.project.user.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
public class CpaProfileResponse {
  UUID id;
  String cpaProfileName;
  String cpaProfileCode;
  double letterCpaScore;
  double numberCpaScore;
  double previousNumberCpaScore;
  int accumulatedCredits;
  List<GpaProfileResponse> gpaProfileResponses;
}
