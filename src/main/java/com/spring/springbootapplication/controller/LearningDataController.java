package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.service.LearningDataService;
import com.spring.springbootapplication.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.ProcessBuilder.Redirect;
import java.time.YearMonth;
import java.util.List;

@Controller
public class LearningDataController {

    private final LearningDataService learningDataService;
    private final UserService userService;

    public LearningDataController(LearningDataService learningDataService, UserService userService) {
        this.learningDataService = learningDataService;
        this.userService = userService;
    }

    @GetMapping("/skills-legacy")
    public String showSkills(@RequestParam(value = "month", required = false) String month,
                            Authentication auth,
                            Model model) {
        // （必要なら）未ログインを弾く
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // 1) まず selectedMonth を決定（yyyy-MM）
        String selectedMonth = (month == null || month.isBlank())
                ? YearMonth.now().toString()
                : month;

        // 2) プルダウンの候補（月, ラベル）
        List<LearningDataService.MonthOption> months = learningDataService.pastThreeMonths();

        // 3) 表示ラベルをここで決める（見つからなければフォールバック）
        String selectedMonthLabel = months.stream()
                .filter(m -> m.value().equals(selectedMonth))
                .map(LearningDataService.MonthOption::label)
                .findFirst()
                .orElseGet(() -> months.isEmpty() ? "今月" : months.get(0).label());

        // 4) Viewに渡す
        model.addAttribute("months", months);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedMonthLabel", selectedMonthLabel);

        // 画面の他のモデルは必要に応じて
        model.addAttribute("backendSkills", List.of());
        model.addAttribute("frontendSkills", List.of());
        model.addAttribute("infraSkills", List.of());

        return "skills";
    }

    @PostMapping("/skills/update")
    public String updateMinutes(
        @RequestParam Integer id,
        @RequestParam(name = "minutes", defaultValue = "0") int minutes, 
        @RequestParam String month,
        RedirectAttributes ra) {

        Long userId = getLoginUserId();
        var updated = learningDataService.updateMinutes(userId, id, minutes);
        ra.addFlashAttribute("editSuccess", true);
        ra.addFlashAttribute("editedCategory", updated.getCategory().getName());
        ra.addFlashAttribute("editedName", updated.getName());
        ra.addFlashAttribute("editedStudyTime", updated.getStudyTime());

        String normalized = learningDataService.normalizeYm(month);
        return"redirect:/skills?month=" + normalized;
        }

        private Long getLoginUserId() {
        // 認証導入前のダミー実装
        return 1L;
    }
}
