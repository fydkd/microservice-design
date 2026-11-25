package com.example.usermgmt.topbiz.service;

import lombok.Data;

@Data
public class DeviceLoginContext {

    private String deviceId;
    private String deviceType;
    private String deviceName;
    private String os;
    private String browser;
    private String loginIp;
}
