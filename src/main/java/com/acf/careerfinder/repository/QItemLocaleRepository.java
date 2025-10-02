package com.acf.careerfinder.repository;

import com.acf.careerfinder.model.QItemLocale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QItemLocaleRepository extends JpaRepository<QItemLocale, Long> {

    List<QItemLocale> findByItemIdInAndLocale(Collection<Long> itemIds, String locale);

    Optional<QItemLocale> findByItemIdAndLocale(Long itemId, String locale);

    @Modifying
    void deleteByItemId(Long itemId);  // used by admin delete
}