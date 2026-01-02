package com.graduation.project.common.mapper;

import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.entity.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMetadataMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "accessType", source = "accessType") // Map enum -> String tự động
  @Mapping(target = "resourceType", expression = "java(fileMetadata.getResourceType() != null ? fileMetadata.getResourceType().name() : null)")
  @Mapping(target = "fileName", expression = "java(extractDisplayName(fileMetadata.getFileName()))")
  FileMetadataResponse toFileMetadataResponse(FileMetadata fileMetadata);

  default String extractDisplayName(String objectName) {
    if (objectName == null)
      return null;
    // Extract original filename from Firebase object name
    // (folder/UUID_originalname)
    // Note: Firebase generates object names with UUID prefixes (e.g.,
    // UUID.extension),
    // but the fileName field typically stores the original user filename.
    // This method handles cases where Firebase object names might be stored as
    // filenames,
    // or where original filenames contain UUID prefixes for other reasons.
    int lastSlashIndex = objectName.lastIndexOf('/');
    if (lastSlashIndex >= 0 && lastSlashIndex < objectName.length() - 1) {
      objectName = objectName.substring(lastSlashIndex + 1);
    }
    // Extract original filename from UUID_originalname
    // Only strip prefix if it matches UUID pattern to avoid corrupting filenames
    // with legitimate underscores
    if (objectName.contains("_")) {
      int underscoreIndex = objectName.indexOf('_');
      if (underscoreIndex > 0 && underscoreIndex < objectName.length() - 1) {
        String prefix = objectName.substring(0, underscoreIndex);
        if (isValidUUID(prefix)) {
          objectName = objectName.substring(underscoreIndex + 1);
        }
      }
    }
    return objectName;
  }

  /**
   * Checks if the given string is a valid UUID format.
   *
   * @param str the string to check
   * @return true if the string matches UUID regex, false otherwise
   */
  default boolean isValidUUID(String str) {
    if (str == null)
      return false;
    return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
  }
}
