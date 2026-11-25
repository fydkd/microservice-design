package com.example.usermgmt.topbiz.controller;

import com.example.usermgmt.common.api.ApiResponse;
import com.example.usermgmt.topbiz.config.FlowProperties;
import com.example.usermgmt.topbiz.flow.FlowDefinitionRepository;
import com.example.usermgmt.topbiz.flow.definition.FlowDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flows")
public class FlowController {

    private final FlowDefinitionRepository repository;
    private final FlowProperties flowProperties;

    @GetMapping("/register")
    public ApiResponse<Map<String, Object>> getRegisterFlow() {
        return ApiResponse.success(toResponse(flowProperties.getRegisterCode()));
    }

    @GetMapping("/cancel")
    public ApiResponse<Map<String, Object>> getCancelFlow() {
        return ApiResponse.success(toResponse(flowProperties.getCancelCode()));
    }

    private Map<String, Object> toResponse(String flowCode) {
        FlowDefinition definition = repository.get(flowCode);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("flowCode", definition.getFlowCode());
        result.put("version", definition.getVersion());
        result.put("nodes", definition.getNodes());
        result.put("transitions", definition.getTransitions());
        result.put("yaml", repository.getRaw(flowCode));
        return result;
    }
}
