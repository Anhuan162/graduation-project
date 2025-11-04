package com.graduation.project.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CpaProfileRequest {
  String cpaProfileName;
  String cpaProfileCode;
  double letterCpaScore;
  double numberCpaScore;
  double previousNumberCpaScore;
  int accumulatedCredits;
  List<GpaProfileRequest> gpaProfileRequests;
}
