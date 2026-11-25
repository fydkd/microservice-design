package com.example.usermgmt.topbiz.flow;

import com.example.usermgmt.topbiz.flow.definition.FlowDefinition;
import com.example.usermgmt.topbiz.flow.definition.FlowNodeDefinition;
import com.example.usermgmt.topbiz.flow.definition.FlowTransitionDefinition;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlowEngine {

    private final FlowDefinitionRepository repository;
    private final List<FlowAction> actions;
    private final List<FlowCondition> conditions;

    public FlowContext execute(String flowCode, FlowContext context) {
        FlowDefinition definition = repository.get(flowCode);
        Map<String, FlowAction> actionMap = actions.stream().collect(Collectors.toMap(FlowAction::code, Function.identity()));
        Map<String, FlowCondition> conditionMap = conditions.stream().collect(Collectors.toMap(FlowCondition::code, Function.identity()));
        Map<String, FlowNodeDefinition> nodeMap = definition.getNodes().stream().collect(Collectors.toMap(FlowNodeDefinition::getId, Function.identity()));

        FlowNodeDefinition current = definition.getNodes().stream()
            .filter(node -> "start".equalsIgnoreCase(node.getType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No start node defined"));

        while (current != null && !"end".equalsIgnoreCase(current.getType())) {
            if ("action".equalsIgnoreCase(current.getType())) {
                actionMap.get(current.getAction()).execute(context);
                current = nextNode(definition.getTransitions(), nodeMap, current.getId(), null);
                continue;
            }
            if ("condition".equalsIgnoreCase(current.getType())) {
                boolean result = conditionMap.get(current.getCondition()).evaluate(context);
                current = nextNode(definition.getTransitions(), nodeMap, current.getId(), result);
                continue;
            }
            current = nextNode(definition.getTransitions(), nodeMap, current.getId(), null);
        }
        return context;
    }

    private FlowNodeDefinition nextNode(
        List<FlowTransitionDefinition> transitions,
        Map<String, FlowNodeDefinition> nodeMap,
        String currentNodeId,
        Boolean conditionResult
    ) {
        FlowTransitionDefinition transition = transitions.stream()
            .filter(item -> item.getFrom().equals(currentNodeId))
            .filter(item -> conditionResult == null || item.getOn() == null || item.getOn().equals(conditionResult))
            .findFirst()
            .orElse(null);
        if (transition == null) {
            return null;
        }
        return nodeMap.get(transition.getTo());
    }
}
