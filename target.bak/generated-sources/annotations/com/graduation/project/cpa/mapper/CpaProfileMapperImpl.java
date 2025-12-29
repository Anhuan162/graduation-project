package com.graduation.project.cpa.mapper;

import com.graduation.project.cpa.dto.CpaProfileRequest;
import com.graduation.project.cpa.dto.CpaProfileResponse;
import com.graduation.project.cpa.dto.GpaProfileRequest;
import com.graduation.project.cpa.dto.GpaProfileResponse;
import com.graduation.project.cpa.entity.CpaProfile;
import com.graduation.project.cpa.entity.GpaProfile;
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
public class CpaProfileMapperImpl implements CpaProfileMapper {

    @Autowired
    private GpaProfileMapper gpaProfileMapper;

    @Override
    public CpaProfile toCpaProfile(CpaProfileRequest cpaProfileRequest) {
        if ( cpaProfileRequest == null ) {
            return null;
        }

        CpaProfile.CpaProfileBuilder cpaProfile = CpaProfile.builder();

        cpaProfile.gpaProfiles( gpaProfileRequestListToGpaProfileList( cpaProfileRequest.getGpaProfileRequests() ) );
        if ( cpaProfileRequest.getId() != null ) {
            cpaProfile.id( UUID.fromString( cpaProfileRequest.getId() ) );
        }
        cpaProfile.cpaProfileName( cpaProfileRequest.getCpaProfileName() );
        cpaProfile.cpaProfileCode( cpaProfileRequest.getCpaProfileCode() );
        cpaProfile.letterCpaScore( cpaProfileRequest.getLetterCpaScore() );
        cpaProfile.numberCpaScore( cpaProfileRequest.getNumberCpaScore() );
        cpaProfile.previousNumberCpaScore( cpaProfileRequest.getPreviousNumberCpaScore() );
        cpaProfile.accumulatedCredits( cpaProfileRequest.getAccumulatedCredits() );

        return cpaProfile.build();
    }

    @Override
    public CpaProfileResponse toCpaProfileResponse(CpaProfile cpaProfile) {
        if ( cpaProfile == null ) {
            return null;
        }

        CpaProfileResponse.CpaProfileResponseBuilder cpaProfileResponse = CpaProfileResponse.builder();

        cpaProfileResponse.gpaProfileResponses( gpaProfileListToGpaProfileResponseList( cpaProfile.getGpaProfiles() ) );
        cpaProfileResponse.id( cpaProfile.getId() );
        cpaProfileResponse.cpaProfileName( cpaProfile.getCpaProfileName() );
        cpaProfileResponse.cpaProfileCode( cpaProfile.getCpaProfileCode() );
        cpaProfileResponse.letterCpaScore( cpaProfile.getLetterCpaScore() );
        cpaProfileResponse.numberCpaScore( cpaProfile.getNumberCpaScore() );
        cpaProfileResponse.previousNumberCpaScore( cpaProfile.getPreviousNumberCpaScore() );
        cpaProfileResponse.accumulatedCredits( cpaProfile.getAccumulatedCredits() );

        return cpaProfileResponse.build();
    }

    @Override
    public CpaProfileResponse toCpaProfileInfoResponse(CpaProfile cpaProfile) {
        if ( cpaProfile == null ) {
            return null;
        }

        CpaProfileResponse.CpaProfileResponseBuilder cpaProfileResponse = CpaProfileResponse.builder();

        cpaProfileResponse.id( cpaProfile.getId() );
        cpaProfileResponse.cpaProfileName( cpaProfile.getCpaProfileName() );
        cpaProfileResponse.cpaProfileCode( cpaProfile.getCpaProfileCode() );
        cpaProfileResponse.letterCpaScore( cpaProfile.getLetterCpaScore() );
        cpaProfileResponse.numberCpaScore( cpaProfile.getNumberCpaScore() );
        cpaProfileResponse.previousNumberCpaScore( cpaProfile.getPreviousNumberCpaScore() );
        cpaProfileResponse.accumulatedCredits( cpaProfile.getAccumulatedCredits() );

        return cpaProfileResponse.build();
    }

    protected List<GpaProfile> gpaProfileRequestListToGpaProfileList(List<GpaProfileRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<GpaProfile> list1 = new ArrayList<GpaProfile>( list.size() );
        for ( GpaProfileRequest gpaProfileRequest : list ) {
            list1.add( gpaProfileMapper.toGpaProfile( gpaProfileRequest ) );
        }

        return list1;
    }

    protected List<GpaProfileResponse> gpaProfileListToGpaProfileResponseList(List<GpaProfile> list) {
        if ( list == null ) {
            return null;
        }

        List<GpaProfileResponse> list1 = new ArrayList<GpaProfileResponse>( list.size() );
        for ( GpaProfile gpaProfile : list ) {
            list1.add( gpaProfileMapper.toGpaProfileResponse( gpaProfile ) );
        }

        return list1;
    }
}
