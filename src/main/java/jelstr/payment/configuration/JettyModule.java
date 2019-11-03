package jelstr.payment.configuration;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.EventListener;

public class JettyModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(GuiceFilter.class);
    }

    public Server startJetty(EventListener eventListener, GuiceFilter filter) throws Exception {
        final Server server = new Server(WebServiceConstants.PORT);

        // Setup the basic Application "context" at "/".
        final ServletContextHandler context = new ServletContextHandler(server, WebServiceConstants.CONTEXT_ROOT);

        // Add the GuiceFilter
        FilterHolder filterHolder = new FilterHolder(filter);
        context.addFilter(filterHolder, WebServiceConstants.APPLICATION_PATH + "/*", null);

        // Setup the DefaultServlet at "/".
        final ServletHolder defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, WebServiceConstants.CONTEXT_ROOT);
        context.addEventListener(eventListener);

        final HandlerCollection handlers = new HandlerCollection();
        // The Application context is currently the server handler,
        // add it to the list.
        handlers.addHandler(server.getHandler());

        server.setHandler(handlers);
        server.start();
        return server;
    }
}
