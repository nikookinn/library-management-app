package com.nikookinn.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Member Controller Integration Tests")
class MemberControllerIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("should get all library members successfully")
    void shouldGetAllMembersSuccessfully() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("frodo@shire.com"));
    }

    @Test
    @DisplayName("should get member details by their id")
    void shouldGetMemberByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/members/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Frodo"));
    }

    @Test
    @DisplayName("should create a new library member")
    void shouldCreateMemberSuccessfully() throws Exception {
        MemberCreateRequest request = new MemberCreateRequest(
                "Harry",
                "Potter",
                "harry@hogwarts.com",
                "123456789"
        );

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("harry@hogwarts.com"));
    }

    @Test
    @DisplayName("should update member information")
    void shouldUpdateMemberSuccessfully() throws Exception {
        MemberUpdateRequest request = new MemberUpdateRequest(
                "Frodo Updated",
                member.getLastName(),
                member.getEmail(),
                member.getPhone()
        );

        mockMvc.perform(put("/api/members/{id}", member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Frodo Updated"));
    }

    @Test
    @DisplayName("should delete member from system")
    void shouldDeleteMemberSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/members/{id}", member.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/members/{id}", member.getId()))
                .andExpect(status().isNotFound());
    }
}
