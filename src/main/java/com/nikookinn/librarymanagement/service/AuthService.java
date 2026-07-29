package com.nikookinn.librarymanagement.service;

import com.nikookinn.librarymanagement.dto.request.LoginRequest;
import com.nikookinn.librarymanagement.dto.request.RegisterRequest;
import com.nikookinn.librarymanagement.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
