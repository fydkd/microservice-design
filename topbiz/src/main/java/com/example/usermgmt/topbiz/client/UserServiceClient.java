package com.example.usermgmt.topbiz.client;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.common.auth.AuthCodeLoginRequest;
import com.example.usermgmt.common.auth.AuthFeishuLoginRequest;
import com.example.usermgmt.common.auth.AuthPasswordLoginRequest;
import com.example.usermgmt.common.auth.AuthRegisterRequest;
import com.example.usermgmt.common.auth.AuthResetPasswordRequest;
import com.example.usermgmt.common.auth.AuthenticatedUser;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service-client", url = "${topbiz.clients.user-service-url}")
public interface UserServiceClient {

    @PostMapping("/internal/auth/password-login")
    ApiResponse<AuthenticatedUser> passwordLogin(@RequestBody AuthPasswordLoginRequest request);

    @PostMapping("/internal/auth/code-login")
    ApiResponse<AuthenticatedUser> codeLogin(@RequestBody AuthCodeLoginRequest request);

    @PostMapping("/internal/auth/feishu-login")
    ApiResponse<AuthenticatedUser> feishuLogin(@RequestBody AuthFeishuLoginRequest request);

    @PostMapping("/internal/auth/register")
    ApiResponse<AuthenticatedUser> register(@RequestBody AuthRegisterRequest request);

    @PostMapping("/internal/auth/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody AuthResetPasswordRequest request);

    @PostMapping("/internal/auth/account-exists")
    ApiResponse<Boolean> accountExists(@RequestParam("account") String account);

    @PostMapping("/internal/users")
    ApiResponse<Object> createUser(@RequestBody Map<String, Object> request);

    @GetMapping("/internal/users")
    ApiResponse<Object> listUsers(
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "role", required = false) String role,
        @RequestParam(value = "status", required = false) Integer status
    );

    @GetMapping("/internal/users/{userId}/detail")
    ApiResponse<Object> getUserDetail(@PathVariable("userId") String userId);

    @GetMapping("/internal/users/me")
    ApiResponse<Object> getCurrentUser();

    @PostMapping("/internal/users/me/cancel")
    ApiResponse<Object> cancelCurrentUser();

    @GetMapping("/internal/users/me/menus")
    ApiResponse<Object> getCurrentMenus();

    @PutMapping("/internal/users")
    ApiResponse<Object> updateUsers(@RequestBody Map<String, Object> request);

    @PutMapping("/internal/users/me")
    ApiResponse<Object> updateCurrentUser(@RequestBody Map<String, Object> request);

    @DeleteMapping("/internal/users/{userId}")
    ApiResponse<Object> deleteUser(@PathVariable("userId") String userId, @RequestParam("permanent") boolean permanent);

    @DeleteMapping("/internal/users/batch")
    ApiResponse<Object> batchDeleteUsers(@RequestBody Map<String, Object> request);

    @PostMapping("/internal/users/me/password")
    ApiResponse<Object> changePassword(@RequestBody Map<String, Object> request);

    @PostMapping("/internal/users/{userId}/password/reset")
    ApiResponse<Object> resetPassword(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request);

    @PostMapping("/internal/users/{userId}/attributes")
    ApiResponse<Object> createUserAttribute(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request);

    @PutMapping("/internal/users/{userId}/attributes/{attributeId}")
    ApiResponse<Object> updateUserAttribute(
        @PathVariable("userId") String userId,
        @PathVariable("attributeId") String attributeId,
        @RequestBody Map<String, Object> request
    );

    @DeleteMapping("/internal/users/{userId}/attributes/{attributeId}")
    ApiResponse<Object> deleteUserAttribute(
        @PathVariable("userId") String userId,
        @PathVariable("attributeId") String attributeId
    );

    @GetMapping("/internal/users/{userId}/attributes")
    ApiResponse<Object> listUserAttributes(@PathVariable("userId") String userId);

    @GetMapping("/internal/users/me/attributes")
    ApiResponse<Object> listCurrentUserAttributes();

    @PostMapping("/internal/groups")
    ApiResponse<Object> createGroup(@RequestBody Map<String, Object> request);

