package com.spring.springbootapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.spring.springbootapplication.entity.LearningData;
import java.util.List;

import com.spring.springbootapplication.dto.SkillsDTO;

import com.spring.springbootapplication.service.LearningDataService;

import jakarta.validation.Valid;

@Controller
public class SkillsController {
    private final LearningDataService learningDataService;

    // ★ コンストラクタ注入（@Autowired は不要）
    public SkillsController(LearningDataService learningDataService) {
        this.learningDataService = learningDataService;
    }

    @GetMapping("/skills")
    public String index(Model model,@RequestParam(name = "month", required = false) String month
    ) {
    Long userId = getLoginUserId();
    var months = learningDataService.pastThreeMonths();
    String selectedMonth = learningDataService.normalizeYm(month);

    model.addAttribute("months", months);                 // 月プルダウン
    model.addAttribute("selectedMonth", selectedMonth);   // "yyyy-MM"
    model.addAttribute("selectedMonthLabel", learningDataService.monthLabel(selectedMonth)); // "M月"

    var skills = learningDataService.listByUserAndYm(userId, selectedMonth);
    model.addAttribute("skills", skills);
    return "skills";
}

    // フォーム表示
    @GetMapping("/skills/new")
    public String showNew(
            Model model,
            @RequestParam String categoryName,
            @RequestParam (required = false) String month) {

        // ✔ 正規化して実際に使う（未使用変数にならないように）
        String normalizedMonth = learningDataService.normalizeYm(month);

        SkillsDTO dto = new SkillsDTO();
        // ✔ hidden の month には正規化後を入れる
        dto.setMonth(normalizedMonth);

        model.addAttribute("userForm", dto);
        model.addAttribute("categoryName", categoryName);
        // ✔ 画面表示・戻りリンク用にも正規化後を使う
        model.addAttribute("selectedMonth", normalizedMonth);
        return "skills-new";
    }

    // 追加保存（ID不要）
    @PostMapping("/skills/new")
public String create(
        @Valid @ModelAttribute("userForm") SkillsDTO form,
        BindingResult br,
        @RequestParam String categoryName,
        Model model) {

    model.addAttribute("categoryName", categoryName);
    model.addAttribute("selectedMonth", form.getMonth()); // 失敗時の再描画で使う

    // ❶ 重複チェックは常に実行（Bean Validation と併存させる）
    String name = form.getName() == null ? "" : form.getName().trim();
    if (!name.isBlank()) {
        boolean dup = learningDataService.existsByUserMonthCategoryAndName(
                getLoginUserId(), form.getMonth(), categoryName, name);
        if (dup) {
            br.rejectValue("name", "duplicate", name + " は既に登録されています");
        }
    }

    // ❷ ここでまとめて判定 → name重複＆studyTimeの両方が同時に出る
    if (br.hasErrors()) {
        return "skills-new";
    }

    // ❸ 保存（念のための二重防御）
    try {
        learningDataService.saveNewByName(getLoginUserId(), categoryName, form);
    } catch (LearningDataService.DuplicateLearningDataException ex) {
        br.rejectValue("name", "duplicate", ex.getName() + " は既に登録されています");
        return "skills-new";
    }

        // モーダル表示用
        model.addAttribute("addedCategory", categoryName);
        model.addAttribute("addedName", form.getName());
        model.addAttribute("addedStudyTime", form.getStudyTime());
        model.addAttribute("selectedMonth", form.getMonth());

        // フォーム初期化（月は残す）
        SkillsDTO empty = new SkillsDTO();
        empty.setMonth(form.getMonth());
        model.addAttribute("userForm", empty);

        return "skills-new";
    }

    private Long getLoginUserId() {
        // ログイン連携前のダミー
        return 1L;
    }
}
