package com.acf.careerfinder.service;

import com.acf.careerfinder.model.Recommendation;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ChatService chatService;
    private final QuestionnaireService questionnaireService; // if you need it elsewhere; safe to remove if unused

    public RecommendationService(ChatService chatService, QuestionnaireService questionnaireService) {
        this.chatService = chatService;
        this.questionnaireService = questionnaireService;
    }

    public Recommendation compute(Map<String, String> answers) {
        // 1) Ask GPT for the full Recommendation, in the user's language.
        Recommendation rec = chatService.askRecommendation(LocaleContextHolder.getLocale(), answers);

        // 2) If GPT didn't provide careers, compute a minimal fallback using your old logic.
        if (rec.getSuggestedCareers() == null || rec.getSuggestedCareers().isEmpty()) {
            rec.setSuggestedCareers(fallbackCareers(answers));
        }

        // 3) If GPT didn't provide next steps, add a tiny fallback.
        if (rec.getNextSteps() == null || rec.getNextSteps().isEmpty()) {
            rec.setNextSteps(List.of(
                    "✅ Review your answers and update the questionnaire."
            ));
        }

        // 4) If GPT didn't send answersEcho, build a plain echo (uses raw keys).
        if (rec.getAnswersEcho() == null || rec.getAnswersEcho().isEmpty()) {
            List<Map.Entry<String,String>> echo = new ArrayList<>();
            if (answers != null) {
                List<String> order = Arrays.asList("q.age", "q.caste", "q.education", "q.employment", "q.goal", "q.mode", "q.sectors");
                for (String k : order) if (answers.containsKey(k)) echo.add(Map.entry(k, answers.get(k)));
                answers.forEach((k, v) -> { if (order.stream().noneMatch(o -> o.equals(k))) echo.add(Map.entry(k, v)); });
            }
            rec.setEchoAnswers(echo);
        }

        // Titles: keep what AI sent, else defaults embedded in the model are fine.
        if (rec.getTitle() == null || rec.getTitle().isBlank()) rec.setTitle("Your result");
        if (rec.getSummary() == null) rec.setSummary("");

        return rec;
    }

    /* ---------------- fallback careers (very light) ---------------- */

    private List<Recommendation.Link> fallbackCareers(Map<String, String> answers) {
        Set<String> sectors = new LinkedHashSet<>();
        String csv = answers != null ? answers.getOrDefault("q.sectors", "") : "";
        Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).forEach(sectors::add);

        List<Recommendation.Link> out = new ArrayList<>();
        if (sectors.contains("it")) out.add(new Recommendation.Link("MS Office / Digital Basics", "https://www.google.com/search?q=MS+Office+course"));
        if (sectors.contains("electrical")) out.add(new Recommendation.Link("Electrician Assistant", "https://www.google.com/search?q=electrician+assistant+course"));
        if (sectors.contains("healthcare")) out.add(new Recommendation.Link("General Duty Assistant (GDA)", "https://www.google.com/search?q=GDA+course"));
        if (sectors.contains("manufacturing")) out.add(new Recommendation.Link("Lab / Field Technician", "https://www.google.com/search?q=lab+field+technician+course"));
        if (out.isEmpty()) out.add(new Recommendation.Link("No‑code tools (Sheets, Apps)", "https://www.google.com/search?q=no+code+tools+basics"));
        return out.stream().limit(6).collect(Collectors.toList());
    }
}