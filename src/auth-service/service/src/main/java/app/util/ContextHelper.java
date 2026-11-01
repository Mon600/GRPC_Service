package app.util;

import app.interceptor.AuthorizationInterceptor;

public class ContextHelper {
    public static String getUserIdFromContext() {
        Object userObj = AuthorizationInterceptor.USER_ID_CONTEXT_KEY.get();
        if (userObj == null) {
            throw new IllegalStateException("User ID not found in context");
        }
        return userObj.toString();
    }
}
