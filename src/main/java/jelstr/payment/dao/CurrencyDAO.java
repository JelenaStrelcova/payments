package jelstr.payment.dao;

import jelstr.payment.entities.Currency;

import java.util.List;

import static jelstr.payment.entities.Currency.FIND_BY_CODE;

public class CurrencyDAO extends AbstractDAO<Currency>{

    public Currency findByCode(String code){
        List<Currency> results= getEntityManager().createNamedQuery(FIND_BY_CODE, Currency.class)
                .setParameter("code", code)
                .getResultList();
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    protected Class<Currency> getEntityClass() {
        return Currency.class;
    }
}
