package com.employeehub.employeehub.security.model;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, String role) {}