package com.ebaas.filter;

import com.ebaas.SecurityContext;
import com.ebaas.SecurityContextProvider;
import com.ebaas.SecurityContextThreadLocal;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by anki on 03-05-2015.
 */
public class AuthenticationFilter implements Filter{



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        SecurityContextProvider securityContextProvider = null;
        String token = (String)((HttpServletRequest)servletRequest).getHeader("Authorization");
        String apiToken = (String)((HttpServletRequest)servletRequest).getHeader("apiKey");
        if(token != null){
            System.out.println("token:"+token);
            SecurityContext securityContext = securityContextProvider.getContext(token);
            SecurityContextThreadLocal.set(securityContext);
        }
        if(apiToken != null){
            System.out.println("apiToken:"+apiToken);
            SecurityContext securityContext = securityContextProvider.getContext(apiToken);
            SecurityContextThreadLocal.set(securityContext);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
