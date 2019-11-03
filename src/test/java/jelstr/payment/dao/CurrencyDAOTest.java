package jelstr.payment.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jelstr.payment.entities.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyDAOTest extends AbstractDAOTest {

    private CurrencyDAO currencyDAO;

    @BeforeEach
    public void setUp() {
        currencyDAO = getInjector().getInstance(CurrencyDAO.class);
    }

    @Test
    void testFindByNumber() {
        Currency currency = persistNewCurrency("CUR");
        Currency result = currencyDAO.findByCode("CUR");
        assertSameAccount(currency, result);
    }

    private void assertSameAccount(Currency currency, Currency result) {
        assertEquals(currency.getId(), result.getId());
        assertEquals(currency.getCode(), result.getCode());
    }

    private Currency persistNewCurrency(String code) {
        Currency currency = new Currency();
        currency.setCode(code);
        return currencyDAO.insert(currency);
    }
}