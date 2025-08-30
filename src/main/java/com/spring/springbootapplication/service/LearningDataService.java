package com.spring.springbootapplication.service;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
public class LearningDataService {

    // プルダウン用：「yyyy-MM」「M月」のペア
    public record MonthOption(String value, String label) {}

    /// LearningDataService
    public List<MonthOption> pastThreeMonths() {
        YearMonth now = YearMonth.now();
        return java.util.stream.IntStream.range(0, 3) // 0,1,2
                .mapToObj(i -> {
                    YearMonth ym = now.minusMonths(i); // 今月, 先月, 先々月
                    String value = ym.toString();                // "2025-08"
                    String label = ym.getMonthValue() + "月";     // "8月"
                    return new MonthOption(value, label);
                })
                .toList();
    }


    // ここから下に、あとで一覧/追加/更新/削除などのメソッドを足していけばOK
}
