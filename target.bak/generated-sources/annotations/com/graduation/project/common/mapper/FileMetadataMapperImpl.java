package com.graduation.project.common.mapper;

import com.graduation.project.common.dto.FileMetadataResponse;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class FileMetadataMapperImpl implements FileMetadataMapper {

    @Override
    public FileMetadataResponse toFileMetadataResponse(FileMetadata fileMetadata) {
        if ( fileMetadata == null ) {
            return null;
        }

        FileMetadataResponse.FileMetadataResponseBuilder fileMetadataResponse = FileMetadataResponse.builder();

        UUID id = fileMetadataUserId( fileMetadata );
        if ( id != null ) {
            fileMetadataResponse.userId( id.toString() );
        }
        if ( fileMetadata.getAccessType() != null ) {
            fileMetadataResponse.accessType( fileMetadata.getAccessType().name() );
        }
        fileMetadataResponse.id( fileMetadata.getId() );
        fileMetadataResponse.fileName( fileMetadata.getFileName() );
        fileMetadataResponse.folder( fileMetadata.getFolder() );
        fileMetadataResponse.url( fileMetadata.getUrl() );
        fileMetadataResponse.contentType( fileMetadata.getContentType() );
        fileMetadataResponse.size( fileMetadata.getSize() );
        fileMetadataResponse.resourceId( fileMetadata.getResourceId() );
        fileMetadataResponse.createdAt( fileMetadata.getCreatedAt() );

        fileMetadataResponse.resourceType( fileMetadata.getResourceType() != null ? fileMetadata.getResourceType().name() : null );

        return fileMetadataResponse.build();
    }

    private UUID fileMetadataUserId(FileMetadata fileMetadata) {
        if ( fileMetadata == null ) {
            return null;
        }
        User user = fileMetadata.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
