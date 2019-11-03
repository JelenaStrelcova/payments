package jelstr.payment.engine;

import jelstr.payment.entities.Payment;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import jelstr.payment.entities.AccountBalance;

@Builder
@EqualsAndHashCode
public final class AccountCurrencyKey {

    public static AccountCurrencyKey getKey(AccountBalance accountBalance) {
        return AccountCurrencyKey.builder()
                .identAccount(accountBalance.getIdentAccount())
                .identCurrency(accountBalance.getIdentCurrency()).build();
    }

    public static AccountCurrencyKey getKey(Payment payment) {
        return AccountCurrencyKey.builder()
                .identAccount(payment.getIdentDebitAccount())
                .identCurrency(payment.getIdentCurrency()).build();
    }

    private long identAccount;
    private long identCurrency;
}
