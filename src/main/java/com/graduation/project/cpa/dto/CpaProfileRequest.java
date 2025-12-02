package com.graduation.project.cpa.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CpaProfileRequest {
  String id;
  String cpaProfileName;
  String cpaProfileCode;
  String letterCpaScore;
  Double numberCpaScore;
  Double previousNumberCpaScore;
  int accumulatedCredits;
  List<GpaProfileRequest> gpaProfileRequests;
}
