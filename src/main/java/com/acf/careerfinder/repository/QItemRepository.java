package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.QItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QItemRepository extends JpaRepository<QItem, Long> {

    // Questionnaire path — ONLY ipip & sjt, in order.
    @Query("""
           select q
           from QItem q
           where q.active = true
             and lower(q.sectionKey) in ('ipip','sjt')
           order by q.sectionKey asc, q.orderIndex asc
           """)
    List<QItem> findActiveOrdered();

    // Admin path (list everything in a stable order)
    @Query("""
           select q
           from QItem q
           order by q.sectionKey asc, q.orderIndex asc
           """)
    List<QItem> findAllOrdered();

    Optional<QItem> findByQkey(String qkey);
}