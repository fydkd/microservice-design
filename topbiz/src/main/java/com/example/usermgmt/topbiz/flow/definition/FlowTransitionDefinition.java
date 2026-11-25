package com.example.usermgmt.topbiz.flow.definition;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlowTransitionDefinition {

    private String from;
    private String to;
    private Boolean on;
}
