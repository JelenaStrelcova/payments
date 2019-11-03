package jelstr.payment.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    private String debitAccountNumber;

    private String creditAccountNumber;

    private BigDecimal amount;

    private String currencyCode;

    private boolean fillOrKill;


}
