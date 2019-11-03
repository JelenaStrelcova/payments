package jelstr.payment.service;

import com.google.inject.Inject;
import jelstr.payment.dao.AccountDAO;
import jelstr.payment.dao.CurrencyDAO;
import jelstr.payment.entities.Account;
import jelstr.payment.entities.Currency;
import jelstr.payment.entities.Payment;
import jelstr.payment.model.PaymentRequest;

import java.util.Optional;

public class PaymentModelConverter {

    @Inject
    private AccountDAO accountDAO;
    @Inject
    private CurrencyDAO currencyDAO;

    public Payment toPayment(PaymentRequest paymentRequest) {

       return Payment.builder()
               .identDebitAccount(getAccountId(paymentRequest.getDebitAccountNumber()))
               .identCreditAccount(getAccountId(paymentRequest.getCreditAccountNumber()))
               .identCurrency(getCurrencyId(paymentRequest.getCurrencyCode()))
               .amount(paymentRequest.getAmount())
               .fillOrKill(paymentRequest.isFillOrKill())
               .build();
    }

    public Long getAccountId(String accountNumber) {
        if (accountNumber != null) {
            return Optional.ofNullable(accountDAO.findByNumber(accountNumber)).map(Account::getId).orElse(null);
        }
        return null;
    }

    public Long getCurrencyId(String currencyCode) {
        if (currencyCode != null) {
            return Optional.ofNullable(currencyDAO.findByCode(currencyCode)).map(Currency::getId).orElse(null);
        }
        return null;
    }
}
