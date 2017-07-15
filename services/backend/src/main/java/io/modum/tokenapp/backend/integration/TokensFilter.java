package io.modum.tokenapp.backend.integration;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
public class TokensFilter extends ZuulFilter {

    private static final String BEARER = "Bearer";
    private static final String AUTH = "authorization";

    @Value("#{'${commands.tokens:}'.split(',')}")
    private Set<String> availableTokens;

    private boolean allowAll;
    private boolean rejectAll;

    @PostConstruct
    public void init() {
        rejectAll = availableTokens.isEmpty();
        allowAll = !rejectAll && availableTokens.size() == 1 && availableTokens.contains("*");
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
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
        RequestContext context = RequestContext.getCurrentContext();
        if (rejectAll) {
            context.setSendZuulResponse(false);
        } else if (!allowAll) {
            HttpServletRequest request = context.getRequest();
            String token = request.getHeader(AUTH);
            boolean allow = false;
            if (token != null && token.contains(BEARER)) {
                String tokenValue = token.replace(BEARER, "").trim();
                allow = availableTokens.contains(tokenValue);
            }
            context.setSendZuulResponse(allow);
        }
        return null;
    }
}
