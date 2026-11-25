package com.example.usermgmt.topbiz.flow;

public interface FlowAction {

    String code();

    void execute(FlowContext context);
}
