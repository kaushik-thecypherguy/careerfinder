package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.QItemLocale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QItemLocaleRepository extends JpaRepository<QItemLocale, Long> {

    List<QItemLocale> findByItemIdInAndLocale(Collection<Long> itemIds, String locale);

    Optional<QItemLocale> findByItemIdAndLocale(Long itemId, String locale);

    @Modifying
    @Transactional
    @Query("delete from QItemLocale l where l.item.id = :itemId")
    void deleteByItemId(Long itemId);  // used by admin delete

    /**
     * Helper for "locale with fallback to English".
     * Returns rows for either the requested lang or 'en', ordered so that
     * the requested lang (if present) comes first for each item id.
     */
    @Query("""
        select l
        from QItemLocale l
        where l.item.id in :ids and l.locale in (:lang, 'en')
        order by l.item.id, case when l.locale = :lang then 0 else 1 end
    """)
    List<QItemLocale> findPreferredLocales(Collection<Long> ids, String lang);
}