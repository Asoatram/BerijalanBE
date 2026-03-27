package com.example.berijalanassesment.security;

import java.util.List;

public final class AuthPermissions {

    public static final String DASHBOARD_READ = "DASHBOARD_READ";
    public static final String VISITOR_READ = "VISITOR_READ";
    public static final String VISITOR_REGISTER = "VISITOR_REGISTER";
    public static final String VISITOR_CHECKOUT = "VISITOR_CHECKOUT";
    public static final String RISK_ALERT_READ = "RISK_ALERT_READ";
    public static final String RISK_ALERT_ACTION = "RISK_ALERT_ACTION";
    public static final String SESSION_READ = "SESSION_READ";
    public static final String SESSION_PRINT_PASS = "SESSION_PRINT_PASS";
    public static final String SESSION_CONTACT_HOST = "SESSION_CONTACT_HOST";
    public static final String SESSION_FORCE_CHECKOUT = "SESSION_FORCE_CHECKOUT";
    public static final String SESSION_REPORT_READ = "SESSION_REPORT_READ";
    public static final String SESSION_FLAG = "SESSION_FLAG";
    public static final String REPORT_READ = "REPORT_READ";
    public static final String REPORT_GENERATE = "REPORT_GENERATE";
    public static final String REPORT_EXPORT = "REPORT_EXPORT";

    public static final List<String> ALL = List.of(
        DASHBOARD_READ,
        VISITOR_READ,
        VISITOR_REGISTER,
        VISITOR_CHECKOUT,
        RISK_ALERT_READ,
        RISK_ALERT_ACTION,
        SESSION_READ,
        SESSION_PRINT_PASS,
        SESSION_CONTACT_HOST,
        SESSION_FORCE_CHECKOUT,
        SESSION_REPORT_READ,
        SESSION_FLAG,
        REPORT_READ,
        REPORT_GENERATE,
        REPORT_EXPORT
    );

    private AuthPermissions() {
    }
}
