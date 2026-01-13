package com.team2.nextpage.category.repository;

import com.team2.nextpage.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 카테고리 Repository
 *
 * @author 정진호
 */
public interface CategoryRepository extends JpaRepository<Category, String> {
}
