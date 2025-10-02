package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.QItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QItemRepository extends JpaRepository<QItem, Long> {

    // KEEP: used by questionnaire & scoring
    @Query("""
           select q
           from QItem q
           where q.active = true
           order by q.sectionKey asc, q.orderIndex asc
           """)
    List<QItem> findActiveOrdered();

    // ADD: used by admin upsert/delete
    Optional<QItem> findByQkey(String qkey);

    @Query("""
       select q
       from QItem q
       order by q.sectionKey asc, q.orderIndex asc
       """)
    java.util.List<com.acf.careerfinder.model.QItem> findAllOrdered();
}