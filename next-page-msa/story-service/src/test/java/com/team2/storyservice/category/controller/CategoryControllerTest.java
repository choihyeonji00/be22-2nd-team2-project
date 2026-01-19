package com.team2.storyservice.category.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.team2.storyservice.category.entity.Category;
import com.team2.storyservice.category.repository.CategoryRepository;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.assertj.core.api.Assertions;
import org.mockito.BDDMockito;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 목록 조회 - 정상")
    void getCategories_Success() {
        // Given
        Category category1 = new Category("ROMANCE", "로맨스");
        Category category2 = new Category("FANTASY", "판타지");

        List<Category> categories = Arrays.asList(category1, category2);
        given(categoryRepository.findAll()).willReturn(categories);

        // When
        var response = categoryController.getCategories();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getCategoryId()).isEqualTo("ROMANCE");
        assertThat(response.getData().get(1).getCategoryId()).isEqualTo("FANTASY");
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 빈 목록")
    void getCategories_EmptyList() {
        // Given
        given(categoryRepository.findAll()).willReturn(Collections.emptyList());

        // When
        var response = categoryController.getCategories();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 단일 카테고리")
    void getCategories_SingleCategory() {
        // Given
        Category category = new Category("MYSTERY", "미스터리");

        given(categoryRepository.findAll()).willReturn(Collections.singletonList(category));

        // When
        var response = categoryController.getCategories();

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCategoryName()).isEqualTo("미스터리");
    }
}
