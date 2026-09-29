package com.ats.userservice.api.rest;

import com.ats.userservice.api.dto.request.ChangeUserRoleRequest;
import com.ats.userservice.api.dto.request.ChangeUserStatusRequest;
import com.ats.userservice.api.dto.request.CreateExternalUserRequest;
import com.ats.userservice.api.dto.request.CreateLocalUserRequest;
import com.ats.userservice.api.dto.request.UpdateUserProfileRequest;
import com.ats.userservice.api.dto.response.UserResponse;
import com.ats.userservice.api.mapper.UserResponseMapper;
import com.ats.userservice.application.command.user.ChangeUserRoleCommand;
import com.ats.userservice.application.command.user.ChangeUserStatusCommand;
import com.ats.userservice.application.command.user.CreateExternalUserCommand;
import com.ats.userservice.application.command.user.CreateLocalUserCommand;
import com.ats.userservice.application.command.user.UpdateUserProfileCommand;
import com.ats.userservice.application.port.in.UserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserResponseMapper userResponseMapper;

    public UserController(UserUseCase userUseCase, UserResponseMapper userResponseMapper) {
        this.userUseCase = userUseCase;
        this.userResponseMapper = userResponseMapper;
    }

    @PostMapping("/local")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createLocal(@Valid @RequestBody CreateLocalUserRequest request) {
        return userResponseMapper.toResponse(userUseCase.createLocal(new CreateLocalUserCommand(
                request.getDepartmentId(),
                request.getFullName(),
                request.getEmail(),
                request.getPasswordHash(),
                request.getPhone(),
                request.getRole(),
                request.getActor()
        )));
    }

    @PostMapping("/external")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createExternal(@Valid @RequestBody CreateExternalUserRequest request) {
        return userResponseMapper.toResponse(userUseCase.createExternal(new CreateExternalUserCommand(
                request.getDepartmentId(),
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getRole(),
                request.getAuthProvider(),
                request.getExternalSubjectId(),
                request.getActor()
        )));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userResponseMapper.toResponse(userUseCase.getById(id));
    }

    @GetMapping("/by-email")
    public UserResponse getByEmail(@RequestParam String email) {
        return userResponseMapper.toResponse(userUseCase.getByEmail(email));
    }

    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(@PathVariable Long id, @Valid @RequestBody UpdateUserProfileRequest request) {
        return userResponseMapper.toResponse(userUseCase.updateProfile(new UpdateUserProfileCommand(
                id,
                request.getDepartmentId(),
                request.getFullName(),
                request.getPhone(),
                request.getActor()
        )));
    }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody ChangeUserRoleRequest request) {
        return userResponseMapper.toResponse(userUseCase.changeRole(new ChangeUserRoleCommand(
                id,
                request.getRole(),
                request.getActor()
        )));
    }

    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeUserStatusRequest request) {
        return userResponseMapper.toResponse(userUseCase.changeStatus(new ChangeUserStatusCommand(
                id,
                request.getStatus(),
                request.getActor()
        )));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam(required = false) String actor) {
        userUseCase.delete(id, actor);
    }
}
