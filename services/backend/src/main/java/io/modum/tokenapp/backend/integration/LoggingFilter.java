package io.modum.tokenapp.backend.integration;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Collections.list;
import static java.util.stream.Collectors.toList;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

@Component
public class LoggingFilter extends ZuulFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        HttpServletResponse response = RequestContext.getCurrentContext().getResponse();
        LOGGER.info("REQUEST :: < {} {}:{}", request.getScheme(), request.getLocalAddr(), request.getLocalPort());
        LOGGER.info("REQUEST :: < {} {} {}", request.getMethod(), request.getRequestURI(), request.getProtocol());
        LOGGER.info("REQUEST :: < {}", list(request.getHeaderNames()).stream().map(n -> n + ": " + request.getHeader(n)).collect(toList()));
        LOGGER.info("RESPONSE:: > HTTP:{}", response.getStatus());
        return null;
    }
}
