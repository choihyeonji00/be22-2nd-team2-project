package com.team2.nextpage.view.controller;

import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("메인 페이지 뷰 반환 테스트")
    void mainPage_ReturnView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("마이페이지 뷰 반환 테스트")
    void mypage_ReturnView() throws Exception {
        mockMvc.perform(get("/mypage"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage"));
    }

    @Test
    @DisplayName("웹소켓 테스트 페이지 뷰 반환 테스트")
    void websocketTest_ReturnView() throws Exception {
        mockMvc.perform(get("/websocket-test"))
                .andExpect(status().isOk())
                .andExpect(view().name("websocket-test"));
    }

    @Test
    @DisplayName("소설 생성 페이지 뷰 반환 테스트")
    void createBook_ReturnView() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-book"));
    }

    @Test
    @DisplayName("소설 상세 페이지 뷰 반환 테스트")
    void bookDetail_ReturnView() throws Exception {
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-detail"));
    }

    @Test
    @DisplayName("소설 뷰어 페이지 뷰 반환 테스트")
    void viewer_ReturnView() throws Exception {
        mockMvc.perform(get("/books/1/viewer"))
                .andExpect(status().isOk())
                .andExpect(view().name("viewer"));
    }
}