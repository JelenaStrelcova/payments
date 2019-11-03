package jelstr.payment.eventbus;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

@Singleton
public class EventBusFactory {

    @Getter
    private EventBus eventBus;

    @Inject
    private EventBusFactory(){
        eventBus = new EventBus();
    }
}
