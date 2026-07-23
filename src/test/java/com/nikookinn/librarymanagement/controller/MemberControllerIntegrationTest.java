package com.nikookinn.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class MemberControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberRepository memberRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Member member;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        member = new Member();
        member.setFirstName("Frodo");
        member.setLastName("Baggins");
        member.setEmail("frodo@shire.com");
        member.setMembershipDate(LocalDate.now());
        member = memberRepository.save(member);
    }

    @Test
    void shouldGetAllMembers() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("frodo@shire.com"));
    }

    @Test
    void shouldGetMemberById() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/members/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Frodo"));
    }

    @Test
    void shouldCreateMember() throws Exception {
        // Arrange
        MemberCreateRequest request = new MemberCreateRequest(
                "Harry",
                "Potter",
                "harry@hogwarts.com",
                "123456789"
        );

        // Act & Assert
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("harry@hogwarts.com"));
    }

    @Test
    void shouldUpdateMember() throws Exception {
        // Arrange
        MemberUpdateRequest request = new MemberUpdateRequest(
                "Frodo Updated",
                member.getLastName(),
                member.getEmail(),
                member.getPhone()
        );

        // Act & Assert
        mockMvc.perform(put("/api/members/{id}", member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Frodo Updated"));
    }

    @Test
    void shouldDeleteMember() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/members/{id}", member.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/members/{id}", member.getId()))
                .andExpect(status().isNotFound());
    }
}
