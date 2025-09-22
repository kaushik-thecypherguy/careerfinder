package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.QItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QItemRepository extends JpaRepository<QItem, Long> {

    @Query("""
           select q
           from QItem q
           where q.active = true
           order by q.sectionKey asc, q.orderIndex asc
           """)
    List<QItem> findActiveOrdered();
}