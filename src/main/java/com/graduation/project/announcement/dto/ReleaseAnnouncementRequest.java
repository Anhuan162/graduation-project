package com.graduation.project.announcement.dto;

import java.util.List;
import java.util.UUID;

import com.graduation.project.cpa.constant.CohortCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseAnnouncementRequest {
  List<UUID> facultyIds;
  List<String> classCodes;
  List<CohortCode> schoolYearCodes;
}
