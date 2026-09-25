package com.itcinnovation.backend.user;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String department,
        UserRole role,
        UserStatus status
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getDepartment(),
                user.getRole(),
                user.getStatus());
    }
}
