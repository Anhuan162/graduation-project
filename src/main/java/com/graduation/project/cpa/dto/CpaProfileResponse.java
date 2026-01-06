package com.graduation.project.cpa.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CpaProfileResponse {
  UUID id;
  String cpaProfileName;
  String cpaProfileCode;
  String letterCpaScore;
  Double numberCpaScore;
  Double previousNumberCpaScore;
  int accumulatedCredits;
  List<GpaProfileResponse> gpaProfiles;
}
