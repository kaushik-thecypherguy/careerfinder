package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.UserProgress;
import com.acf.careerfinder.model.UserProgress.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUserEmailAndSection(String userEmail, Section section);
}