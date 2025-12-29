package com.graduation.project.announcement.mapper;

import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.dto.FullAnnouncementResponse;
import com.graduation.project.announcement.entity.Announcement;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class AnnouncementMapperImpl implements AnnouncementMapper {

    @Override
    public AnnouncementResponse toResponse(Announcement entity) {
        if ( entity == null ) {
            return null;
        }

        AnnouncementResponse.AnnouncementResponseBuilder announcementResponse = AnnouncementResponse.builder();

        announcementResponse.id( entity.getId() );
        announcementResponse.title( entity.getTitle() );
        announcementResponse.content( entity.getContent() );
        announcementResponse.announcementType( entity.getAnnouncementType() );
        announcementResponse.announcementStatus( entity.getAnnouncementStatus() );
        announcementResponse.createdDate( entity.getCreatedDate() );
        announcementResponse.modifiedDate( entity.getModifiedDate() );

        announcementResponse.createdBy( entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : "System" );
        announcementResponse.modifiedBy( entity.getModifiedBy() != null ? entity.getModifiedBy().getFullName() : null );

        return announcementResponse.build();
    }

    @Override
    public FullAnnouncementResponse toFullResponse(Announcement entity) {
        if ( entity == null ) {
            return null;
        }

        FullAnnouncementResponse.FullAnnouncementResponseBuilder fullAnnouncementResponse = FullAnnouncementResponse.builder();

        fullAnnouncementResponse.announcementTargetResponses( mapTargets( entity.getTargets() ) );
        fullAnnouncementResponse.id( entity.getId() );
        fullAnnouncementResponse.title( entity.getTitle() );
        fullAnnouncementResponse.content( entity.getContent() );
        fullAnnouncementResponse.createdDate( entity.getCreatedDate() );
        fullAnnouncementResponse.modifiedDate( entity.getModifiedDate() );
        fullAnnouncementResponse.announcementStatus( entity.getAnnouncementStatus() );
        fullAnnouncementResponse.announcementType( entity.getAnnouncementType() );

        fullAnnouncementResponse.createdByFullName( entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : "System" );
        fullAnnouncementResponse.modifiedByFullName( entity.getModifiedBy() != null ? entity.getModifiedBy().getFullName() : null );

        return fullAnnouncementResponse.build();
    }
}
