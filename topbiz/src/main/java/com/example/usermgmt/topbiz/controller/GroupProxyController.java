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
@RequestMapping("/api/v1/groups")
public class GroupProxyController {

    private final UserServiceClient userServiceClient;

    @PostMapping
    public ApiResponse<Object> createGroup(@RequestBody Map<String, Object> request) {
        return userServiceClient.createGroup(request);
    }

    @GetMapping
    public ApiResponse<Object> listGroups(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) Integer status
    ) {
        return userServiceClient.listGroups(page, pageSize, keyword, status);
    }

    @GetMapping("/my")
    public ApiResponse<Object> listMyGroups(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "status", required = false) Integer status
    ) {
        return userServiceClient.listMyGroups(page, pageSize, keyword, status);
    }

    @GetMapping("/public")
    public ApiResponse<Object> listPublicGroups(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return userServiceClient.listPublicGroups(page, pageSize, keyword);
    }

    @GetMapping("/{groupId}")
    public ApiResponse<Object> getGroupDetail(@PathVariable("groupId") String groupId) {
        return userServiceClient.getGroupDetail(groupId);
    }

    @GetMapping("/{groupId}/my")
    public ApiResponse<Object> getMyGroupDetail(@PathVariable("groupId") String groupId) {
        return userServiceClient.getMyGroupDetail(groupId);
    }

    @PutMapping
    public ApiResponse<Object> updateGroups(@RequestBody Map<String, Object> request) {
        return userServiceClient.updateGroupsProxy(request);
    }

    @DeleteMapping
    public ApiResponse<Object> deleteGroups(@RequestBody Map<String, Object> request) {
        return userServiceClient.deleteGroups(request);
    }

    @PostMapping("/{groupId}/members")
    public ApiResponse<Object> addMembers(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request) {
        return userServiceClient.addGroupMembers(groupId, request);
    }

    @DeleteMapping("/{groupId}/members")
    public ApiResponse<Object> removeMembers(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request) {
        return userServiceClient.removeGroupMembers(groupId, request);
    }

    @PutMapping("/{groupId}/members/{userId}/upgrade")
    public ApiResponse<Object> upgradeMember(@PathVariable("groupId") String groupId, @PathVariable("userId") String userId, @RequestBody Map<String, Object> request) {
        return userServiceClient.upgradeGroupMember(groupId, userId, request);
    }

    @PostMapping("/{groupId}/temp-password")
    public ApiResponse<Object> setTempPassword(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request) {
        return userServiceClient.setGroupTempPassword(groupId, request);
    }

    @DeleteMapping("/{groupId}/temp-password")
    public ApiResponse<Object> clearTempPassword(@PathVariable("groupId") String groupId) {
        return userServiceClient.clearGroupTempPassword(groupId);
    }

    @PostMapping("/{groupId}/join")
    public ApiResponse<Object> joinGroup(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request) {
        return userServiceClient.joinGroup(groupId, request);
    }

    @PostMapping("/{groupId}/attributes")
    public ApiResponse<Object> createAttribute(@PathVariable("groupId") String groupId, @RequestBody Map<String, Object> request) {
        return userServiceClient.createGroupAttribute(groupId, request);
    }

    @PutMapping("/{groupId}/attributes/{attributeId}")
    public ApiResponse<Object> updateAttribute(@PathVariable("groupId") String groupId, @PathVariable("attributeId") String attributeId, @RequestBody Map<String, Object> request) {
        return userServiceClient.updateGroupAttribute(groupId, attributeId, request);
    }

    @DeleteMapping("/{groupId}/attributes/{attributeId}")
    public ApiResponse<Object> deleteAttribute(@PathVariable("groupId") String groupId, @PathVariable("attributeId") String attributeId) {
        return userServiceClient.deleteGroupAttribute(groupId, attributeId);
    }

    @GetMapping("/{groupId}/attributes")
    public ApiResponse<Object> listAttributes(@PathVariable("groupId") String groupId) {
        return userServiceClient.listGroupAttributes(groupId);
    }
}
