package jelstr.payment.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Builder
@Getter
public class ValidationResult {

    private boolean ok;

    private Optional<String> validationError;

    public static ValidationResult OK(){
        return ValidationResult.builder().ok(true).validationError(Optional.empty()).build();
    }

    public static ValidationResult error(String validationError){
        return ValidationResult.builder().ok(false).validationError(Optional.of(validationError)).build();
    }
}
