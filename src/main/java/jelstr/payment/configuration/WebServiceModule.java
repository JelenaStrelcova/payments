package jelstr.payment.configuration;

import com.google.inject.AbstractModule;
import jelstr.payment.rest.PaymentWebService;

public class WebServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PaymentWebService.class);
    }
}
