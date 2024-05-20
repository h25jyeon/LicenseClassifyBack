package com.gytni.licenseclassify.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Https {

    public static String getClientIp(HttpServletRequest request) {

        if (request == null) return "request is null";

        String clientIp = request.getHeader("X-Forwarded-For");

        if (isEmpty(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP"); // Proxy 서버인 경우
        }
        if (isEmpty(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");// Weblogic 서버인 경우
        }
        if (isEmpty(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isEmpty(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isEmpty(clientIp) || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
            
            if ("0:0:0:0:0:0:0:1".equalsIgnoreCase(clientIp)) {
                clientIp = "127.0.0.1"; // localhost인경우 변환
            }
        }

        return clientIp;
    }

	public static String getFullUrl() {
        return getFullUrl(getCurrentRequest());
    }
	public static String getFullUrl(HttpServletRequest request) {
        final StringBuffer sb = request.getRequestURL();
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }
		return sb.toString();
	}

    public static String getFullURI() {
        return getFullURI(getCurrentRequest());
    }
	public static String getFullURI(HttpServletRequest request) {
		final String uri = request.getRequestURI();
        return request.getQueryString() != null
            ? (uri + "?" + request.getQueryString())
            : uri;
    }

    public static HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
        Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
        HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
        return servletRequest;
    }

    public static Map<String, String> getCookies() {
        return getCookies(null);
    }

    public static Map<String, String> getCookies(HttpServletRequest request) {
        Map<String, String> result = new HashMap<String, String>();
        if (request == null) {
            request = getCurrentRequest();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                result.put(c.getName(), c.getValue());
            }
        }
        return result;
    }

	public static String getRequestInfo() {
        return getRequestInfo(getCurrentRequest());
    }

	public static String getRequestInfo(HttpServletRequest request) {
        return String.format("clientIp: %s, sessionId: %s, url: %s, referer: %s, user-agent: %s",
            getClientIp(request),
            Optional.ofNullable(request.getSession(false)).map(HttpSession::getId).orElse("(empty)"),
            getFullUrl(request), request.getHeader("Referer"), request.getHeader("User-Agent")
        );
	}
}