package jelstr.payment.engine;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import jelstr.payment.entities.Payment;

@Log4j2
@Singleton
public class PaymentTransactionListener {

    @Inject
    private PaymentProcessingEngine paymentProcessingEngine;

    @Subscribe
    public void onEvent(Payment payment){
        log.info("Got payment transaction {}", payment);
        paymentProcessingEngine.addPaymentToEndOfQueue(payment);
    }
}
