package com.graduation.project.common.mapper;

import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.entity.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMetadataMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "accessType", source = "accessType") // Map enum -> String tự động
  @Mapping(
      target = "resourceType",
      expression =
          "java(fileMetadata.getResourceType() != null ? fileMetadata.getResourceType().name() : null)")
  FileMetadataResponse toFileMetadataResponse(FileMetadata fileMetadata);
}
