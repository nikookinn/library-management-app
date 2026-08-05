package com.nikookinn.librarymanagement.controller.api;

import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.MemberResponse;
import com.nikookinn.librarymanagement.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Member", description = "Operations for managing library members")
public interface MemberApi {

    @Operation(summary = "List all members")
    ResponseEntity<Page<MemberResponse>> getAllMembers(Pageable pageable);

    @Operation(summary = "Get member by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "404", description = "Member not found",
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<MemberResponse> getMemberById(Long id);

    @Operation(summary = "Create a new member")
    ResponseEntity<MemberResponse> createMember(MemberCreateRequest request);

    @Operation(summary = "Update a member")
    ResponseEntity<MemberResponse> updateMember(Long id, MemberUpdateRequest request);

    @Operation(summary = "Delete a member")
    ResponseEntity<Void> deleteMember(Long id);

    @Operation(summary = "Search members by name")
    ResponseEntity<Page<MemberResponse>> searchByName(String name, Pageable pageable);

    @Operation(summary = "Search members by email")
    ResponseEntity<Page<MemberResponse>> searchByEmail(String email, Pageable pageable);

    @Operation(summary = "Get members with overdue loans")
    ResponseEntity<List<MemberResponse>> getMembersWithOverdueLoans();
}
