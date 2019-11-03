package jelstr.payment.service;

import jelstr.payment.entities.Payment;
import jelstr.payment.model.ValidationResult;

public class PaymentValidator {

    public ValidationResult validate(Payment payment) {
        if (noCurrency(payment)) {
            return ValidationResult.error("Currency is required");
        }
        if (noDebitAccount(payment)) {
            return ValidationResult.error("Debit account is required");
        }

        if (noCreditAccount(payment)) {
            return ValidationResult.error("Credit account is required");
        }
        if (isSameAccountTransfer(payment)) {
            return ValidationResult.error("Debit account and credit account cannot be the same");
        }
        if (invalidAmount(payment)) {
            return ValidationResult.error("Requested amount is invalid");
        }

        return ValidationResult.OK();
    }

    private boolean invalidAmount(Payment payment) {
        return payment.getAmount() == null || payment.getAmount().signum() <= 0;
    }

    private boolean isSameAccountTransfer(Payment payment) {
        return payment.getIdentDebitAccount() == payment.getIdentCreditAccount();
    }

    private boolean noCreditAccount(Payment payment) {
        return payment.getIdentCreditAccount() == null;
    }

    private boolean noDebitAccount(Payment payment) {
        return payment.getIdentDebitAccount() == null;
    }

    private boolean noCurrency(Payment payment) {
        return payment.getIdentCurrency() == null;
    }
}
