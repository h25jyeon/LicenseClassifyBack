package com.gytni.licenseclassify.config;

import jakarta.servlet.http.HttpServletRequest;

import com.gytni.licenseclassify.annotation.RemoteIp;
import com.gytni.licenseclassify.util.Https;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class RemoteIpResolver implements HandlerMethodArgumentResolver {

    /**
     * resolveArgument를 실행 할 수 있는 method인지 판별
     * 
     * @param methodParameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(RemoteIp.class);
    }

    /**
     * Method parameter에 대한 Argument Resovler로직 처리
     * 
     * @param methodParameter
     * @param modelAndViewContainer
     * @param nativeWebRequest
     * @param webDataBinderFactory
     * @return
     * @throws Exception
     */
    @Override
    public Object resolveArgument(MethodParameter param, ModelAndViewContainer mavc, NativeWebRequest req, WebDataBinderFactory wbf) throws Exception {
        return Https.getClientIp((HttpServletRequest) req.getNativeRequest());
    }
}