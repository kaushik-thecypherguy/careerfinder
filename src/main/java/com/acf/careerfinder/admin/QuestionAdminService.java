package com.acf.careerfinder.admin;

import com.acf.careerfinder.model.QItem;
import com.acf.careerfinder.model.QItemLocale;
import com.acf.careerfinder.psychometrics.QuestionMeta;
import com.acf.careerfinder.repository.QItemLocaleRepository;
import com.acf.careerfinder.repository.QItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuestionAdminService {

    private final QItemRepository qItemRepo;
    private final QItemLocaleRepository qItemLocaleRepo;
    private static final ObjectMapper M = new ObjectMapper();

    private static final Pattern IPIP = Pattern.compile("^ipip\\.(O|C|E|A|ES)\\.\\d{2}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SJT  = Pattern.compile("^sjt\\.T\\d{2}\\.\\d{2}$", Pattern.CASE_INSENSITIVE);

    public QuestionAdminService(QItemRepository qItemRepo,
                                QItemLocaleRepository qItemLocaleRepo) {
        this.qItemRepo = qItemRepo;
        this.qItemLocaleRepo = qItemLocaleRepo;
    }

    /** Create or update a question, with strong validation. */
    @Transactional
    public void upsert(AdminQuestionDTO dto) {
        if (blank(dto.getQkey())) throw new IllegalArgumentException("qkey required");

        normalizeQkeyAndSection(dto);                 // canonicalize qkey + section
        QuestionMeta meta = parseMeta(dto.getMetaJson());
        QItem.QType qtype = normalizeAndCheckQType(dto, meta); // align qtype with meta, enforce kind vs qkey
        ensureOptionsConsistency(dto, qtype, meta);  // stable values + required sets
        ensureQuestionsPresent(dto);                 // ensure texts exist; hi/mr inherit en

        // Upsert item
        QItem item = qItemRepo.findByQkey(dto.getQkey()).orElseGet(QItem::new);
        if (item.getQkey() == null) item.setQkey(dto.getQkey());
        item.setSectionKey(dto.getSectionKey());
        item.setOrderIndex(dto.getOrderIndex() == null ? 0 : dto.getOrderIndex());
        item.setQtype(qtype);
        item.setRequired(dto.isRequired());
        item.setActive(dto.isActive());
        item.setMetaJson(dto.getMetaJson());
        item = qItemRepo.save(item);

        // Upsert locales (mr/hi inherit English options json when blank)
        upsertLocale(item, "en", dto.getEnQuestion(), dto.getEnOptionsJson());
        upsertLocale(item, "hi",
                orElse(dto.getHiQuestion(), dto.getEnQuestion()),
                orElse(dto.getHiOptionsJson(), dto.getEnOptionsJson()));
        upsertLocale(item, "mr",
                orElse(dto.getMrQuestion(), dto.getEnQuestion()),
                orElse(dto.getMrOptionsJson(), dto.getEnOptionsJson()));
    }

    @Transactional
    public void deleteByQkey(String qkey) {
        QItem item = qItemRepo.findByQkey(qkey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown qkey: " + qkey));
        qItemLocaleRepo.deleteByItemId(item.getId());
        qItemRepo.delete(item);
    }

    // ---------- helpers ----------

    private void upsertLocale(QItem item, String locale, String questionText, String optionsJson) {
        QItemLocale row = qItemLocaleRepo.findByItemIdAndLocale(item.getId(), locale)
                .orElseGet(QItemLocale::new);
        row.setItem(item);
        row.setLocale(locale);
        row.setQuestionText(nonNull(questionText));
        row.setOptionsJson(nonNull(optionsJson));
        qItemLocaleRepo.save(row);
    }

    public Optional<QItem> findByQkey(String qkey) { return qItemRepo.findByQkey(qkey); }

    public AdminQuestionDTO toDto(QItem item) {
        AdminQuestionDTO dto = new AdminQuestionDTO();
        dto.setQkey(item.getQkey());
        dto.setSectionKey(item.getSectionKey());
        dto.setQtype(item.getQtype() == null ? "SINGLE" : item.getQtype().name());
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

    /** List only IPIP/SJT items (active=true), with locale preview; prefer requested lang then fallback to English. */
    public List<AdminQuestionRow> listRows(String locale) {
        List<QItem> items = qItemRepo.findAllOrdered();
        if (items.isEmpty()) return Collections.emptyList();

        // STRICT filter: only ipip/sjt and active
        items = items.stream()
                .filter(q -> q.getSectionKey() != null
                        && (q.getSectionKey().equalsIgnoreCase("ipip")
                        || q.getSectionKey().equalsIgnoreCase("sjt")))
                .filter(q -> Boolean.TRUE.equals(q.getActive()))
                .toList();

        if (items.isEmpty()) return Collections.emptyList();

        List<Long> ids = items.stream().map(QItem::getId).toList();

        Map<Long, QItemLocale> byReq = qItemLocaleRepo
                .findByItemIdInAndLocale(ids, locale)
                .stream().collect(Collectors.toMap(r -> r.getItem().getId(), Function.identity(), (a,b)->a));

        Map<Long, QItemLocale> byEn = qItemLocaleRepo
                .findByItemIdInAndLocale(ids, "en")
                .stream().collect(Collectors.toMap(r -> r.getItem().getId(), Function.identity(), (a,b)->a));

        List<AdminQuestionRow> rows = new ArrayList<>();
        int i = 1;
        for (QItem q : items) {
            QItemLocale loc = byReq.getOrDefault(q.getId(), byEn.get(q.getId()));
            String qtext = (loc != null && !blank(loc.getQuestionText())) ? loc.getQuestionText() : "";
            String metaSummary = summarizeMeta(q.getMetaJson());
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

    // ----- validation/normalization internals -----

    private void normalizeQkeyAndSection(AdminQuestionDTO dto) {
        String qk = dto.getQkey().trim();
        if (qk.toLowerCase().startsWith("ipip.")) {
            String[] p = qk.split("\\.");
            if (p.length != 3) throw new IllegalArgumentException("Bad qkey: " + qk);
            p[0] = "ipip";
            p[1] = p[1].toUpperCase(); // O|C|E|A|ES
            p[2] = String.format("%02d", parseTwoDigits(p[2]));
            qk = String.join(".", p);
            if (!IPIP.matcher(qk).matches()) throw new IllegalArgumentException("Bad IPIP qkey: " + qk);
            dto.setSectionKey("ipip");
        } else if (qk.toLowerCase().startsWith("sjt.")) {
            String[] p = qk.split("\\.");
            if (p.length != 3) throw new IllegalArgumentException("Bad qkey: " + qk);
            p[0] = "sjt";
            p[1] = p[1].toUpperCase(); // Tnn
            p[2] = String.format("%02d", parseTwoDigits(p[2]));
            qk = String.join(".", p);
            if (!SJT.matcher(qk).matches()) throw new IllegalArgumentException("Bad SJT qkey: " + qk);
            dto.setSectionKey("sjt");
        } else {
            throw new IllegalArgumentException("qkey must start with ipip. or sjt.");
        }
        dto.setQkey(qk);
    }

    private int parseTwoDigits(String s) {
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { throw new IllegalArgumentException("Expect two digits, got: " + s); }
    }

    private QuestionMeta parseMeta(String json) {
        if (blank(json)) {
            return new QuestionMeta(null, null, null, null, null, null, null, null, null);
        }
        try { return M.readValue(json, QuestionMeta.class); }
        catch (Exception e) { throw new IllegalArgumentException("Bad meta JSON: " + e.getMessage()); }
    }

    private QItem.QType normalizeAndCheckQType(AdminQuestionDTO dto, QuestionMeta meta) {
        String qk = dto.getQkey();
        boolean isIpip = qk.toLowerCase().startsWith("ipip.");
        boolean isSjt  = qk.toLowerCase().startsWith("sjt.");

        String kind = meta.kind() == null ? "" : meta.kind().trim().toUpperCase();
        String fmt  = meta.format() == null ? "" : meta.format().trim().toUpperCase();

        // Enforce meta.kind matches qkey family
        if (isIpip && !"IPIP".equals(kind)) {
            throw new IllegalArgumentException("For qkey "+qk+", meta.kind must be IPIP.");
        }
        if (isSjt && !"SJT".equals(kind)) {
            throw new IllegalArgumentException("For qkey "+qk+", meta.kind must be SJT.");
        }

        QItem.QType expected =
                "IPIP".equals(kind) ? QItem.QType.SINGLE :
                        ("SJT".equals(kind) && "MULTI_SELECT".equals(fmt)) ? QItem.QType.MULTI :
                                QItem.QType.SINGLE;

        QItem.QType provided;
        try {
            provided = QItem.QType.valueOf(blank(dto.getQtype()) ? expected.name() : dto.getQtype().toUpperCase());
        } catch (Exception e) {
            provided = expected;
        }

        if ("IPIP".equals(kind) && provided != QItem.QType.SINGLE) {
            throw new IllegalArgumentException("IPIP must be SINGLE (Likert).");
        }
        if ("SJT".equals(kind) && "MULTI_SELECT".equals(fmt) && provided != QItem.QType.MULTI) {
            throw new IllegalArgumentException("SJT MULTI_SELECT must be MULTI.");
        }
        dto.setQtype(provided.name());
        return provided;
    }

    private void ensureOptionsConsistency(AdminQuestionDTO dto, QItem.QType qtype, QuestionMeta meta) {
        List<Opt> enOpts = readOptions(dto.getEnOptionsJson());
        List<Opt> hiOpts = readOptions(dto.getHiOptionsJson());
        List<Opt> mrOpts = readOptions(dto.getMrOptionsJson());
        List<String> vEn = values(enOpts);
        List<String> vHi = values(hiOpts);
        List<String> vMr = values(mrOpts);

        if (qtype == QItem.QType.TEXT) {
            if (!vEn.isEmpty() || !vHi.isEmpty() || !vMr.isEmpty()) {
                throw new IllegalArgumentException("TEXT questions must not have options.");
            }
            return;
        }

        if (vEn.isEmpty()) throw new IllegalArgumentException("Options (English) required.");

        // Values across locales must match English (labels can differ)
        if (!vHi.isEmpty() && !vHi.equals(vEn))
            throw new IllegalArgumentException("Hindi option VALUES must match English (labels can differ).");
        if (!vMr.isEmpty() && !vMr.equals(vEn))
            throw new IllegalArgumentException("Marathi option VALUES must match English (labels can differ).");

        // fill in missing locale options with English values
        if (blank(dto.getHiOptionsJson())) dto.setHiOptionsJson(nonNull(dto.getEnOptionsJson()));
        if (blank(dto.getMrOptionsJson())) dto.setMrOptionsJson(nonNull(dto.getEnOptionsJson()));

        // meta-specific sanity
        String kind = meta.kind() == null ? "" : meta.kind().toUpperCase();
        String fmt  = meta.format() == null ? "" : meta.format().toUpperCase();

        if ("IPIP".equals(kind)) {
            if (!(vEn.equals(List.of("1","2","3","4","5")))) {
                throw new IllegalArgumentException("IPIP requires Likert values 1..5.");
            }
            return;
        }

        if (!"SJT".equals(kind)) return;

        if ("YES_NO".equals(fmt)) {
            Set<String> s = new HashSet<>(lower(vEn));
            if (!(s.contains("yes") && s.contains("no")))
                throw new IllegalArgumentException("SJT YES_NO must have values yes/no.");
            String cv = meta.correctValue();
            if (cv != null && !(cv.equalsIgnoreCase("yes") || cv.equalsIgnoreCase("no"))) {
                throw new IllegalArgumentException("correctValue must be 'yes' or 'no'.");
            }
            return;
        }

        if ("SINGLE_BEST".equals(fmt)) {
            if (vEn.size() < 2) throw new IllegalArgumentException("SJT SINGLE_BEST needs at least two options.");
            String cv = meta.correctValue();
            if (cv != null && vEn.stream().noneMatch(v -> v.equalsIgnoreCase(cv))) {
                throw new IllegalArgumentException("correctValue must be one of the option values.");
            }
            return;
        }

        if ("MULTI_SELECT".equals(fmt)) {
            if (vEn.size() < 2 || vEn.size() > 8)
                throw new IllegalArgumentException("SJT MULTI_SELECT supports between 2 and 8 options.");

            Map<String,String> tv = meta.tagByValue();
            if (tv == null || tv.isEmpty())
                throw new IllegalArgumentException("Please provide Meta.tagByValue mapping (E/O/X) for each option.");

            // normalize + validate tags; keys must exactly match values (case-insensitive)
            Map<String,String> tvLower = new HashMap<>();
            for (Map.Entry<String,String> e : tv.entrySet()) {
                tvLower.put(e.getKey()==null?null:e.getKey().toLowerCase(Locale.ROOT), e.getValue());
            }
            Set<String> allowed = Set.of("E","O","X");

            Map<String,String> normalized = new LinkedHashMap<>();
            for (String v : vEn) {
                String tag = tv.get(v);
                if (tag == null) tag = tvLower.get(v.toLowerCase(Locale.ROOT));
                if (tag == null)
                    throw new IllegalArgumentException("Missing tag for option value '" + v + "' in tagByValue.");
                String up = tag.trim().toUpperCase(Locale.ROOT);
                if (!allowed.contains(up))
                    throw new IllegalArgumentException("Invalid tag '" + tag + "' for value '" + v + "'. Allowed: E, O, X.");
                normalized.put(v, up);
            }

            // write back normalized tagByValue to metaJson to persist cleanly
            QuestionMeta newMeta = new QuestionMeta(
                    meta.kind(), meta.domain(), meta.keyed(), meta.scale(),
                    meta.category(), meta.format(), meta.trait(), normalized, meta.correctValue()
            );
            try {
                dto.setMetaJson(M.writeValueAsString(newMeta));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to serialize normalized meta JSON: " + e.getMessage());
            }
        }
    }

    private void ensureQuestionsPresent(AdminQuestionDTO dto) {
        if (blank(dto.getEnQuestion())) throw new IllegalArgumentException("English question text required.");
        if (blank(dto.getHiQuestion())) dto.setHiQuestion(dto.getEnQuestion());
        if (blank(dto.getMrQuestion())) dto.setMrQuestion(dto.getEnQuestion());
    }

    // ---- small utils ----
    private static boolean blank(String s){ return s == null || s.isBlank(); }
    private static String nonNull(String s){ return s == null ? "" : s; }
    private static String orElse(String a, String b){ return blank(a) ? b : a; }

    private record Opt(String value, String label) { }
    private List<Opt> readOptions(String json) {
        if (blank(json)) return List.of();
        try {
            JsonNode n = M.readTree(json);
            if (!n.isArray()) return List.of();
            List<Opt> list = new ArrayList<>();
            for (JsonNode e : n) {
                if (!e.isObject()) continue;
                String v = e.hasNonNull("value") ? e.get("value").asText() : null;
                if (v == null || v.isBlank()) continue;
                String lbl = e.has("label") ? e.get("label").asText() : v;
                list.add(new Opt(v, lbl));
            }
            return list;
        } catch (Exception ignore) {
            return List.of();
        }
    }
    private List<String> values(List<Opt> opts){ return opts.stream().map(o -> o.value).toList(); }
    private List<String> lower(List<String> xs){ return xs.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList(); }

    private String summarizeMeta(String json) {
        try {
            if (blank(json)) return "";
            QuestionMeta m = M.readValue(json, QuestionMeta.class);
            if ("IPIP".equalsIgnoreCase(m.kind())) {
                return "IPIP " + (m.domain()==null?"?":m.domain()) + " " + (m.keyed()==null?"":m.keyed());
            } else if ("SJT".equalsIgnoreCase(m.kind())) {
                return "SJT " + (m.format()==null?"?":m.format()) + " " + (m.trait()==null?"":m.trait());
            }
        } catch (Exception ignore) {}
        return "";
    }
}