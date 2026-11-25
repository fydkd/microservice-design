package com.example.usermgmt.topbiz.context;

import com.example.usermgmt.common.auth.SessionUser;

public final class SessionContext {

    private static final ThreadLocal<SessionUser> HOLDER = new ThreadLocal<>();

    private SessionContext() {
    }

    public static void set(SessionUser sessionUser) {
        HOLDER.set(sessionUser);
    }

    public static SessionUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
