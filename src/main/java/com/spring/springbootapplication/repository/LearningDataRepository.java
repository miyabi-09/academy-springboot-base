package com.spring.springbootapplication.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.springbootapplication.entity.LearningData;
import org.springframework.data.domain.Sort; 

public interface LearningDataRepository extends JpaRepository<LearningData, Integer> {

     // 学習データの重複チェック 同一ユーザー、月、スキル名が既に存在するか確認
    Optional<LearningData> findByUser_IdAndStudyMonthAndName(
        Long userId,
        LocalDate studyMonth,
        String name
    );
    
     // 指定されたカテゴリ名と月の学習データを取得
    List<LearningData> findByUser_IdAndCategory_NameAndStudyMonth(
        Long userId,
        String categoryName,
        LocalDate studyMonth,
        Sort Sort
    );
    

    // 学習時間の更新
    Optional<LearningData> findByIdAndUser_Id(Integer Id,Long userId);

    // 削除処理
    void deleteByIdAndUser_Id(Long id, Long userId);

    // カテゴリごとの各月の合計学習時間を取得
    List<LearningData> findByUser_IdAndStudyMonthBetween(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
    );
}
