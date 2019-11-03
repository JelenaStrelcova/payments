package jelstr.payment.dao;

import jelstr.payment.entities.Payment;
import jelstr.payment.entities.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentDAOTest  extends AbstractDAOTest{

    private static final Long IDENT_ACCOUNT_A = 1L;
    private static final Long IDENT_ACCOUNT_B = 2L;
    private static final Long IDENT_CURRENCY = 3L;

    private PaymentDAO paymentDAO;

    @BeforeEach
    public void setUp() {
        paymentDAO = getInjector().getInstance(PaymentDAO.class);
    }

    @Test
    void testSavePayment(){
        Payment payment = buildPayment();
        payment = paymentDAO.insert(payment);

        assertNotNull(payment);
        assertNotNull(payment.getId());
        assertTrue(payment.getId() > 0);
    }

    @Test
    void testGetAcceptedPayments_onlyAcceptedPaymentsExists(){
        paymentDAO.insert(buildPayment());
        paymentDAO.insert(buildPayment());
        paymentDAO.insert(buildPayment());

        List<Payment> result = paymentDAO.findAllPendingPayments();
        assertEquals(3, result.size());
    }

    @Test
    void testGetAcceptedPayments_variousPaymentsExist(){
        Payment acceptedPayment = paymentDAO.insert(buildPayment());
        Payment executedPayment = buildPayment();
        executedPayment.setPaymentStatus(PaymentStatus.EXECUTED);
        paymentDAO.insert(executedPayment);
        Payment rejectedPayment = buildPayment();
        rejectedPayment.setPaymentStatus(PaymentStatus.REJECTED);
        paymentDAO.insert(rejectedPayment);

        List<Payment> result = paymentDAO.findAllPendingPayments();
        assertEquals(1, result.size());
        assertEquals(acceptedPayment.getId(), result.get(0).getId());
    }

    @Test
    void testGetAcceptedPayments_noPaymentsExist(){
        List<Payment> result = paymentDAO.findAllPendingPayments();
        assertEquals(0, result.size());
    }


    private Payment buildPayment() {
        Payment payment = new Payment();
        payment.setIdentDebitAccount(IDENT_ACCOUNT_A);
        payment.setIdentCreditAccount(IDENT_ACCOUNT_B);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setIdentCurrency(IDENT_CURRENCY);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        return payment;
    }


}