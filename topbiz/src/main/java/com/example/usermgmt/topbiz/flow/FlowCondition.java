package com.example.usermgmt.topbiz.flow;

public interface FlowCondition {

    String code();

    boolean evaluate(FlowContext context);
}
