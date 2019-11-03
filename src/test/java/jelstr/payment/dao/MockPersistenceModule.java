package jelstr.payment.dao;

import com.google.inject.persist.jpa.JpaPersistModule;
import jelstr.payment.configuration.PersistenceModule;

public class MockPersistenceModule extends PersistenceModule {
    @Override
    protected void configure() {
        install(new JpaPersistModule("payments-persister"));
        bind(MockPersistenceInitializer.class);
    }

}
