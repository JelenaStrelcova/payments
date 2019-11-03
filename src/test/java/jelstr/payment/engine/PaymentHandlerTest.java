package jelstr.payment.engine;

import jelstr.payment.dao.AccountBalanceDAO;
import jelstr.payment.dao.PaymentDAO;
import jelstr.payment.entities.AccountBalance;
import jelstr.payment.entities.Payment;
import jelstr.payment.entities.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentHandlerTest {

    public static final long IDENT_DEBIT_ACCOUNT = 1L;
    public static final long IDENT_CREDIT_ACCOUNT = 2L;
    public static final long IDENT_CURRENCY = 3L;

    @InjectMocks
    private PaymentHandler paymentHandler;

    @Mock
    private AccountBalanceCache accountBalanceCache;
    @Mock
    private PaymentDAO paymentDAO;
    @Mock
    private AccountBalanceDAO accountBalanceDAO;

    @BeforeEach
    public void setupMocks(){
        when(accountBalanceDAO.insertNewBalance(any(AccountBalance.class), any(LocalDateTime.class)))
                .thenAnswer(AdditionalAnswers.returnsFirstArg());
    }
    @Test
    public void testPaymentIsRejectedIfNoBalancesFound() {
        Payment payment = getPayment();

        when(accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency()))
                .thenReturn(null);
        when(accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency()))
                .thenReturn(null);

        PaymentStatus paymentStatus = paymentHandler.execute(payment);

        verifyPaymentRejected(payment, paymentStatus);
    }

    @Test
    public void testPaymentIsRejectedIfDebitBalanceIsZero() {
        Payment payment = getPayment();

        when(accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency()))
                .thenReturn(getAccountBalance(payment.getIdentDebitAccount(), BigDecimal.ZERO));
        when(accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency()))
                .thenReturn(null);

        PaymentStatus paymentStatus = paymentHandler.execute(payment);

        verifyPaymentRejected(payment, paymentStatus);
    }


    @Test
    public void testPaymentIsRejectedIfBalanceIsInsufficient() {
        Payment payment = getPayment();

        when(accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency()))
                .thenReturn(getAccountBalance(payment.getIdentDebitAccount(), BigDecimal.valueOf(5)));
        when(accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency()))
                .thenReturn(null);

        PaymentStatus paymentStatus = paymentHandler.execute(payment);

        verifyPaymentRejected(payment, paymentStatus);
    }

    @Test
    public void testPaymentIsLeftToWaitIfNotFillOrKill() {
        Payment payment = getPayment();
        payment.setFillOrKill(false);

        when(accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency()))
                .thenReturn(getAccountBalance(payment.getIdentDebitAccount(), BigDecimal.valueOf(5)));
        when(accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency()))
                .thenReturn(null);

        PaymentStatus paymentStatus = paymentHandler.execute(payment);

        verifyPaymentPending(payment, paymentStatus);
    }

    @Test
    public void testPaymentIsExecutedIfBalanceIsSufficient() {
        Payment payment = getPayment();

        AccountBalance debitBalance = getAccountBalance(payment.getIdentDebitAccount(), BigDecimal.valueOf(15));
        when(accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency()))
                .thenReturn(debitBalance);
        AccountBalance creditBalance = getAccountBalance(payment.getIdentCreditAccount(), BigDecimal.valueOf(100));
        when(accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency()))
                .thenReturn(creditBalance);

        PaymentStatus paymentStatus = paymentHandler.execute(payment);

        verifyPaymentExecuted(payment, paymentStatus, debitBalance, creditBalance);
    }

    @Test
    public void testPaymentIsExecutedIfBalanceIsSufficientAndCreditAccountIsEmpty() {
        Payment payment = getPayment();

        AccountBalance debitBalance = getAccountBalance(payment.getIdentDebitAccount(), BigDecimal.valueOf(15));
        when(accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency()))
                .thenReturn(debitBalance);
        when(accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency()))
                .thenReturn(null);

        PaymentStatus paymentStatus = paymentHandler.execute(payment);

        verifyPaymentExecuted(payment, paymentStatus, debitBalance, null);
    }
    @Test
    public void testPaymentIsExecutedIfBalanceIsEqualToRequestedAmount() {
        Payment payment = getPayment();
        payment.setAmount(BigDecimal.valueOf(0.001));

        AccountBalance debitBalance = getAccountBalance(payment.getIdentDebitAccount(), BigDecimal.valueOf(0.001));
        when(accountBalanceCache.findAccountBalance(payment.getIdentDebitAccount(), payment.getIdentCurrency()))
                .thenReturn(debitBalance);
        when(accountBalanceCache.findAccountBalance(payment.getIdentCreditAccount(), payment.getIdentCurrency()))
                .thenReturn(null);

        PaymentStatus paymentStatus = paymentHandler.execute(payment);

        verifyPaymentExecuted(payment, paymentStatus, debitBalance, null);
    }

    private AccountBalance getAccountBalance(long identAccount, BigDecimal value) {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setIdentAccount(identAccount);
        accountBalance.setIdentCurrency(IDENT_CURRENCY);
        accountBalance.setAmount(value);
        accountBalance.setCurrent(true);
        return accountBalance;
    }

    private void verifyPaymentRejected(Payment payment, PaymentStatus paymentStatus) {
        assertEquals(PaymentStatus.REJECTED, paymentStatus);
        verify(paymentDAO).update(payment);
        verifyNoInteractions(accountBalanceDAO);
    }

    private void verifyPaymentPending(Payment payment, PaymentStatus paymentStatus) {
        assertEquals(PaymentStatus.PENDING, paymentStatus);
        verifyNoInteractions(paymentDAO);
        verifyNoInteractions(accountBalanceDAO);
    }

    private void verifyPaymentExecuted(Payment payment, PaymentStatus paymentStatus,  AccountBalance initialDebitBalance,
                                       AccountBalance initialCreditBalance) {
        assertEquals(PaymentStatus.EXECUTED, paymentStatus);
        assertNotNull(payment.getExecutedOn());

        verify(paymentDAO).update(payment);

        verify(accountBalanceDAO).closeCurrentBalance(eq(initialDebitBalance), any(LocalDateTime.class));
        if (initialCreditBalance != null) {
            verify(accountBalanceDAO).closeCurrentBalance(eq(initialCreditBalance), any(LocalDateTime.class));
        }

        ArgumentCaptor<AccountBalance> captor = ArgumentCaptor.forClass(AccountBalance.class);
        verify(accountBalanceDAO, times(2)).insertNewBalance(captor.capture(), any(LocalDateTime.class));

        AccountBalance finalDebitBalance = captor.getAllValues().stream().filter(ab -> ab.getIdentAccount() == payment.getIdentDebitAccount()).findFirst().get();
        AccountBalance finalCreditBalance = captor.getAllValues().stream().filter(ab -> ab.getIdentAccount() == payment.getIdentCreditAccount()).findFirst().get();

        verifyBalance(finalDebitBalance, initialDebitBalance.getAmount().subtract(payment.getAmount()));
        BigDecimal initialCreditAmount = initialCreditBalance != null ? initialCreditBalance.getAmount() : BigDecimal.ZERO;
        verifyBalance(finalCreditBalance, initialCreditAmount.add(payment.getAmount()));

        verify(accountBalanceCache).addAccountBalance(finalDebitBalance);
        verify(accountBalanceCache).addAccountBalance(finalCreditBalance);
    }

    private void verifyBalance(AccountBalance accountBalance, BigDecimal expectedBalance){
        assertEquals(0, accountBalance.getAmount().compareTo(expectedBalance));
    }


    private Payment getPayment() {
        Payment payment = new Payment();
        payment.setIdentDebitAccount(IDENT_DEBIT_ACCOUNT);
        payment.setIdentCreditAccount(IDENT_CREDIT_ACCOUNT);
        payment.setIdentCurrency(IDENT_CURRENCY);
        payment.setFillOrKill(true);
        payment.setAmount(BigDecimal.TEN);
        return payment;
    }
}