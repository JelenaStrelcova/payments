package jelstr.payment;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jelstr.payment.configuration.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new PersistenceModule(),
                new JettyModule(),
                new WebServiceModule(),
                new RestEasyModule());
        injector.getInstance(PaymentAppInitializer.class).run(injector);
    }
}
