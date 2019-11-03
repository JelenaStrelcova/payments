package jelstr.payment.service;

import com.google.inject.Inject;
import jelstr.payment.dao.PaymentDAO;
import jelstr.payment.entities.PaymentStatus;
import jelstr.payment.model.AccountBalanceResponse;
import jelstr.payment.model.PaymentRequest;
import jelstr.payment.model.PaymentRequestResponse;
import jelstr.payment.model.ValidationResult;
import lombok.extern.log4j.Log4j2;
import jelstr.payment.dao.AccountBalanceDAO;
import jelstr.payment.entities.AccountBalance;
import jelstr.payment.entities.Payment;
import jelstr.payment.eventbus.EventBusFactory;

import java.math.BigDecimal;
import java.util.Optional;

@Log4j2
public class PaymentService {

    @Inject
    private EventBusFactory eventBusFactory;
    @Inject
    private PaymentValidator paymentValidator;
    @Inject
    private PaymentModelConverter paymentModelConverter;
    @Inject
    private PaymentDAO paymentDAO;
    @Inject
    private AccountBalanceDAO accountBalanceDAO;

    public PaymentRequestResponse pay(PaymentRequest paymentRequest) {
        Payment payment = paymentModelConverter.toPayment(paymentRequest);
        ValidationResult validationResult = paymentValidator.validate(payment);
        if (validationResult.isOk()) {
            payment = paymentDAO.insert(payment);
            eventBusFactory.getEventBus().post(payment);
            return PaymentRequestResponse.accepted(payment.getId());
        } else {
            return PaymentRequestResponse.rejected(validationResult.getValidationError().get());
        }
    }

    public Optional<PaymentStatus> getPaymentStatus(long transactionId) {
        Payment payment = paymentDAO.findById(transactionId);
        PaymentStatus paymentStatus = Optional.ofNullable(payment).map(Payment::getPaymentStatus).orElse(null);
        return Optional.ofNullable(paymentStatus);
    }

    public AccountBalanceResponse getAccountBalance(String accountNumber, String currencyCode) {
        Long accountId = paymentModelConverter.getAccountId(accountNumber);
        Long currencyId = paymentModelConverter.getCurrencyId(currencyCode);
        if (accountId == null) {
            return AccountBalanceResponse.rejected("Account not found");
        }
        if (currencyId == null) {
            return AccountBalanceResponse.rejected("Currency not found");
        }
        BigDecimal balance = Optional.ofNullable(accountBalanceDAO.findCurrentBalance(accountId, currencyId))
                .map(AccountBalance::getAmount).orElse(BigDecimal.ZERO);
        return AccountBalanceResponse.retrieved(balance);
    }
}
