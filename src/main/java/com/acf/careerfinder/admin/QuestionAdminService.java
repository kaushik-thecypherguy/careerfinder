package com.acf.careerfinder.admin;

import com.acf.careerfinder.model.QItem;
import com.acf.careerfinder.model.QItemLocale;
import com.acf.careerfinder.repository.QItemLocaleRepository;
import com.acf.careerfinder.repository.QItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.acf.careerfinder.admin.AdminQuestionRow;
import com.acf.careerfinder.model.QItem;
import com.acf.careerfinder.model.QItemLocale;
import com.acf.careerfinder.psychometrics.QuestionMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class QuestionAdminService {

    private final QItemRepository qItemRepo;
    private final QItemLocaleRepository qItemLocaleRepo;
    private static final ObjectMapper M = new ObjectMapper();

    public QuestionAdminService(QItemRepository qItemRepo,
                                QItemLocaleRepository qItemLocaleRepo) {
        this.qItemRepo = qItemRepo;
        this.qItemLocaleRepo = qItemLocaleRepo;
    }

    /** Create or update a question, overwriting ALL provided fields and locales. */
    @Transactional
    public void upsert(AdminQuestionDTO dto) {
        if (dto.getQkey() == null || dto.getQkey().isBlank()) {
            throw new IllegalArgumentException("qkey required");
        }
        QItem item = qItemRepo.findByQkey(dto.getQkey()).orElseGet(QItem::new);

        // If new, set qkey
        if (item.getQkey() == null) item.setQkey(dto.getQkey());

        // Core fields
        item.setSectionKey(dto.getSectionKey() == null ? "ipip" : dto.getSectionKey());
        item.setOrderIndex(dto.getOrderIndex() == null ? 0 : dto.getOrderIndex());
        item.setQtype(QItem.QType.valueOf(dto.getQtype() == null ? "SINGLE" : dto.getQtype()));
        item.setRequired(dto.isRequired());
        item.setActive(dto.isActive());
        item.setMetaJson(dto.getMetaJson());

        // Save item, then locales
        item = qItemRepo.save(item);

        upsertLocale(item, "en", dto.getEnQuestion(), dto.getEnOptionsJson());
        upsertLocale(item, "hi", dto.getHiQuestion(), dto.getHiOptionsJson());
        upsertLocale(item, "mr", dto.getMrQuestion(), dto.getMrOptionsJson());
    }

    @Transactional
    public void deleteByQkey(String qkey) {
        QItem item = qItemRepo.findByQkey(qkey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown qkey: " + qkey));
        qItemLocaleRepo.deleteByItemId(item.getId());
        qItemRepo.delete(item);
    }

    // --- helpers ---

    private void upsertLocale(QItem item, String locale, String questionText, String optionsJson) {
        QItemLocale row = qItemLocaleRepo.findByItemIdAndLocale(item.getId(), locale)
                .orElseGet(QItemLocale::new);
        row.setItem(item);
        row.setLocale(locale);

        // Overwrite to whatever the form sends (even empty string)
        if (questionText != null) row.setQuestionText(questionText);
        if (optionsJson != null) row.setOptionsJson(optionsJson);

        qItemLocaleRepo.save(row);
    }

    // Used by controller load/edit
    public java.util.Optional<QItem> findByQkey(String qkey) { return qItemRepo.findByQkey(qkey); }

    public AdminQuestionDTO toDto(QItem item) {
        AdminQuestionDTO dto = new AdminQuestionDTO();
        dto.setQkey(item.getQkey());
        dto.setSectionKey(item.getSectionKey());
        dto.setQtype(item.getQtype().name());
        dto.setOrderIndex(item.getOrderIndex());
        dto.setRequired(Boolean.TRUE.equals(item.getRequired()));
        dto.setActive(Boolean.TRUE.equals(item.getActive()));
        dto.setMetaJson(item.getMetaJson());
        qItemLocaleRepo.findByItemIdAndLocale(item.getId(), "en")
                .ifPresent(l -> { dto.setEnQuestion(l.getQuestionText()); dto.setEnOptionsJson(l.getOptionsJson()); });
        qItemLocaleRepo.findByItemIdAndLocale(item.getId(), "hi")
                .ifPresent(l -> { dto.setHiQuestion(l.getQuestionText()); dto.setHiOptionsJson(l.getOptionsJson()); });
        qItemLocaleRepo.findByItemIdAndLocale(item.getId(), "mr")
                .ifPresent(l -> { dto.setMrQuestion(l.getQuestionText()); dto.setMrOptionsJson(l.getOptionsJson()); });
        return dto;
    }

    public List<AdminQuestionRow> listRows(String locale) {
        List<QItem> items = qItemRepo.findAllOrdered();
        if (items.isEmpty()) return Collections.emptyList();

        List<Long> ids = items.stream().map(QItem::getId).toList();
        Map<Long, QItemLocale> locById = qItemLocaleRepo
                .findByItemIdInAndLocale(ids, locale)
                .stream()
                .collect(Collectors.toMap(r -> r.getItem().getId(), Function.identity(), (a,b)->a));

        List<AdminQuestionRow> rows = new ArrayList<>();
        int i = 1;
        for (QItem q : items) {
            String qtext = Optional.ofNullable(locById.get(q.getId()))
                    .map(QItemLocale::getQuestionText).orElse("");

            String metaSummary = "";
            try {
                if (q.getMetaJson() != null && !q.getMetaJson().isBlank()) {
                    QuestionMeta m = M.readValue(q.getMetaJson(), QuestionMeta.class);
                    if ("IPIP".equalsIgnoreCase(m.kind())) {
                        metaSummary = "IPIP " + (m.domain()==null?"?":m.domain()) +
                                " " + (m.keyed()==null?"":m.keyed());
                    } else if ("SJT".equalsIgnoreCase(m.kind())) {
                        metaSummary = "SJT " + (m.format()==null?"?":m.format()) +
                                " " + (m.trait()==null?"":m.trait());
                    }
                }
            } catch (Exception ignore) {}

            rows.add(new AdminQuestionRow(
                    i++,
                    q.getQkey(),
                    q.getSectionKey(),
                    q.getOrderIndex(),
                    q.getQtype()==null ? "" : q.getQtype().name(),
                    Boolean.TRUE.equals(q.getRequired()),
                    Boolean.TRUE.equals(q.getActive()),
                    metaSummary,
                    qtext
            ));
        }
        return rows;
    }
}