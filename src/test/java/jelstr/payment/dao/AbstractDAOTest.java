package jelstr.payment.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AbstractDAOTest {

    protected Injector getInjector() {
        Injector injector = Guice.createInjector(new MockPersistenceModule());
        injector.getInstance(MockPersistenceInitializer.class);
        return injector;
    }
}
