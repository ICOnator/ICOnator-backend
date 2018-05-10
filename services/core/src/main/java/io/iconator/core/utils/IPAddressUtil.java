package io.iconator.core.utils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static java.util.Optional.ofNullable;

public class IPAddressUtil {

    public static String getIPAddress(@NotNull HttpServletRequest requestContext) {
        return ofNullable(requestContext.getHeader("X-Real-IP"))
                .orElse(requestContext.getRemoteAddr());
    }

}
