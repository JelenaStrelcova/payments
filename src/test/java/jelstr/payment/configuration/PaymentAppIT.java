package jelstr.payment.configuration;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jelstr.payment.engine.PaymentProcessingEngine;
import jelstr.payment.entities.PaymentStatus;
import jelstr.payment.model.AccountBalanceResponse;
import jelstr.payment.model.PaymentRequest;
import jelstr.payment.model.PaymentRequestResponse;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentAppIT {

    public static final String EUR = "EUR";
    public static final String ACCOUNT_A = "ACCOUNT_A";
    public static final String ACCOUNT_B = "ACCOUNT_B";
    public static final String ACCOUNT_C = "ACCOUNT_C";

    private PaymentProcessingEngine paymentProcessingEngine;

    private Injector injector;

    private Server server;

    @BeforeEach
    public void setup() throws Exception {
        injector = Guice.createInjector(new PersistenceModule(),
                new JettyModule(),
                new WebServiceModule(),
                new RestEasyModule());
        PaymentAppInitializer paymentAppInitializer = injector.getInstance(PaymentAppInitializer.class);
        paymentAppInitializer.run(injector);
        server = paymentAppInitializer.server;
        paymentProcessingEngine = paymentAppInitializer.paymentProcessingEngine;
    }

    @AfterEach
    public void cleanup() throws Exception {
        paymentProcessingEngine.shutdown();
        server.stop();
    }

    @Test
    public void testPaySuccessfully() {
        WebTarget target = getWebTarget();
        BigDecimal startDebitBalance = getAccountBalance(ACCOUNT_A, target);
        BigDecimal startCreditBalance = getAccountBalance(ACCOUNT_B, target);

        PaymentRequest paymentRequest = createPaymentRequest(ACCOUNT_A, ACCOUNT_B, BigDecimal.TEN, false);

        PaymentRequestResponse paymentRequestResponse = postPayment(paymentRequest, target);

        verifyPaymentIsExecuted(target, paymentRequestResponse);

        verifyBalance(ACCOUNT_A, startDebitBalance.subtract(BigDecimal.TEN));
        verifyBalance(ACCOUNT_B, startCreditBalance.add(BigDecimal.TEN));
    }

    @Test
    public void testPay_NotEnoughBalance_KillImmediately() {
        WebTarget target = getWebTarget();
        BigDecimal startDebitBalance = getAccountBalance(ACCOUNT_A, target);
        BigDecimal startCreditBalance = getAccountBalance(ACCOUNT_B, target);

        PaymentRequest paymentRequest = createPaymentRequest(ACCOUNT_A, ACCOUNT_B, BigDecimal.valueOf(20000), true);
        PaymentRequestResponse paymentRequestResponse = postPayment(paymentRequest, target);

        verifyPaymentIsRejected(paymentRequestResponse, target);

        verifyBalance(ACCOUNT_A, startDebitBalance);
        verifyBalance(ACCOUNT_B, startCreditBalance);
    }

    private PaymentRequestResponse postPayment(PaymentRequest paymentRequest, WebTarget target) {
        Response response = target.path("pay")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(paymentRequest, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        return response.readEntity(PaymentRequestResponse.class);
    }

    private WebTarget getWebTarget() {
        return ClientBuilder.newClient().target(server.getURI()).path(WebServiceConstants.PAYMENT_WEBSERVICE_PATH);
    }

    @Test
    public void testPay_NotEnoughBalance_WaitForTheFunds() {
        WebTarget target = getWebTarget();
        BigDecimal startDebitBalance = getAccountBalance(ACCOUNT_A, target);

        PaymentRequest paymentRequest = createPaymentRequest(ACCOUNT_B, ACCOUNT_C, startDebitBalance, false);
        PaymentRequestResponse paymentRequestResponse = postPayment(paymentRequest, target);
        verifyPaymentIsPending(paymentRequestResponse, target);

        PaymentRequest paymentRequest2 = createPaymentRequest(ACCOUNT_A, ACCOUNT_B, startDebitBalance, false);

        PaymentRequestResponse paymentRequestResponse2 = postPayment(paymentRequest2, target);

        verifyPaymentIsExecuted(target, paymentRequestResponse2);
        verifyPaymentIsExecuted(target, paymentRequestResponse);

        verifyBalance(ACCOUNT_A, BigDecimal.ZERO);
        verifyBalance(ACCOUNT_B, BigDecimal.ZERO);
        verifyBalance(ACCOUNT_C, startDebitBalance);
    }

    @Test
    public void testPay_MultipleThreads() throws InterruptedException, ExecutionException {

        Callable<List<PaymentRequestResponse>> task = () -> {
            final WebTarget webTarget = getWebTarget();
            return IntStream.range(0, 10).mapToObj(i -> {
                PaymentRequest paymentRequest = createPaymentRequest(ACCOUNT_A, ACCOUNT_B, BigDecimal.valueOf(100), true);
                return postPayment(paymentRequest, webTarget);
            }).collect(Collectors.toList());
        };

        int threadCount = 10;
        List<PaymentRequestResponse> resultList = executeInMultipleThreads(task, threadCount);

        final WebTarget webTarget = getWebTarget();
        Map<PaymentStatus, Long> result = resultList.stream()
                .map(response -> waitForFinalStatus(response, webTarget))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertEquals(10, result.get(PaymentStatus.EXECUTED));
        assertEquals(90, result.get(PaymentStatus.REJECTED));

        verifyBalance(ACCOUNT_A, BigDecimal.ZERO);
        verifyBalance(ACCOUNT_B, BigDecimal.valueOf(1000));
    }

    private List<PaymentRequestResponse> executeInMultipleThreads(Callable<List<PaymentRequestResponse>> task, int threadCount) throws InterruptedException, ExecutionException {
        List<Callable<List<PaymentRequestResponse>>> tasks = Collections.nCopies(threadCount, task);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<List<PaymentRequestResponse>>> futures = executorService.invokeAll(tasks);
        List<PaymentRequestResponse> resultList = new ArrayList<>();
        for (Future<List<PaymentRequestResponse>> future : futures) {
            resultList.addAll(future.get());
        }
        return resultList;
    }

    @Test
    public void testPay_MultipleTimes() {
        final WebTarget webTarget = getWebTarget();

        List<PaymentRequestResponse> requestResponses = IntStream.range(0, 100).mapToObj(i -> {
            PaymentRequest paymentRequest = createPaymentRequest(ACCOUNT_A, ACCOUNT_B, BigDecimal.valueOf(100), true);
            return postPayment(paymentRequest, webTarget);
        }).collect(Collectors.toList());

        Map<PaymentStatus, Long> result = requestResponses.stream()
                .map(response -> waitForFinalStatus(response, getWebTarget()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertEquals(10, result.get(PaymentStatus.EXECUTED));
        assertEquals(90, result.get(PaymentStatus.REJECTED));

        verifyBalance(ACCOUNT_A, BigDecimal.ZERO);
        verifyBalance(ACCOUNT_B, BigDecimal.valueOf(1000));

    }


    @Test
    public void testRestartPaymentProcessingEngine() {
        WebTarget target = getWebTarget();
        BigDecimal startDebitBalance = getAccountBalance(ACCOUNT_A, target);
        BigDecimal startCreditBalance = getAccountBalance(ACCOUNT_B, target);

        PaymentRequest paymentRequest = createPaymentRequest(ACCOUNT_A, ACCOUNT_B, BigDecimal.TEN, false);

        paymentProcessingEngine.shutdown();

        PaymentRequestResponse paymentRequestResponse = postPayment(paymentRequest, target);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(paymentProcessingEngine);

        verifyPaymentIsExecuted(target, paymentRequestResponse);

        verifyBalance(ACCOUNT_A, startDebitBalance.subtract(BigDecimal.TEN));
        verifyBalance(ACCOUNT_B, startCreditBalance.add(BigDecimal.TEN));
    }


    private PaymentRequest createPaymentRequest(String debitAccount, String creditAccount, BigDecimal amount, boolean fillOrKill) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setDebitAccountNumber(debitAccount);
        paymentRequest.setCreditAccountNumber(creditAccount);
        paymentRequest.setCurrencyCode("EUR");
        paymentRequest.setAmount(amount);
        paymentRequest.setFillOrKill(fillOrKill);
        return paymentRequest;
    }

    private void verifyPaymentIsExecuted(WebTarget target, PaymentRequestResponse response) {
        verifyPaymentStatus(response, target, PaymentStatus.EXECUTED);
    }

    private String getPaymentStatusResponse(WebTarget target, PaymentRequestResponse response) {
        Response paymentStatusResponse = target.path("paymentstatus/" + response.getTransactionId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertEquals(Response.Status.OK.getStatusCode(), paymentStatusResponse.getStatus());
        return paymentStatusResponse.readEntity(String.class);
    }

    private void verifyPaymentIsRejected(PaymentRequestResponse response, WebTarget target) {
        verifyPaymentStatus(response, target, PaymentStatus.REJECTED);
    }

    private void verifyPaymentStatus(PaymentRequestResponse response, WebTarget target, PaymentStatus expectedStatus) {
        assertTrue(response.isAccepted());
        await().atMost(5, SECONDS).until(() -> {
            String paymentStatus = getPaymentStatusResponse(target, response);
            return paymentStatus.equals(expectedStatus.name());
        });
    }

    private PaymentStatus waitForFinalStatus(PaymentRequestResponse response, WebTarget target) {
        Set<String> finalStatuses = Sets.newHashSet(PaymentStatus.EXECUTED.name(), PaymentStatus.REJECTED.name());
        String status = getPaymentStatusResponse(target, response);
        assertNotEquals("NOT_FOUND", status);
        assertTrue(response.isAccepted());
        await().atMost(10, SECONDS).until(() ->
                finalStatuses.contains(getPaymentStatusResponse(getWebTarget(), response))
        );
        return PaymentStatus.valueOf(getPaymentStatusResponse(getWebTarget(), response));
    }

    private BigDecimal getAccountBalance(String accountNumber, WebTarget target) {
        Response response = target.path("accountbalance")
                .queryParam("accountNumber", accountNumber)
                .queryParam("currencyCode", EUR)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        return response.readEntity(AccountBalanceResponse.class).getBalance();
    }

    private void verifyPaymentIsPending(PaymentRequestResponse response, WebTarget target) {
        verifyPaymentStatus(response, target, PaymentStatus.PENDING);
    }

    private void verifyBalance(String accountNumber, BigDecimal expectedBalance){
        assertEquals(0, getAccountBalance(accountNumber, getWebTarget()).compareTo(expectedBalance));
    }
}
