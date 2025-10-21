package com.graduation.project.user.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnnoucementTargetResponse {
  UUID id;
  String classroomCode;
}
