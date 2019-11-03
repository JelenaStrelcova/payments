package jelstr.payment.dao;

import com.google.inject.persist.Transactional;
import jelstr.payment.entities.AccountBalance;

import java.time.LocalDateTime;
import java.util.List;

import static jelstr.payment.entities.AccountBalance.FIND_ALL_CURRENT_BALANCES;
import static jelstr.payment.entities.AccountBalance.FIND_CURRENT_BALANCE;

public class AccountBalanceDAO extends AbstractDAO<AccountBalance> {

    @Transactional
    public List<AccountBalance> findAllCurrentBalances() {
        return getEntityManager().createNamedQuery(FIND_ALL_CURRENT_BALANCES, AccountBalance.class)
                .getResultList();
    }

    public AccountBalance findCurrentBalance(long identAccount, long identCurrency) {
        List<AccountBalance> balances = getEntityManager().createNamedQuery(FIND_CURRENT_BALANCE, AccountBalance.class)
                .setParameter("identAccount", identAccount)
                .setParameter("identCurrency", identCurrency)
                .getResultList();
        if (balances.size() == 1) {
            return balances.get(0);
        }
        return null;
    }

    public AccountBalance closeCurrentBalance(AccountBalance accountBalance, LocalDateTime now) {
        accountBalance.setCurrent(false);
        accountBalance.setValidTo(now);
        return update(accountBalance);
    }

    public AccountBalance insertNewBalance(AccountBalance accountBalance, LocalDateTime now) {
        if (accountBalance.getAmount().signum() < 0) {
            throw new IllegalArgumentException("Cannot update account balance to negative value");
        }
        accountBalance.setCurrent(true);
        accountBalance.setValidFrom(now);
        accountBalance.setValidTo(null);
        return insert(accountBalance);
    }

    @Override
    protected Class<AccountBalance> getEntityClass() {
        return AccountBalance.class;
    }

}
