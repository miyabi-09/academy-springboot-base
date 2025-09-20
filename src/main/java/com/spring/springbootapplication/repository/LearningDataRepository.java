package com.spring.springbootapplication.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spring.springbootapplication.dto.CategoryTotalDTO;
import com.spring.springbootapplication.entity.LearningData;

public interface LearningDataRepository extends JpaRepository<LearningData, Integer> {

    // 単一取得（例）
    Optional<LearningData> findByUser_IdAndStudyMonthAndName(
        Long userId,
        LocalDate studyMonth,
        String name
    );

    // 一覧（カテゴリ名指定＋月）
    List<LearningData> findByUser_IdAndCategory_NameAndStudyMonth(
        Long userId,
        String categoryName,
        LocalDate studyMonth,
        Sort sort
    );

    // ==== 重複チェック（カテゴリ無視：ユーザー × 月 × 項目名）====
    boolean existsByUser_IdAndStudyMonthAndName(
        Long userId,
        LocalDate studyMonth,
        String name
    );

    // ==== 重複チェック（カテゴリ込み：ユーザー × 月 × カテゴリ名 × 項目名・大文字小文字無視）====
    boolean existsByUser_IdAndCategory_NameAndStudyMonthAndNameIgnoreCase(
        Long userId,
        String categoryName,
        LocalDate studyMonth,
        String name
    );

    // 更新用
    Optional<LearningData> findByIdAndUser_Id(Integer id, Long userId);

    // 削除用（EntityのIDがIntegerなので合わせる）
    void deleteByIdAndUser_Id(Integer id, Long userId);

    // 月範囲
    List<LearningData> findByUser_IdAndStudyMonthBetween(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
    );
    // 一覧（Service.listByUserAndYm が呼ぶやつ）
List<LearningData> findByUser_IdAndStudyMonthOrderByIdDesc(
    Long userId,
    LocalDate studyMonth
);
// グラフ用：カテゴリ別合計（DTOを返す）← これが肝
@Query("""
    select new com.spring.springbootapplication.dto.CategoryTotalDTO(
      ld.category.name,
      coalesce(sum(ld.studyTime), 0)
    )
    from LearningData ld
    where ld.user.id = :userId
      and ld.studyMonth >= :monthStart
      and ld.studyMonth < :monthEnd
    group by ld.category.name
    order by ld.category.name
  """)

    // 月の一覧（ID降順）
    List<CategoryTotalDTO> findCategoryTotalsByUserAndMonthRange(
    @Param("userId") Long userId,
    @Param("monthStart") LocalDate monthStart,
    @Param("monthEnd") LocalDate monthEnd
);
}
