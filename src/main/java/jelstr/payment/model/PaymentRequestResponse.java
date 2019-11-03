package jelstr.payment.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestResponse {

    private boolean accepted;

    private String validationError;

    private long transactionId;

    public static PaymentRequestResponse accepted(long transactionId){
        return PaymentRequestResponse.builder().accepted(true).transactionId(transactionId).build();
    }

    public static PaymentRequestResponse rejected(String validationError){
        return PaymentRequestResponse.builder().accepted(false).validationError(validationError).build();
    }

}
