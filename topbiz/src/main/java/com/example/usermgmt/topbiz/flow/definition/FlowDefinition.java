package com.example.usermgmt.topbiz.flow.definition;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlowDefinition {

    private String flowCode;
    private Integer version;
    private List<FlowNodeDefinition> nodes = new ArrayList<>();
    private List<FlowTransitionDefinition> transitions = new ArrayList<>();
}
