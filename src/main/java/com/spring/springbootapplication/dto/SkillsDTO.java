package com.spring.springbootapplication.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SkillsDTO {

    @NotBlank(message = "項目名は必ず入力してください")
    @Size(max = 50, message = "項目名は50文字以内で入力してください")
    private String name;

    @NotNull(message = "学習時間は必ず入力してください")
    @Min(value = 0, message = "学習時間は0以上の数字で入力してください")
    private Integer studyTime;

    // プルダウンで選択中の月を "YYYY-MM" で必ず渡す（例: "2025-09"）
    @NotBlank
    
    private String month;

    // ----- getter・setter -----
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getStudyTime() {
        return studyTime;
    }
    public void setStudyTime(Integer studyTime) {
        this.studyTime = studyTime;
    }

    public String getMonth() { 
        return month;
    }
    public void setMonth(String month) { 
        this.month = month; 
    }
}
