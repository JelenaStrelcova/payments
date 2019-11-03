package jelstr.payment.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jelstr.payment.entities.Account;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountDAOTest extends AbstractDAOTest{

    private AccountDAO accountDAO;

    @BeforeEach
    public void setUp() {
        accountDAO = getInjector().getInstance(AccountDAO.class);
    }

    @Test
    void testFindByNumber(){
        Account account = persistNewAccount("ACC_NUMBER");
        Account result = accountDAO.findByNumber("ACC_NUMBER");
        assertSameAccount(account, result);
    }

    private void assertSameAccount(Account account, Account result) {
        assertEquals(account.getId(), result.getId());
        assertEquals(account.getNumber(), result.getNumber());
    }

    private Account persistNewAccount(String accountNumber) {
        Account account = new Account();
        account.setNumber(accountNumber);
        return accountDAO.insert(account);
    }
}