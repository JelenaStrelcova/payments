package jelstr.payment.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jelstr.payment.entities.AccountBalance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountBalanceDAOTest extends AbstractDAOTest{

    private static final Long IDENT_ACCOUNT_A = 55L;
    private static final Long IDENT_CURRENCY = 340L;

    private AccountBalanceDAO accountBalanceDAO;

    @BeforeEach
    public void setUp() {
        accountBalanceDAO = getInjector().getInstance(AccountBalanceDAO.class);
    }

    @Test
    public void testInsertNewBalance(){
        AccountBalance accountBalance = createAccountBalance();

        LocalDateTime now = LocalDateTime.now();
        accountBalance = accountBalanceDAO.insertNewBalance(accountBalance, now);

        assertTrue(accountBalance.isCurrent());
        assertTrue(accountBalance.getValidFrom().equals(now));
        assertNull(accountBalance.getValidTo());
        verifyAmount(BigDecimal.TEN, accountBalance);

    }

    private void verifyAmount(BigDecimal expectedAmount, AccountBalance accountBalance) {
        assertTrue(expectedAmount.compareTo(accountBalance.getAmount()) == 0);
    }

    @Test
    public void testCloseBalance(){
        AccountBalance accountBalance = createAccountBalance();
        LocalDateTime validFrom = LocalDateTime.now();
        accountBalance = accountBalanceDAO.insertNewBalance(accountBalance, validFrom);
        LocalDateTime validTo = LocalDateTime.now();
        accountBalance = accountBalanceDAO.closeCurrentBalance(accountBalance, validTo);

        assertFalse(accountBalance.isCurrent());
        assertTrue(accountBalance.getValidFrom().equals(validFrom));
        assertTrue(accountBalance.getValidTo().equals(validTo));
        verifyAmount(BigDecimal.TEN, accountBalance);

    }

    @Test
    public void testCannotInsertNewBalanceWithNegativeAmount(){
        final AccountBalance accountBalance = createAccountBalance();
        updateAmount(accountBalance, -1);

        Assertions.assertThrows(IllegalArgumentException.class, () ->  accountBalanceDAO.insertNewBalance(accountBalance,  LocalDateTime.now()));
    }

    @Test
    public void testInsertNewBalanceWithZeroAmount(){
        AccountBalance accountBalance = createAccountBalance();
        accountBalance.setAmount(BigDecimal.ZERO);

        LocalDateTime now = LocalDateTime.now();
        accountBalance = accountBalanceDAO.insertNewBalance(accountBalance, now);

        assertTrue(accountBalance.isCurrent());
        assertTrue(accountBalance.getValidFrom().equals(now));
        assertNull(accountBalance.getValidTo());
        verifyAmount(BigDecimal.ZERO, accountBalance);

    }
    @Test
    public void testGetCurrentBalances_historicalDataExist() {
        List<AccountBalance> currentBalances = accountBalanceDAO.findAllCurrentBalances();

        AccountBalance accountBalance = createNewBalanceWithHistory();

        List<AccountBalance> result = accountBalanceDAO.findAllCurrentBalances();

        assertEquals(currentBalances.size() + 1, result.size());

        Optional<AccountBalance> retrievedBalance = findOriginalBalance(result);
        assertTrue(retrievedBalance.isPresent());
        verifyAmount(accountBalance.getAmount(), retrievedBalance.get());
    }

    private AccountBalance createNewBalanceWithHistory() {
        AccountBalance accountBalance = createAccountBalance();

        accountBalance = accountBalanceDAO.insertNewBalance(accountBalance, LocalDateTime.now());
        accountBalance = accountBalanceDAO.closeCurrentBalance(accountBalance, LocalDateTime.now());

        updateAmount(accountBalance, 20);
        return accountBalanceDAO.insertNewBalance(accountBalance, LocalDateTime.now());
    }

    private Optional<AccountBalance> findOriginalBalance(List<AccountBalance> result) {
        return result.stream()
                .filter(ab -> ab.getIdentAccount().equals(IDENT_ACCOUNT_A) && ab.getIdentCurrency().equals(IDENT_CURRENCY))
                .findFirst();
    }

    private void updateAmount(AccountBalance accountBalance, int newValue) {
        accountBalance.setAmount(BigDecimal.valueOf(newValue));
    }


    private AccountBalance createAccountBalance() {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setIdentAccount(IDENT_ACCOUNT_A);
        accountBalance.setIdentCurrency(IDENT_CURRENCY);
        accountBalance.setAmount(BigDecimal.TEN);
        return accountBalance;
    }
}