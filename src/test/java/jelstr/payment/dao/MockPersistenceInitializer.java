package jelstr.payment.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;

@Singleton
public
class MockPersistenceInitializer {
    @Inject
    MockPersistenceInitializer(PersistService service) {
        service.start();
    }
}
