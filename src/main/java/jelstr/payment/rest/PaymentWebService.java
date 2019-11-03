package jelstr.payment.rest;

import com.google.inject.Inject;
import jelstr.payment.configuration.WebServiceConstants;
import jelstr.payment.entities.PaymentStatus;
import jelstr.payment.model.AccountBalanceResponse;
import jelstr.payment.model.PaymentRequest;
import jelstr.payment.model.PaymentRequestResponse;
import jelstr.payment.service.PaymentService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path(WebServiceConstants.PAYMENT_WEBSERVICE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentWebService {

    @Inject
    private PaymentService paymentService;

    @GET
    @Path("paymentstatus/{transactionId}")
    public String paymentStatus(@PathParam("transactionId") long transactionId) {
        return paymentService.getPaymentStatus(transactionId).map(PaymentStatus::name).orElse("NOT_FOUND");
    }

    @POST
    @Path("pay")
    public PaymentRequestResponse pay(PaymentRequest paymentRequest) {
        return paymentService.pay(paymentRequest);
    }

    @GET
    @Path("accountbalance")
    public AccountBalanceResponse accountBalance(@QueryParam("accountNumber") String accountNumber,
                                                 @QueryParam("currencyCode") String currencyCode) {
        return paymentService.getAccountBalance(accountNumber, currencyCode);
    }
}
