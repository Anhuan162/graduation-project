package com.graduation.project.library.mapper;

import com.graduation.project.library.dto.SubjectRequest;
import com.graduation.project.library.dto.SubjectResponse;
import com.graduation.project.library.entity.Subject;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class SubjectMapperImpl implements SubjectMapper {

    @Override
    public Subject toSubject(SubjectRequest request) {
        if ( request == null ) {
            return null;
        }

        Subject.SubjectBuilder subject = Subject.builder();

        subject.subjectName( request.getSubjectName() );
        subject.subjectCode( request.getSubjectCode() );
        subject.credit( request.getCredit() );
        subject.description( request.getDescription() );

        subject.createdDate( LocalDateTime.now() );
        subject.lastModifiedDate( LocalDateTime.now() );

        return subject.build();
    }

    @Override
    public SubjectResponse toSubjectResponse(Subject subject) {
        if ( subject == null ) {
            return null;
        }

        SubjectResponse.SubjectResponseBuilder subjectResponse = SubjectResponse.builder();

        subjectResponse.id( subject.getId() );
        subjectResponse.subjectName( subject.getSubjectName() );
        subjectResponse.subjectCode( subject.getSubjectCode() );
        subjectResponse.credit( subject.getCredit() );
        subjectResponse.description( subject.getDescription() );
        subjectResponse.createdDate( subject.getCreatedDate() );
        subjectResponse.lastModifiedDate( subject.getLastModifiedDate() );

        return subjectResponse.build();
    }
}
