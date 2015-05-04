package com.ebaas;

/**
 * Created by anki on 03-05-2015.
 */
public class SecurityContextThreadLocal {

    public static final ThreadLocal userThreadLocal = new ThreadLocal();

    public static void set(SecurityContext user) {
        userThreadLocal.set(user);
    }

    public static void unset() {
        userThreadLocal.remove();
    }

    public static SecurityContext get() {
        return (SecurityContext)userThreadLocal.get();
    }
}
