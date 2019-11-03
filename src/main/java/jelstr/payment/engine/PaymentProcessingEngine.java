package jelstr.payment.engine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jelstr.payment.dao.PaymentDAO;
import jelstr.payment.entities.PaymentStatus;
import lombok.extern.log4j.Log4j2;
import jelstr.payment.entities.Payment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

@Log4j2
@Singleton
public class PaymentProcessingEngine implements Runnable {

    private volatile LinkedBlockingDeque<IEngineWorkItem> workQueue = new LinkedBlockingDeque<>();
    private boolean initialized = false;

    private Map<AccountCurrencyKey, List<Payment>> waitingList = Maps.newHashMap();

    private static final IEngineWorkItem POISON_PILL = new IEngineWorkItem() {
    };

    @Inject
    private AccountBalanceCache accountBalanceCache;
    @Inject
    private PaymentHandler paymentHandler;
    @Inject
    private PaymentDAO paymentDAO;

    @Override
    public void run() {
        while (true) {
            try {
                if (!initialized) {
                    initialize();
                    log.info("Payment processing engine fully started");
                }
                IEngineWorkItem workItem = workQueue.take();
                if (workItem == POISON_PILL) {
                    log.info("Shutting down payment processing engine");
                    initialized = false;
                    break;
                }
                if (workItem instanceof Payment) {
                    handle((Payment) workItem);
                }
            } catch (Exception e) {
                log.error("Caught exception while working", e);
                initialized = false;
            }
        }
    }

    private void handle(Payment payment) {
        PaymentStatus paymentStatus = paymentHandler.execute(payment);
        switch (paymentStatus) {
            case PENDING:
                addToWaitList(payment);
                break;
            case EXECUTED:
                retryWaitingPayments(payment);
                break;
            case REJECTED:
                break;
        }
    }

    private void retryWaitingPayments(Payment executedPayment) {
        AccountCurrencyKey key = AccountCurrencyKey.builder().identAccount(executedPayment.getIdentCreditAccount())
                .identCurrency(executedPayment.getIdentCurrency()).build();
        List<Payment> waitingPayments = waitingList.get(key);
        if (waitingPayments != null) {
            waitingPayments.forEach(this::addPaymentToStartOfQueue);
        }
    }

    private void addToWaitList(Payment payment) {
        waitingList.computeIfAbsent(AccountCurrencyKey.getKey(payment), (key) -> Lists.newArrayList()).add(payment);
    }

    public void addPaymentToEndOfQueue(Payment payment) {
        workQueue.add(payment);
    }

    private void addPaymentToStartOfQueue(Payment payment) {
        workQueue.addFirst(payment);
    }

    public void shutdown() {
        workQueue.addFirst(POISON_PILL);
    }

    private void initialize() {
        workQueue.clear();
        waitingList.clear();
        accountBalanceCache.initialize();
        paymentDAO.findAllPendingPayments().forEach(this::addPaymentToEndOfQueue);
        initialized = true;
    }
}
