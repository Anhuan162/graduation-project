package com.graduation.project.announcement.controller;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.service.AnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementControllerTest {

        @Mock
        private AnnouncementService announcementService;

        @InjectMocks
        private AnnouncementController announcementController;

        private MockMvc mockMvc;
        private Pageable pageable;
        private AnnouncementResponse announcementResponse;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(announcementController).build();
                pageable = PageRequest.of(0, 10);

                announcementResponse = AnnouncementResponse.builder()
                                .id(UUID.randomUUID())
                                .title("Test Announcement")
                                .content("Test Content")
                                .build();
        }

        @Test
        void searchAnnouncements_ValidType_ShouldReturnResults() throws Exception {
                // Given
                Page<AnnouncementResponse> page = new PageImpl<>(Collections.singletonList(announcementResponse));
                when(announcementService.searchAnnouncements(eq(pageable), eq(AnnouncementType.CLASS_MEETING), isNull(),
                                isNull(), isNull(), isNull(), isNull()))
                                .thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/announcements")
                                .param("type", "class_meeting")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.result.content[0].title").value("Test Announcement"));
        }

        @Test
        void searchAnnouncements_InvalidType_ShouldReturnBadRequest() throws Exception {
                // Given
                String invalidType = "invalid_type";

                // When & Then
                mockMvc.perform(get("/api/announcements")
                                .param("type", invalidType)
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void searchAnnouncements_ValidTypeCaseInsensitive_ShouldWork() throws Exception {
                // Given
                Page<AnnouncementResponse> page = new PageImpl<>(Collections.singletonList(announcementResponse));
                when(announcementService.searchAnnouncements(eq(pageable), eq(AnnouncementType.PAY_FEE), isNull(),
                                isNull(),
                                isNull(), isNull(), isNull()))
                                .thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/announcements")
                                .param("type", "pay_fee")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk());
        }

        @Test
        void searchAnnouncements_ValidTypeLowerCase_ShouldWork() throws Exception {
                // Given
                Page<AnnouncementResponse> page = new PageImpl<>(Collections.singletonList(announcementResponse));
                when(announcementService.searchAnnouncements(eq(pageable), eq(AnnouncementType.GENERAL), isNull(),
                                isNull(),
                                isNull(), isNull(), isNull()))
                                .thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/announcements")
                                .param("type", "general")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk());
        }

        @Test
        void searchAnnouncements_NullType_ShouldWork() throws Exception {
                // Given
                Page<AnnouncementResponse> page = new PageImpl<>(Collections.singletonList(announcementResponse));
                when(announcementService.searchAnnouncements(eq(pageable), isNull(), isNull(), isNull(), isNull(),
                                isNull(), isNull()))
                                .thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/announcements")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk());
        }

        @Test
        void searchAnnouncements_EmptyType_ShouldWork() throws Exception {
                // Given
                Page<AnnouncementResponse> page = new PageImpl<>(Collections.singletonList(announcementResponse));
                when(announcementService.searchAnnouncements(eq(pageable), isNull(), isNull(), isNull(), isNull(),
                                isNull(), isNull()))
                                .thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/announcements")
                                .param("type", "")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk());
        }

        @Test
        void searchAnnouncements_TypeWithWhitespace_ShouldTrimAndValidate() throws Exception {
                // Given
                Page<AnnouncementResponse> page = new PageImpl<>(Collections.singletonList(announcementResponse));
                when(announcementService.searchAnnouncements(eq(pageable), eq(AnnouncementType.CLASS_MEETING), isNull(),
                                isNull(), isNull(), isNull(), isNull()))
                                .thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/announcements")
                                .param("type", "  class_meeting  ")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk());
        }
}
