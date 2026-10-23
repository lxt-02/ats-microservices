package com.ats.user.api.controller;

import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.PageResponse;
import com.ats.shared.web.ApiResponse;
import com.ats.user.api.request.ChangeUserRoleRequest;
import com.ats.user.api.request.ChangeUserStatusRequest;
import com.ats.user.api.request.CreateExternalUserRequest;
import com.ats.user.api.request.CreateLocalUserRequest;
import com.ats.user.api.request.ListUserRequest;
import com.ats.user.api.request.UpdateUserProfileRequest;
import com.ats.user.application.response.UserResponse;
import com.ats.user.application.usecase.ChangeUserRoleUseCase;
import com.ats.user.application.usecase.ChangeUserStatusUseCase;
import com.ats.user.application.usecase.CreateExternalUserUseCase;
import com.ats.user.application.usecase.CreateLocalUserUseCase;
import com.ats.user.application.usecase.DeleteUserUseCase;
import com.ats.user.application.usecase.GetUserByEmailUseCase;
import com.ats.user.application.usecase.GetUserByIdUseCase;
import com.ats.user.application.usecase.ListUsersUseCase;
import com.ats.user.application.usecase.UpdateUserProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiresRole(Roles.ADMIN)
public class UserController {

    private static final int OK = HttpStatus.OK.value();

    private final CreateLocalUserUseCase createLocalUserUseCase;
    private final CreateExternalUserUseCase createExternalUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetUserByEmailUseCase getUserByEmailUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ListUsersUseCase listUsersUseCase;

    public UserController(CreateLocalUserUseCase createLocalUserUseCase,
                          CreateExternalUserUseCase createExternalUserUseCase,
                          GetUserByIdUseCase getUserByIdUseCase,
                          GetUserByEmailUseCase getUserByEmailUseCase,
                          UpdateUserProfileUseCase updateUserProfileUseCase,
                          ChangeUserRoleUseCase changeUserRoleUseCase,
                          ChangeUserStatusUseCase changeUserStatusUseCase,
                          DeleteUserUseCase deleteUserUseCase,
                          ListUsersUseCase listUsersUseCase) {
        this.createLocalUserUseCase = createLocalUserUseCase;
        this.createExternalUserUseCase = createExternalUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.getUserByEmailUseCase = getUserByEmailUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.changeUserRoleUseCase = changeUserRoleUseCase;
        this.changeUserStatusUseCase = changeUserStatusUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
    }

    @PostMapping("/local")
    public ResponseEntity<ApiResponse<UserResponse>> createLocal(@Valid @RequestBody CreateLocalUserRequest request,
                                                                 CurrentUser currentUser) {
        return created(createLocalUserUseCase.execute(request.toCommand(), currentUser.actor()));
    }

    @PostMapping("/external")
    public ResponseEntity<ApiResponse<UserResponse>> createExternal(
            @Valid @RequestBody CreateExternalUserRequest request, CurrentUser currentUser) {
        return created(createExternalUserUseCase.execute(request.toCommand(), currentUser.actor()));
    }

    /** Also used by recruiters to pick a Hiring Manager: {@code ?role=HIRING_MANAGER&status=ACTIVE}. */
    @GetMapping
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER})
    public ApiResponse<PageResponse<UserResponse>> list(@Valid ListUserRequest request) {
        return ApiResponse.success(OK, listUsersUseCase.execute(request.toQuery()));
    }

    @GetMapping("/{id}")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(OK, getUserByIdUseCase.execute(id));
    }

    @GetMapping("/by-email")
    public ApiResponse<UserResponse> getByEmail(@RequestParam String email) {
        return ApiResponse.success(OK, getUserByEmailUseCase.execute(email));
    }

    @PutMapping("/{id}/profile")
    public ApiResponse<UserResponse> updateProfile(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserProfileRequest request,
                                                   CurrentUser currentUser) {
        return ApiResponse.success(OK, updateUserProfileUseCase.execute(request.toCommand(id), currentUser.actor()));
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<UserResponse> changeRole(@PathVariable Long id,
                                                @Valid @RequestBody ChangeUserRoleRequest request,
                                                CurrentUser currentUser) {
        return ApiResponse.success(OK, changeUserRoleUseCase.execute(request.toCommand(id), currentUser.actor()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserResponse> changeStatus(@PathVariable Long id,
                                                  @Valid @RequestBody ChangeUserStatusRequest request,
                                                  CurrentUser currentUser) {
        return ApiResponse.success(OK, changeUserStatusUseCase.execute(request.toCommand(id), currentUser.actor()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, CurrentUser currentUser) {
        deleteUserUseCase.execute(id, currentUser.actor());
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<ApiResponse<UserResponse>> created(UserResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }
}
