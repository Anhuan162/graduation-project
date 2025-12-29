package com.graduation.project.cpa.mapper;

import com.graduation.project.cpa.dto.GradeSubjectAverageProfileRequest;
import com.graduation.project.cpa.dto.GradeSubjectAverageProfileResponse;
import com.graduation.project.cpa.entity.GradeSubjectAverageProfile;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.entity.SubjectReference;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class GradeSubjectAverageProfileMapperImpl implements GradeSubjectAverageProfileMapper {

    @Override
    public GradeSubjectAverageProfile toGradeSubjectAverageProfile(GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest) {
        if ( gradeSubjectAverageProfileRequest == null ) {
            return null;
        }

        GradeSubjectAverageProfile.GradeSubjectAverageProfileBuilder gradeSubjectAverageProfile = GradeSubjectAverageProfile.builder();

        if ( gradeSubjectAverageProfileRequest.getId() != null ) {
            gradeSubjectAverageProfile.id( UUID.fromString( gradeSubjectAverageProfileRequest.getId() ) );
        }
        gradeSubjectAverageProfile.letterCurrentScore( gradeSubjectAverageProfileRequest.getLetterCurrentScore() );
        gradeSubjectAverageProfile.letterImprovementScore( gradeSubjectAverageProfileRequest.getLetterImprovementScore() );

        return gradeSubjectAverageProfile.build();
    }

    @Override
    public GradeSubjectAverageProfileResponse toGradeSubjectAverageProfileResponse(GradeSubjectAverageProfile gradeSubjectAverageProfile) {
        if ( gradeSubjectAverageProfile == null ) {
            return null;
        }

        GradeSubjectAverageProfileResponse.GradeSubjectAverageProfileResponseBuilder gradeSubjectAverageProfileResponse = GradeSubjectAverageProfileResponse.builder();

        gradeSubjectAverageProfileResponse.subjectName( gradeSubjectAverageProfileSubjectReferenceSubjectSubjectName( gradeSubjectAverageProfile ) );
        gradeSubjectAverageProfileResponse.subjectCode( gradeSubjectAverageProfileSubjectReferenceSubjectSubjectCode( gradeSubjectAverageProfile ) );
        gradeSubjectAverageProfileResponse.subjectId( gradeSubjectAverageProfileSubjectReferenceSubjectId( gradeSubjectAverageProfile ) );
        gradeSubjectAverageProfileResponse.credit( gradeSubjectAverageProfileSubjectReferenceSubjectCredit( gradeSubjectAverageProfile ) );
        gradeSubjectAverageProfileResponse.id( gradeSubjectAverageProfile.getId() );
        gradeSubjectAverageProfileResponse.letterCurrentScore( gradeSubjectAverageProfile.getLetterCurrentScore() );
        gradeSubjectAverageProfileResponse.letterImprovementScore( gradeSubjectAverageProfile.getLetterImprovementScore() );
        gradeSubjectAverageProfileResponse.currentScore( gradeSubjectAverageProfile.getCurrentScore() );
        gradeSubjectAverageProfileResponse.improvementScore( gradeSubjectAverageProfile.getImprovementScore() );

        return gradeSubjectAverageProfileResponse.build();
    }

    private String gradeSubjectAverageProfileSubjectReferenceSubjectSubjectName(GradeSubjectAverageProfile gradeSubjectAverageProfile) {
        if ( gradeSubjectAverageProfile == null ) {
            return null;
        }
        SubjectReference subjectReference = gradeSubjectAverageProfile.getSubjectReference();
        if ( subjectReference == null ) {
            return null;
        }
        Subject subject = subjectReference.getSubject();
        if ( subject == null ) {
            return null;
        }
        String subjectName = subject.getSubjectName();
        if ( subjectName == null ) {
            return null;
        }
        return subjectName;
    }

    private String gradeSubjectAverageProfileSubjectReferenceSubjectSubjectCode(GradeSubjectAverageProfile gradeSubjectAverageProfile) {
        if ( gradeSubjectAverageProfile == null ) {
            return null;
        }
        SubjectReference subjectReference = gradeSubjectAverageProfile.getSubjectReference();
        if ( subjectReference == null ) {
            return null;
        }
        Subject subject = subjectReference.getSubject();
        if ( subject == null ) {
            return null;
        }
        String subjectCode = subject.getSubjectCode();
        if ( subjectCode == null ) {
            return null;
        }
        return subjectCode;
    }

    private UUID gradeSubjectAverageProfileSubjectReferenceSubjectId(GradeSubjectAverageProfile gradeSubjectAverageProfile) {
        if ( gradeSubjectAverageProfile == null ) {
            return null;
        }
        SubjectReference subjectReference = gradeSubjectAverageProfile.getSubjectReference();
        if ( subjectReference == null ) {
            return null;
        }
        Subject subject = subjectReference.getSubject();
        if ( subject == null ) {
            return null;
        }
        UUID id = subject.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private int gradeSubjectAverageProfileSubjectReferenceSubjectCredit(GradeSubjectAverageProfile gradeSubjectAverageProfile) {
        if ( gradeSubjectAverageProfile == null ) {
            return 0;
        }
        SubjectReference subjectReference = gradeSubjectAverageProfile.getSubjectReference();
        if ( subjectReference == null ) {
            return 0;
        }
        Subject subject = subjectReference.getSubject();
        if ( subject == null ) {
            return 0;
        }
        int credit = subject.getCredit();
        return credit;
    }
}
