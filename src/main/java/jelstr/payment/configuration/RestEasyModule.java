package jelstr.payment.configuration;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.ServletModule;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import java.util.Map;

public class RestEasyModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(GuiceResteasyBootstrapServletContextListener.class);
        bind(HttpServletDispatcher.class).in(Singleton.class);

        final Map<String, String> initParams = ImmutableMap.of("resteasy.servlet.mapping.prefix", WebServiceConstants.APPLICATION_PATH);
        String urlPattern = WebServiceConstants.APPLICATION_PATH + "/*";
        filter(urlPattern).through(PersistFilter.class);
        serve(urlPattern).with(HttpServletDispatcher.class, initParams);
    }
}
