package com.spring.springbootapplication.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.springbootapplication.dto.SkillsDTO;
import com.spring.springbootapplication.entity.Category;
import com.spring.springbootapplication.entity.LearningData;
import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.repository.CategoryRepository;
import com.spring.springbootapplication.repository.LearningDataRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class LearningDataService {

    private final LearningDataRepository repo;
    private final CategoryRepository categoryRepo;

    @PersistenceContext
    private EntityManager em;

    public LearningDataService(LearningDataRepository repo, CategoryRepository categoryRepo) {
        this.repo = repo;
        this.categoryRepo = categoryRepo;
    }

    // ---- 月セレクト用 ----
    private static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");
    private static final DateTimeFormatter MONTH_ONLY_LABEL_FMT = DateTimeFormatter.ofPattern("M月");

    /** value= "yyyy-MM", label= "M月" */
    public record MonthOption(String value, String label) {}

    /** 今月から過去2か月（計3か月）を新しい順で返す */
    @Transactional(readOnly = true)
    public List<MonthOption> pastThreeMonths() {
        YearMonth base = YearMonth.now(ZONE_TOKYO);
        return IntStream.range(0, 3)
                .mapToObj(i -> base.minusMonths(i))
                .map(ym -> new MonthOption(
                        ym.toString(),                      // 例: "2025-09"
                        MONTH_ONLY_LABEL_FMT.format(ym.atDay(1)) // "M月"
                ))
                .toList();
    }

    /** 一覧取得（ユーザー×月） */
    @Transactional(readOnly = true)
    public List<LearningData> listByUserAndYm(Long userId, String ym) {
        LocalDate monthStart = toStudyMonth(ym); // "yyyy-MM" → その月の1日
        return repo.findByUser_IdAndStudyMonthOrderByIdDesc(userId, monthStart);
    }

    /** コントローラから呼ぶ：カテゴリ「名」で保存する版 */
    public LearningData saveNewByName(Long userId, String categoryName, SkillsDTO form) {
        // "yyyy-MM" → 月初 LocalDate（不正なら今月にフォールバック）
        LocalDate studyMonth = toStudyMonth(form.getMonth());
        String trimmed = form.getName() == null ? "" : form.getName().trim();

        // 同一ユーザー×同一月×同一項目名の重複を禁止（カテゴリ無視）
        if (!trimmed.isEmpty() && repo.existsByUser_IdAndStudyMonthAndName(userId, studyMonth, trimmed)) {
            throw new DuplicateLearningDataException(trimmed);
        }

        // カテゴリ名 → ID 解決
        Integer categoryId = categoryRepo.findByName(categoryName)
                .map(Category::getId)
                .orElseThrow(() -> new IllegalArgumentException("カテゴリが見つかりません: " + categoryName));

        // 保存（FKは getReference でOK）
        LearningData ld = new LearningData();
        ld.setUser(em.getReference(User.class, userId));
        ld.setCategory(em.getReference(Category.class, categoryId));
        ld.setName(trimmed);
        ld.setStudyTime(form.getStudyTime());
        ld.setStudyMonth(studyMonth);

        return repo.save(ld);
    }

    /** 入力がnull/不正なら今月にフォールバックして yyyy-MM を返す（東京TZ） */
    @Transactional(readOnly = true)
    public String normalizeYm(String ym) {
        try {
            return (ym == null || ym.isBlank())
                    ? YearMonth.now(ZONE_TOKYO).toString()
                    : YearMonth.parse(ym).toString();
        } catch (DateTimeParseException ex) {
            return YearMonth.now(ZONE_TOKYO).toString();
        }
    }

    /** "yyyy-MM" -> LocalDate(yyyy-MM-01)（不正なら今月1日） */
    @Transactional(readOnly = true)
    public LocalDate toStudyMonth(String ym) {
        return YearMonth.parse(normalizeYm(ym)).atDay(1);
    }

    /** 画面表示用の "M月" ラベル */
    @Transactional(readOnly = true)
    public String monthLabel(String ym) {
        String norm = normalizeYm(ym); // "yyyy-MM"
        return MONTH_ONLY_LABEL_FMT.format(YearMonth.parse(norm).atDay(1));
    }

    /** 追加バリデーション用：ユーザー×月×カテゴリ×項目名の重複チェック（大文字小文字無視） */
    @Transactional(readOnly = true)
    public boolean existsByUserMonthCategoryAndName(Long userId, String month, String categoryName, String name) {
        if (name == null || name.isBlank()) return false;
        LocalDate studyMonth = toStudyMonth(month);
        return repo.existsByUser_IdAndCategory_NameAndStudyMonthAndNameIgnoreCase(
                userId,
                categoryName,
                studyMonth,
                name.trim()
        );
    }

    // ====== 重複時に投げる簡易例外 ======
    public static class DuplicateLearningDataException extends RuntimeException {
        private final String name;
        public DuplicateLearningDataException(String name) {
            super("Duplicate: " + name);
            this.name = name;
        }
        public String getName() { return name; }
    }

    // 学習時間を更新
public LearningData updateMinutes(Long userId, Integer id, int minutes) {
    int safe = Math.max(0, Math.min(minutes, 1440)); // 0〜1440に丸める
    LearningData ld = repo.findByIdAndUser_Id(id, userId)
        .orElseThrow(() -> new IllegalArgumentException("データが見つかりません (id=" + id + ")"));
    ld.setStudyTime(safe);
    return repo.save(ld);
}

}
