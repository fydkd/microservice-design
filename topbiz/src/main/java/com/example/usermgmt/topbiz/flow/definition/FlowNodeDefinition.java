package com.example.usermgmt.topbiz.flow.definition;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlowNodeDefinition {

    private String id;
    private String type;
    private String condition;
    private String action;
}
