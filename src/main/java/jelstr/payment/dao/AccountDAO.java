package jelstr.payment.dao;

import jelstr.payment.entities.Account;

import java.util.List;

import static jelstr.payment.entities.Account.FIND_BY_NUMBER;

public class AccountDAO extends AbstractDAO<Account>{

    public Account findByNumber(String number){
        List<Account> results= getEntityManager().createNamedQuery(FIND_BY_NUMBER, Account.class)
                .setParameter("number", number)
                .getResultList();
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    protected Class<Account> getEntityClass() {
        return Account.class;
    }
}
