package jelstr.payment.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {

    private BigDecimal balance;
    private String validationError;

    public static AccountBalanceResponse retrieved(BigDecimal balance){
        return AccountBalanceResponse.builder().balance(balance).build();
    }

    public static AccountBalanceResponse rejected(String validationError){
        return AccountBalanceResponse.builder().validationError(validationError).build();
    }
}
