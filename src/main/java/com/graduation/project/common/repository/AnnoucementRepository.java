package com.graduation.project.common.repository;

import com.graduation.project.common.entity.Annoucement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnoucementRepository extends JpaRepository<Annoucement, String> {}
