package ru.shortLinkService.config;

import jakarta.servlet.*;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.EnumSet;

public class AppInitializer implements WebApplicationInitializer {
    private static final String DISPATCHER = "dispatcher";
    private static final String FILTER = "encodingFilter";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);
        servletContext.addListener(new ContextLoaderListener(context));

        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(DISPATCHER, new DispatcherServlet(context));
        servletRegistration.addMapping("/");
        servletRegistration.setLoadOnStartup(1);


        FilterRegistration.Dynamic encoding = servletContext.addFilter(FILTER, new CharacterEncodingFilter());
        encoding.setInitParameter("encoding", "UTF-8");
        encoding.setInitParameter("forceEncoding", "true");
        encoding.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.REQUEST.getDeclaringClass()), false, "/*");

    }
}
