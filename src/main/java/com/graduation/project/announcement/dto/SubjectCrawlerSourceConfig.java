package com.graduation.project.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCrawlerSourceConfig {
  private String crawlUrl;
  private String facultyUrl;
  private String itemSelector;

  private String subjectNameSelector;
  private String creditSelector;
  private String subjectCodeAttr;
}
