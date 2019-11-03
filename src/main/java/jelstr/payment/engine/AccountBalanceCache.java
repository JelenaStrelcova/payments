package jelstr.payment.engine;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import jelstr.payment.dao.AccountBalanceDAO;
import jelstr.payment.entities.AccountBalance;

import javax.inject.Inject;
import java.util.Map;

@Singleton
public class AccountBalanceCache {

    private Map<AccountCurrencyKey, AccountBalance> cache = Maps.newHashMap();

    @Inject
    private AccountBalanceDAO accountBalanceDAO;

    public void addAccountBalance(AccountBalance accountBalance){
        cache.put(AccountCurrencyKey.getKey(accountBalance), accountBalance);
    }

    public AccountBalance findAccountBalance(long identAccount, long identCurrency) {
        AccountCurrencyKey key = AccountCurrencyKey.builder()
                .identAccount(identAccount)
                .identCurrency(identCurrency).build();
        return cache.get(key);
    }

    public void initialize(){
        cache.clear();
        accountBalanceDAO.findAllCurrentBalances().forEach(this::addAccountBalance);
    }

}
