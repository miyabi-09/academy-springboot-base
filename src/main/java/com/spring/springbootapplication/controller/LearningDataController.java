package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.dto.SkillsDTO;
import com.spring.springbootapplication.service.LearningDataService;
import com.spring.springbootapplication.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

<<<<<<< HEAD
=======
import java.time.YearMonth;
>>>>>>> 7ffd22b (fix(delete): 削除ボタンの連打防止)
import java.util.List;

@Controller
public class LearningDataController {

    private final LearningDataService learningDataService;
    private final UserService userService;

    public LearningDataController(LearningDataService learningDataService, UserService userService) {
        this.learningDataService = learningDataService;
        this.userService = userService;
    }

    // （任意）古い /skills へのリンクを救済
    @GetMapping("/skills")
    public String redirectSkills() {
        return "redirect:/skills-legacy";
    }

    // 一覧（従来UI）
    @GetMapping("/skills-legacy")
    public String showSkills(
            @RequestParam(value = "month", required = false) String month,
            @AuthenticationPrincipal(expression = "username") String email,
            Model model) {

        if (email == null) return "redirect:/login";

        String selectedMonth = learningDataService.normalizeYm(month);
        Long userId = userService.findIdByEmail(email);

        var skills = learningDataService.listByUserAndYm(userId, selectedMonth);
        model.addAttribute("skills", skills);

<<<<<<< HEAD
=======

        
        // ★ 2) skills を必ず取得してモデルへ（nullを渡さない！）
        Long userId = getLoginUserId(); // 実装があるなら置き換え
        var skills = learningDataService.listByUserAndYm(userId, selectedMonth);
        model.addAttribute("skills", skills);

        // 2) プルダウンの候補（月, ラベル）
>>>>>>> 7ffd22b (fix(delete): 削除ボタンの連打防止)
        List<LearningDataService.MonthOption> months = learningDataService.pastThreeMonths();
        model.addAttribute("months", months);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedMonthLabel", learningDataService.monthLabel(selectedMonth));

        return "skills";
    }

    // 学習時間の保存（更新）
    @PostMapping("/skills/update")
    public String updateMinutes(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam Integer id,
            @RequestParam String month,               
            @RequestParam(name = "minutes", defaultValue = "0") int minutes,
            RedirectAttributes ra) {

        Long userId = userService.findIdByEmail(email);
        int safe = Math.max(0, Math.min(minutes, 1440)); 
        var updated = learningDataService.updateMinutes(userId, id, safe);
        ra.addFlashAttribute("editSuccess", true);
        ra.addFlashAttribute("editedCategory", updated.getCategory().getName());
        ra.addFlashAttribute("editedName", updated.getName());
<<<<<<< HEAD
        ra.addFlashAttribute("editedMinutes", safe); 
        return "redirect:/skills-legacy?month=" + learningDataService.normalizeYm(month);
=======
        ra.addFlashAttribute("editedStudyTime", updated.getStudyTime());

        String normalized = learningDataService.normalizeYm(month);
        return "redirect:/skills-legacy?month=" + normalized;
        }

        private Long getLoginUserId() {
        // 認証導入前のダミー実装
        return 1L;
>>>>>>> 7ffd22b (fix(delete): 削除ボタンの連打防止)
    }

    // 学習時間の削除
    @PostMapping("/skills/delete")
    public String delete(
<<<<<<< HEAD
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam Integer id,
            @RequestParam String month,
            RedirectAttributes ra) {

        Long userId = userService.findIdByEmail(email);
        var target = learningDataService.deleteByIdForUser(userId, id);

        ra.addFlashAttribute("deleteSuccess", true);
        ra.addFlashAttribute("deletedCategory", target.getCategory().getName());
        ra.addFlashAttribute("deletedName", target.getName());
        return "redirect:/skills-legacy?month=" + learningDataService.normalizeYm(month);
    }

    // ★ 新規フォーム表示（ここで必ず form を詰める）
    @GetMapping("/skills/new")
    public String newSkill(
            @RequestParam String categoryName,
            @RequestParam(required = false) String month,
            Model model
    ) {
        String normalized = learningDataService.normalizeYm(month);
        SkillsDTO form = new SkillsDTO();
        form.setMonth(normalized);           // hiddenにセット（skills-new.html の *{month} 用）

        model.addAttribute("categoryName", categoryName);
        model.addAttribute("form", form);    // ★ skills-new.html と一致（th:object="${form}"）
        return "skills-new";
    }

    // ★ 新規作成
    @PostMapping("/skills/create")
    public String create(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam String categoryName,
            @ModelAttribute("form") SkillsDTO form,
            RedirectAttributes ra
    ) {
        Long userId = userService.findIdByEmail(email);
        learningDataService.saveNewByName(userId, categoryName, form);

    ra.addFlashAttribute("editSuccess", true);
    ra.addFlashAttribute("editedCategory", categoryName);
    ra.addFlashAttribute("editedName", form.getName());
    ra.addFlashAttribute("editedMinutes", form.getStudyTime());

        return "redirect:/skills-legacy?month=" + learningDataService.normalizeYm(form.getMonth());
    }
=======
        @RequestParam Integer id,
        @RequestParam(required = false) String month,
        RedirectAttributes ra) {

        Long userId = getLoginUserId();
       // サービスは「存在すれば削除して、表示用の名前を返す」「無ければ空」を返す想定
    var infoOpt = learningDataService.deleteByIdForUser(userId, id);

    // フラッシュメッセージ（存在しなくても成功扱いにする）
    ra.addFlashAttribute("deleteSuccess", infoOpt.isPresent());
    infoOpt.ifPresent(info -> {
        ra.addFlashAttribute("deletedCategory", info.category());
        ra.addFlashAttribute("deletedName", info.name());
    });

    // month が null でも落ちないように
    String normalized = learningDataService.normalizeYm(month);
    return  "redirect:/skills-legacy?month=" + normalized;
    
>>>>>>> 7ffd22b (fix(delete): 削除ボタンの連打防止)
}
}