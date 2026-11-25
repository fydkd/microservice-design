package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.topbiz.client.UserServiceClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PermissionProxyController {

    private final UserServiceClient userServiceClient;

    @PostMapping("/permissions")
    public ApiResponse<Object> createPermission(@RequestBody Map<String, Object> request) {
        return userServiceClient.createPermission(request);
    }

    @PutMapping("/permissions/{permId}")
    public ApiResponse<Object> updatePermission(@PathVariable("permId") String permId, @RequestBody Map<String, Object> request) {
        return userServiceClient.updatePermission(permId, request);
    }

    @DeleteMapping("/permissions/{permId}")
    public ApiResponse<Object> deletePermission(@PathVariable("permId") String permId, @RequestParam(value = "force", defaultValue = "false") boolean force) {
        return userServiceClient.deletePermission(permId, force);
    }

    @GetMapping("/permissions")
    public ApiResponse<Object> listPermissions(@RequestParam(value = "keyword", required = false) String keyword) {
        return userServiceClient.listPermissions(keyword);
    }

    @PostMapping("/groups/{groupId}/permissions")
    public ApiResponse<Object> grantGroupPermissions(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request) {
        return userServiceClient.grantGroupPermissions(groupId, request);
    }

    @DeleteMapping("/groups/{groupId}/permissions")
    public ApiResponse<Object> revokeGroupPermissions(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request) {
        return userServiceClient.revokeGroupPermissions(groupId, request);
    }

    @GetMapping("/groups/{groupId}/permissions")
    public ApiResponse<Object> getGroupPermissions(@PathVariable("groupId") String groupId) {
        return userServiceClient.getGroupPermissions(groupId);
    }

    @PostMapping("/users/{userId}/permissions")
    public ApiResponse<Object> grantUserPermissions(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request) {
        return userServiceClient.grantUserPermissions(userId, request);
    }

    @DeleteMapping("/users/{userId}/permissions")
    public ApiResponse<Object> revokeUserPermissions(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request) {
        return userServiceClient.revokeUserPermissions(userId, request);
    }

    @GetMapping("/users/{userId}/permissions")
    public ApiResponse<Object> getUserDirectPermissions(@PathVariable("userId") String userId) {
        return userServiceClient.getUserDirectPermissions(userId);
    }

    @GetMapping("/users/{userId}/permissions/effective")
    public ApiResponse<Object> getUserEffectivePermissions(@PathVariable("userId") String userId) {
        return userServiceClient.getUserEffectivePermissions(userId);
    }

    @GetMapping("/users/{userId}/permissions/source/{permId}")
    public ApiResponse<Object> getPermissionSource(@PathVariable("userId") String userId, @PathVariable("permId") String permId) {
        return userServiceClient.getPermissionSource(userId, permId);
    }

    @GetMapping("/permissions/{permId}/groups")
    public ApiResponse<Object> getPermissionGroups(
        @PathVariable("permId") String permId,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", defaultValue = "active") String status
    ) {
        return userServiceClient.getPermissionGroups(permId, page, pageSize, keyword, status);
    }

    @GetMapping("/permissions/{permId}/users")
    public ApiResponse<Object> getPermissionUsers(
        @PathVariable("permId") String permId,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "sourceType", defaultValue = "all") String sourceType,
        @RequestParam(value = "status", defaultValue = "active") String status
    ) {
        return userServiceClient.getPermissionUsers(permId, page, pageSize, keyword, sourceType, status);
    }
}
