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
@RequestMapping("/api/v1/users")
public class UserProxyController {

    private final UserServiceClient userServiceClient;

    @PostMapping
    public ApiResponse<Object> createUser(@RequestBody Map<String, Object> request) {
        return userServiceClient.createUser(request);
    }

    @GetMapping
    public ApiResponse<Object> listUsers(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "role", required = false) String role,
        @RequestParam(value = "status", required = false) Integer status
    ) {
        return userServiceClient.listUsers(page, pageSize, keyword, role, status);
    }

    @GetMapping("/{userId}/detail")
    public ApiResponse<Object> getUserDetail(@PathVariable("userId") String userId) {
        return userServiceClient.getUserDetail(userId);
    }

    @GetMapping("/me")
    public ApiResponse<Object> getCurrentUser() {
        return userServiceClient.getCurrentUser();
    }

    @GetMapping("/me/menus")
    public ApiResponse<Object> getCurrentMenus() {
        return userServiceClient.getCurrentMenus();
    }

    @PutMapping
    public ApiResponse<Object> updateUsers(@RequestBody Map<String, Object> request) {
        return userServiceClient.updateUsers(request);
    }

    @PutMapping("/me")
    public ApiResponse<Object> updateCurrentUser(@RequestBody Map<String, Object> request) {
        return userServiceClient.updateCurrentUser(request);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Object> deleteUser(@PathVariable("userId") String userId, @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        return userServiceClient.deleteUser(userId, permanent);
    }

    @DeleteMapping("/batch")
    public ApiResponse<Object> batchDeleteUsers(@RequestBody Map<String, Object> request) {
        return userServiceClient.batchDeleteUsers(request);
    }

    @PostMapping("/me/password")
    public ApiResponse<Object> changePassword(@RequestBody Map<String, Object> request) {
        return userServiceClient.changePassword(request);
    }

    @PostMapping("/{userId}/password/reset")
    public ApiResponse<Object> resetPassword(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request) {
        return userServiceClient.resetPassword(userId, request);
    }

    @PostMapping("/{userId}/attributes")
    public ApiResponse<Object> createUserAttribute(@PathVariable("userId") String userId, @RequestBody Map<String, Object> request) {
        return userServiceClient.createUserAttribute(userId, request);
    }

    @PutMapping("/{userId}/attributes/{attributeId}")
    public ApiResponse<Object> updateUserAttribute(
        @PathVariable("userId") String userId,
        @PathVariable("attributeId") String attributeId,
        @RequestBody Map<String, Object> request
    ) {
        return userServiceClient.updateUserAttribute(userId, attributeId, request);
    }

    @DeleteMapping("/{userId}/attributes/{attributeId}")
    public ApiResponse<Object> deleteUserAttribute(@PathVariable("userId") String userId, @PathVariable("attributeId") String attributeId) {
        return userServiceClient.deleteUserAttribute(userId, attributeId);
    }

    @GetMapping("/{userId}/attributes")
    public ApiResponse<Object> listUserAttributes(@PathVariable("userId") String userId) {
        return userServiceClient.listUserAttributes(userId);
    }

    @GetMapping("/me/attributes")
    public ApiResponse<Object> listCurrentUserAttributes() {
        return userServiceClient.listCurrentUserAttributes();
    }
}
