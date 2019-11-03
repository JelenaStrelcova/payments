package jelstr.payment.configuration;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import jelstr.payment.engine.PaymentProcessingEngine;
import jelstr.payment.engine.PaymentTransactionListener;
import lombok.Getter;
import org.eclipse.jetty.server.Server;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import jelstr.payment.eventbus.EventBusFactory;

import java.util.EventListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentAppInitializer {

    @Getter
    Server server;
    @Getter
    PaymentProcessingEngine paymentProcessingEngine;

    public void run(Injector injector) throws Exception {
        registerPaymentTransactionListener(injector);
        startJetty(injector);
        startPaymentProcessingEngine(injector);
    }

    private void startJetty(Injector injector) throws Exception {
        EventListener eventListener = injector.getInstance(GuiceResteasyBootstrapServletContextListener.class);
        GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);
        server = injector.getInstance(JettyModule.class).startJetty(eventListener, guiceFilter);
    }

    private void registerPaymentTransactionListener(Injector injector) {
        EventBus eventBus = injector.getInstance(EventBusFactory.class).getEventBus();
        PaymentTransactionListener listener = injector.getInstance(PaymentTransactionListener.class);
        eventBus.register(listener);
    }

    private void startPaymentProcessingEngine(Injector injector) {
        paymentProcessingEngine =  injector.getInstance(PaymentProcessingEngine.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(paymentProcessingEngine);
    }
}
