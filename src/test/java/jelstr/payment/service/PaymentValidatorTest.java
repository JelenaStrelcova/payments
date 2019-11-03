package jelstr.payment.service;

import jelstr.payment.model.ValidationResult;
import org.junit.jupiter.api.Test;
import jelstr.payment.entities.Payment;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentValidatorTest {

    private static final Long IDENT_CURRENCY = 1L;
    private static final Long IDENT_ACCOUNT_A = 1L;
    private static final Long IDENT_ACCOUNT_B = 2L;

    private PaymentValidator paymentValidator = new PaymentValidator();

    @Test
    void testHappyCase(){
        Payment payment = createValidPaymentRequest();
        ValidationResult validationResult = validate(payment);
        assertThatValidationPassed(validationResult);
    }

    @Test
    void testSourceAccountNotProvided(){
        Payment payment = createValidPaymentRequest();
        payment.setIdentDebitAccount(null);

        ValidationResult validationResult = validate(payment);
        assertThatValidationFailed(validationResult, "Debit account is required");
    }

    @Test
    void testDestinationAccountNotProvided(){
        Payment payment = createValidPaymentRequest();
        payment.setIdentCreditAccount(null);

        ValidationResult validationResult = validate(payment);
        assertThatValidationFailed(validationResult, "Credit account is required");
    }


    @Test
    void testCurrencyNotProvided(){
        Payment payment = createValidPaymentRequest();
        payment.setIdentCurrency(null);

        ValidationResult validationResult = validate(payment);
        assertThatValidationFailed(validationResult, "Currency is required");
    }

    @Test
    void testAmountNotProvided(){
        Payment payment = createValidPaymentRequest();
        payment.setAmount(null);

        ValidationResult validationResult = validate(payment);
        assertThatValidationFailed(validationResult, "Requested amount is invalid");
    }

    @Test
    void testAmountIsEqualToZero(){
        Payment payment = createValidPaymentRequest();
        payment.setAmount(BigDecimal.ZERO);

        ValidationResult validationResult = validate(payment);
        assertThatValidationFailed(validationResult, "Requested amount is invalid");
    }

    @Test
    void testAmountIsNegative(){
        Payment payment = createValidPaymentRequest();
        payment.setAmount(BigDecimal.valueOf(-5.15));

        ValidationResult validationResult = validate(payment);
        assertThatValidationFailed(validationResult, "Requested amount is invalid");
    }

    @Test
    void testSourceAccountAndDestinationAccountsAreTheSame(){
        Payment payment = createValidPaymentRequest();
        payment.setIdentCreditAccount(IDENT_ACCOUNT_A);

        ValidationResult validationResult = validate(payment);
        assertThatValidationFailed(validationResult, "Debit account and credit account cannot be the same");
    }


    private void assertThatValidationFailed(ValidationResult validationResult, String expectedErrorMessage) {
        assertFalse(validationResult.isOk());
        assertEquals(expectedErrorMessage, validationResult.getValidationError().get());
    }


    private Payment createValidPaymentRequest() {
        Payment payment = new Payment();
        payment.setIdentCurrency(IDENT_CURRENCY);
        payment.setIdentDebitAccount(IDENT_ACCOUNT_A);
        payment.setIdentCreditAccount(IDENT_ACCOUNT_B);
        payment.setAmount(BigDecimal.TEN);
        return payment;
    }

    private void assertThatValidationPassed(ValidationResult validationResult) {
        assertTrue(validationResult.isOk());
        assertTrue(validationResult.getValidationError().isEmpty());
    }

    private ValidationResult validate(Payment payment) {
        return paymentValidator.validate(payment);
    }
}