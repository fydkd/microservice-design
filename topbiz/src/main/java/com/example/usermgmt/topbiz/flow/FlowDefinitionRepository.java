package com.example.usermgmt.topbiz.flow;

import com.example.usermgmt.topbiz.flow.definition.FlowDefinition;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Component
public class FlowDefinitionRepository {

    private final Map<String, FlowDefinition> definitions = new HashMap<>();
    private final Map<String, String> rawDefinitions = new HashMap<>();

    @Getter
    private final List<String> resourcePatterns = List.of("classpath*:flows/*.yml");

    public FlowDefinitionRepository() {
        loadDefinitions();
    }

    public FlowDefinition get(String flowCode) {
        FlowDefinition definition = definitions.get(flowCode);
        if (definition == null) {
            throw new IllegalArgumentException("Flow definition not found: " + flowCode);
        }
        return definition;
    }

    public String getRaw(String flowCode) {
        String raw = rawDefinitions.get(flowCode);
        if (raw == null) {
            throw new IllegalArgumentException("Raw flow definition not found: " + flowCode);
        }
        return raw;
    }

    private void loadDefinitions() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Yaml yaml = new Yaml(new Constructor(FlowDefinition.class, new LoaderOptions()));
            for (String pattern : resourcePatterns) {
                Resource[] resources = resolver.getResources(pattern);
                for (Resource resource : resources) {
                    try (InputStream inputStream = resource.getInputStream()) {
                        FlowDefinition definition = yaml.loadAs(inputStream, FlowDefinition.class);
                        if (definition != null && definition.getFlowCode() != null) {
                            definitions.put(definition.getFlowCode(), definition);
                            rawDefinitions.put(definition.getFlowCode(), resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load flow definitions", exception);
        }
    }
}