    @GetMapping("/internal/groups")
    ApiResponse<Object> listGroups(
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) Integer status
    );

    @GetMapping("/internal/groups/{groupId}")
    ApiResponse<Object> getGroupDetail(@PathVariable("groupId") String groupId);

    @GetMapping("/internal/groups/my")
    ApiResponse<Object> listMyGroups(
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) Integer status
    );

    @GetMapping("/internal/groups/public")
    ApiResponse<Object> listPublicGroups(
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword
    );

    @GetMapping("/internal/groups/{groupId}/my")
    ApiResponse<Object> getMyGroupDetail(@PathVariable("groupId") String groupId);

    @PutMapping("/internal/groups")
    ApiResponse<Object> updateGroupsProxy(@RequestBody Map<String, Object> request);

    @DeleteMapping("/internal/groups")
    ApiResponse<Object> deleteGroups(@RequestBody Map<String, Object> request);

    @PostMapping("/internal/groups/{groupId}/members")
    ApiResponse<Object> addGroupMembers(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/internal/groups/{groupId}/members")
    ApiResponse<Object> removeGroupMembers(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request);

    @PutMapping("/internal/groups/{groupId}/members/{userId}/upgrade")
    ApiResponse<Object> upgradeGroupMember(
        @PathVariable("groupId") String groupId,
        @PathVariable("userId") String userId,
        @RequestBody Map<String, Object> request
    );

    @PostMapping("/internal/groups/{groupId}/temp-password")
    ApiResponse<Object> setGroupTempPassword(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/internal/groups/{groupId}/temp-password")
    ApiResponse<Object> clearGroupTempPassword(@PathVariable("groupId") String groupId);

    @PostMapping("/internal/groups/{groupId}/join")
    ApiResponse<Object> joinGroup(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request);

    @PostMapping("/internal/groups/{groupId}/attributes")
    ApiResponse<Object> createGroupAttribute(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request);

    @PutMapping("/internal/groups/{groupId}/attributes/{attributeId}")
    ApiResponse<Object> updateGroupAttribute(
        @PathVariable("groupId") String groupId,
        @PathVariable("attributeId") String attributeId,
        @RequestBody Map<String, Object> request
    );

    @DeleteMapping("/internal/groups/{groupId}/attributes/{attributeId}")
    ApiResponse<Object> deleteGroupAttribute(
        @PathVariable("groupId") String groupId,
        @PathVariable("attributeId") String attributeId
    );

    @GetMapping("/internal/groups/{groupId}/attributes")
    ApiResponse<Object> listGroupAttributes(@PathVariable("groupId") String groupId);

    @PostMapping("/internal/permissions")
    ApiResponse<Object> createPermission(@RequestBody Map<String, Object> request);

    @PutMapping("/internal/permissions/{permId}")
    ApiResponse<Object> updatePermission(@PathVariable("permId") String permId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/internal/permissions/{permId}")
    ApiResponse<Object> deletePermission(@PathVariable("permId") String permId, @RequestParam("force") boolean force);

    @GetMapping("/internal/permissions")
    ApiResponse<Object> listPermissions(@RequestParam(value = "keyword", required = false) String keyword);

    @PostMapping("/internal/groups/{groupId}/permissions")
    ApiResponse<Object> grantGroupPermissions(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/internal/groups/{groupId}/permissions")
    ApiResponse<Object> revokeGroupPermissions(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request);

    @GetMapping("/internal/groups/{groupId}/permissions")
    ApiResponse<Object> getGroupPermissions(@PathVariable("groupId") String groupId);

    @PostMapping("/internal/users/{userId}/permissions")
    ApiResponse<Object> grantUserPermissions(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request);

    @DeleteMapping("/internal/users/{userId}/permissions")
    ApiResponse<Object> revokeUserPermissions(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request);

    @GetMapping("/internal/users/{userId}/permissions")
    ApiResponse<Object> getUserDirectPermissions(@PathVariable("userId") String userId);

    @GetMapping("/internal/users/{userId}/permissions/effective")
    ApiResponse<Object> getUserEffectivePermissions(@PathVariable("userId") String userId);

    @GetMapping("/internal/users/{userId}/permissions/source/{permId}")
    ApiResponse<Object> getPermissionSource(@PathVariable("userId") String userId, @PathVariable("permId") String permId);

    @GetMapping("/internal/permissions/{permId}/groups")
    ApiResponse<Object> getPermissionGroups(
        @PathVariable("permId") String permId,
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/internal/permissions/{permId}/users")
    ApiResponse<Object> getPermissionUsers(
        @PathVariable("permId") String permId,
        @RequestParam("page") Integer page,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "sourceType", required = false) String sourceType,
        @RequestParam(value = "status", required = false) String status
    );
}
