package com.graduation.project.cpa.mapper;

import com.graduation.project.cpa.dto.GpaProfileRequest;
import com.graduation.project.cpa.dto.GpaProfileResponse;
import com.graduation.project.cpa.dto.GradeSubjectAverageProfileRequest;
import com.graduation.project.cpa.dto.GradeSubjectAverageProfileResponse;
import com.graduation.project.cpa.entity.GpaProfile;
import com.graduation.project.cpa.entity.GradeSubjectAverageProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class GpaProfileMapperImpl implements GpaProfileMapper {

    @Autowired
    private GradeSubjectAverageProfileMapper gradeSubjectAverageProfileMapper;

    @Override
    public GpaProfile toGpaProfile(GpaProfileRequest gpaProfileRequest) {
        if ( gpaProfileRequest == null ) {
            return null;
        }

        GpaProfile.GpaProfileBuilder gpaProfile = GpaProfile.builder();

        gpaProfile.gradeSubjectAverageProfiles( gradeSubjectAverageProfileRequestListToGradeSubjectAverageProfileList( gpaProfileRequest.getGradeSubjectAverageProfileRequests() ) );
        if ( gpaProfileRequest.getId() != null ) {
            gpaProfile.id( UUID.fromString( gpaProfileRequest.getId() ) );
        }
        gpaProfile.gpaProfileCode( gpaProfileRequest.getGpaProfileCode() );
        if ( gpaProfileRequest.getLetterGpaScore() != null ) {
            gpaProfile.letterGpaScore( String.valueOf( gpaProfileRequest.getLetterGpaScore() ) );
        }
        gpaProfile.numberGpaScore( gpaProfileRequest.getNumberGpaScore() );
        gpaProfile.previousNumberGpaScore( gpaProfileRequest.getPreviousNumberGpaScore() );
        gpaProfile.passedCredits( gpaProfileRequest.getPassedCredits() );

        return gpaProfile.build();
    }

    @Override
    public GpaProfileResponse toGpaProfileResponse(GpaProfile gpaProfile) {
        if ( gpaProfile == null ) {
            return null;
        }

        GpaProfileResponse.GpaProfileResponseBuilder gpaProfileResponse = GpaProfileResponse.builder();

        gpaProfileResponse.gradeSubjectAverageProfileResponses( gradeSubjectAverageProfileListToGradeSubjectAverageProfileResponseList( gpaProfile.getGradeSubjectAverageProfiles() ) );
        gpaProfileResponse.id( gpaProfile.getId() );
        gpaProfileResponse.gpaProfileCode( gpaProfile.getGpaProfileCode() );
        gpaProfileResponse.letterGpaScore( gpaProfile.getLetterGpaScore() );
        gpaProfileResponse.numberGpaScore( gpaProfile.getNumberGpaScore() );
        gpaProfileResponse.previousNumberGpaScore( gpaProfile.getPreviousNumberGpaScore() );
        gpaProfileResponse.passedCredits( gpaProfile.getPassedCredits() );

        return gpaProfileResponse.build();
    }

    protected List<GradeSubjectAverageProfile> gradeSubjectAverageProfileRequestListToGradeSubjectAverageProfileList(List<GradeSubjectAverageProfileRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<GradeSubjectAverageProfile> list1 = new ArrayList<GradeSubjectAverageProfile>( list.size() );
        for ( GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest : list ) {
            list1.add( gradeSubjectAverageProfileMapper.toGradeSubjectAverageProfile( gradeSubjectAverageProfileRequest ) );
        }

        return list1;
    }

    protected List<GradeSubjectAverageProfileResponse> gradeSubjectAverageProfileListToGradeSubjectAverageProfileResponseList(List<GradeSubjectAverageProfile> list) {
        if ( list == null ) {
            return null;
        }

        List<GradeSubjectAverageProfileResponse> list1 = new ArrayList<GradeSubjectAverageProfileResponse>( list.size() );
        for ( GradeSubjectAverageProfile gradeSubjectAverageProfile : list ) {
            list1.add( gradeSubjectAverageProfileMapper.toGradeSubjectAverageProfileResponse( gradeSubjectAverageProfile ) );
        }

        return list1;
    }
}
