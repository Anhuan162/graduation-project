package com.graduation.project.library.mapper;

import com.graduation.project.library.constant.SemesterType;
import com.graduation.project.library.dto.SemesterRequest;
import com.graduation.project.library.dto.SemesterResponse;
import com.graduation.project.library.entity.Semester;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class SemesterMapperImpl implements SemesterMapper {

    @Override
    public Semester toSemester(SemesterRequest semesterRequest) {
        if ( semesterRequest == null ) {
            return null;
        }

        Semester.SemesterBuilder semester = Semester.builder();

        semester.id( semesterRequest.getId() );
        if ( semesterRequest.getSemesterType() != null ) {
            semester.semesterType( Enum.valueOf( SemesterType.class, semesterRequest.getSemesterType() ) );
        }
        semester.schoolYear( semesterRequest.getSchoolYear() );

        return semester.build();
    }

    @Override
    public SemesterResponse toSemesterResponse(Semester semester) {
        if ( semester == null ) {
            return null;
        }

        SemesterResponse.SemesterResponseBuilder semesterResponse = SemesterResponse.builder();

        semesterResponse.id( semester.getId() );
        if ( semester.getSemesterType() != null ) {
            semesterResponse.semesterType( semester.getSemesterType().name() );
        }
        semesterResponse.schoolYear( semester.getSchoolYear() );

        return semesterResponse.build();
    }
}
