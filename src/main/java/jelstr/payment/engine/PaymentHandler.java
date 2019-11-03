package jelstr.payment.engine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import jelstr.payment.dao.PaymentDAO;
import jelstr.payment.entities.Payment;
import jelstr.payment.entities.PaymentStatus;
import lombok.extern.log4j.Log4j2;
import jelstr.payment.dao.AccountBalanceDAO;
import jelstr.payment.entities.AccountBalance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Log4j2
@Singleton
public class PaymentHandler {

    @Inject
    private AccountBalanceCache accountBalanceCache;
    @Inject
    private PaymentDAO paymentDAO;
    @Inject
    private AccountBalanceDAO accountBalanceDAO;

    @Transactional
    public PaymentStatus execute(Payment payment) {
        log.info("Processing payment {}", payment);
        AccountBalance debitBalance = accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency());
        AccountBalance creditBalance = accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency());
        if (isEnoughBalance(payment, debitBalance)) {
            LocalDateTime executedOn = LocalDateTime.now();
            updateBalances(payment, debitBalance, creditBalance, executedOn);
            executePayment(payment, executedOn);
            log.info("Payment {} is executed", payment);
        } else if (payment.isFillOrKill()) {
            rejectPayment(payment);
            log.info("Payment {} is rejected, not enough balance", payment);
        } else {
            log.info("Payment {} does not have enough balance, recycling", payment);
        }
        return payment.getPaymentStatus();
    }

    private void rejectPayment(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.REJECTED);
        paymentDAO.update(payment);
    }

    private void executePayment(Payment payment, LocalDateTime executedOn) {
        payment.setExecutedOn(executedOn);
        payment.setPaymentStatus(PaymentStatus.EXECUTED);
        paymentDAO.update(payment);
    }

    private void updateBalances(Payment payment, AccountBalance debitBalance, AccountBalance creditBalance, LocalDateTime executedOn) {
        AccountBalance newDebitBalance = debitBalance.copy();
        accountBalanceDAO.closeCurrentBalance(debitBalance, executedOn);

        if (creditBalance == null) {
            creditBalance = createEmptyDestinationBalance(payment);
        } else {
            accountBalanceDAO.closeCurrentBalance(creditBalance, executedOn);
        }
        AccountBalance newCreditBalance = creditBalance.copy();

        newDebitBalance.setAmount(debitBalance.getAmount().subtract(payment.getAmount()));
        newCreditBalance.setAmount(creditBalance.getAmount().add(payment.getAmount()));

        accountBalanceCache.addAccountBalance(accountBalanceDAO.insertNewBalance(newDebitBalance, executedOn));
        accountBalanceCache.addAccountBalance(accountBalanceDAO.insertNewBalance(newCreditBalance, executedOn));
    }

    private boolean isEnoughBalance(Payment payment, AccountBalance debitBalance) {
        return debitBalance != null && debitBalance.getAmount().compareTo(payment.getAmount()) >= 0;
    }

    private AccountBalance createEmptyDestinationBalance(Payment payment) {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAmount(BigDecimal.ZERO);
        accountBalance.setIdentCurrency(payment.getIdentCurrency());
        accountBalance.setIdentAccount(payment.getIdentCreditAccount());
        accountBalance.setCurrent(true);
        return accountBalance;
    }

}
