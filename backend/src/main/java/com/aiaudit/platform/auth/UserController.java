package com.aiaudit.platform.auth;

import com.aiaudit.platform.auth.dto.UpdateProfileRequest;
import com.aiaudit.platform.auth.dto.UserDto;
import com.aiaudit.platform.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(ApiResponse.success(UserDto.from(user)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getLanguagePreference() != null) user.setLanguagePreference(request.getLanguagePreference());

        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(UserDto.from(user), "Profile updated successfully"));
    }
}
