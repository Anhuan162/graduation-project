package com.graduation.project.library.repository;

import com.graduation.project.library.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

//    ThieuNN
@Query("SELECT distinct s from Subject as s " +
        " inner join SubjectReference as sref on sref.subject = s " +
        " inner join Semester as se on se = sref.semester " +
        " inner join Faculty as fa on sref.faculty = fa " +
        " where (:facultyId is null or fa.id = :facultyId) and " +
        " ( :semesterId is null or se.id = :semesterId)")
public List<Subject> findSubjectByFacultyIdAndSemesterId(
        @Param("facultyId") UUID facultyId,
        @Param("semesterId") UUID semesterId
    );
}
