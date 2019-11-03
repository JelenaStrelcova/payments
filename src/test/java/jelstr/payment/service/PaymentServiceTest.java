package jelstr.payment.service;

import com.google.common.eventbus.EventBus;
import jelstr.payment.dao.PaymentDAO;
import jelstr.payment.entities.Payment;
import jelstr.payment.model.AccountBalanceResponse;
import jelstr.payment.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jelstr.payment.dao.AccountBalanceDAO;
import jelstr.payment.entities.AccountBalance;
import jelstr.payment.eventbus.EventBusFactory;
import jelstr.payment.model.PaymentRequest;
import jelstr.payment.model.PaymentRequestResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String ACCOUNT_NUMBER = "ACC";
    private static final String CURRENCY_CODE = "CUR";
    private static final long IDENT_ACCOUNT = 1L;
    private static final long IDENT_CURRENCY = 2L;

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private EventBusFactory eventBusFactory;
    @Mock
    private PaymentValidator paymentValidator;
    @Mock
    private PaymentModelConverter paymentModelConverter;
    @Mock
    private PaymentDAO paymentDAO;
    @Mock
    private AccountBalanceDAO accountBalanceDAO;
    @Mock
    private EventBus eventBus;

    @Test
    void testValidPaymentRequest(){
        PaymentRequest paymentRequest = new PaymentRequest();
        Payment payment = givenValidPaymentRequest(paymentRequest);

        PaymentRequestResponse response = paymentService.pay(paymentRequest);

        verifyPaymentRequestIsAccepted(payment, response);
    }

    @Test
    void testInvalidPaymentRequest(){
        PaymentRequest paymentRequest = new PaymentRequest();
        Payment payment = givenInvalidPaymentRequest(paymentRequest);

        PaymentRequestResponse response = paymentService.pay(paymentRequest);

        verifyPaymentRequestIsNotAccepted(payment, response);
    }

    @Test
    public void testGetAccountBalance_returnsCorrectBalance(){
        when(paymentModelConverter.getAccountId(ACCOUNT_NUMBER)).thenReturn(IDENT_ACCOUNT);
        when(paymentModelConverter.getCurrencyId(CURRENCY_CODE)).thenReturn(IDENT_CURRENCY);

        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAmount(BigDecimal.TEN);
        when(accountBalanceDAO.findCurrentBalance(IDENT_ACCOUNT, IDENT_CURRENCY)).thenReturn(accountBalance);

        AccountBalanceResponse accountBalanceResponse = paymentService.getAccountBalance(ACCOUNT_NUMBER, CURRENCY_CODE);

        verifyBalance(accountBalanceResponse, BigDecimal.TEN);
    }

    @Test
    public void testGetAccountBalance_emptyBalance(){
        when(paymentModelConverter.getAccountId(ACCOUNT_NUMBER)).thenReturn(IDENT_ACCOUNT);
        when(paymentModelConverter.getCurrencyId(CURRENCY_CODE)).thenReturn(IDENT_CURRENCY);

        when(accountBalanceDAO.findCurrentBalance(IDENT_ACCOUNT, IDENT_CURRENCY)).thenReturn(null);

        AccountBalanceResponse accountBalanceResponse = paymentService.getAccountBalance(ACCOUNT_NUMBER, CURRENCY_CODE);

        verifyBalance(accountBalanceResponse, BigDecimal.ZERO);
    }

    @Test
    public void testGetAccountBalance_accountNotFound(){
        when(paymentModelConverter.getAccountId(ACCOUNT_NUMBER)).thenReturn(null);

        AccountBalanceResponse accountBalanceResponse = paymentService.getAccountBalance(ACCOUNT_NUMBER, CURRENCY_CODE);

        assertNull(accountBalanceResponse.getBalance());
        assertEquals("Account not found", accountBalanceResponse.getValidationError());
    }

    @Test
    public void testGetAccountBalance_currencyNotFound(){
        when(paymentModelConverter.getAccountId(ACCOUNT_NUMBER)).thenReturn(1L);
        when(paymentModelConverter.getCurrencyId(CURRENCY_CODE)).thenReturn(null);

        AccountBalanceResponse accountBalanceResponse = paymentService.getAccountBalance(ACCOUNT_NUMBER, CURRENCY_CODE);

        assertNull(accountBalanceResponse.getBalance());
        assertEquals("Currency not found", accountBalanceResponse.getValidationError());
    }

    private void verifyBalance(AccountBalanceResponse accountBalanceResponse, BigDecimal ten) {
        assertTrue(accountBalanceResponse.getBalance().compareTo(ten) == 0);
    }


    private void verifyPaymentRequestIsAccepted(Payment payment, PaymentRequestResponse response) {
        assertTrue(response.isAccepted());
        verify(paymentDAO).insert(payment);
        verify(eventBus).post(payment);
    }

    private void verifyPaymentRequestIsNotAccepted(Payment payment, PaymentRequestResponse response) {
        assertFalse(response.isAccepted());
        assertNotNull(response.getValidationError());
        verify(paymentDAO, never()).insert(payment);
        verify(eventBus, never()).post(payment);
    }

    private Payment givenValidPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        when(paymentModelConverter.toPayment(paymentRequest)).thenReturn(payment);
        when(paymentValidator.validate(payment)).thenReturn(ValidationResult.OK());
        when(paymentDAO.insert(payment)).thenReturn(payment);
        when(eventBusFactory.getEventBus()).thenReturn(eventBus);
        return payment;
    }

    private Payment givenInvalidPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        when(paymentModelConverter.toPayment(paymentRequest)).thenReturn(payment);
        when(paymentValidator.validate(payment)).thenReturn(ValidationResult.error("Invalid request"));
        return payment;
    }
}