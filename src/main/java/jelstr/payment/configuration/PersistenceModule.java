package jelstr.payment.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

public class PersistenceModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new JpaPersistModule("payments-persister"));
    }
}
