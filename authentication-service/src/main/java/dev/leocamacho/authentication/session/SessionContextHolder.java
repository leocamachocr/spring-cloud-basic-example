package dev.leocamacho.authentication.session;

import dev.leocamacho.authentication.session.Session;

public class SessionContextHolder {

    //generate thread variable
    private static final ThreadLocal<Session> contextHolder = new ThreadLocal<>();

    public static void setSession(Session session) {
        if (contextHolder.get() != null) {
            throw new IllegalStateException("Session already set");
        }
        contextHolder.set(session);
    }

    public static Session getSession() {
        return contextHolder.get();
    }

    public static void clearSession() {
        contextHolder.remove();
    }

}
