package jelstr.payment.entities;

import lombok.*;
import jelstr.payment.engine.IEngineWorkItem;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jelstr.payment.entities.Payment.FIND_ALL_BY_STATUS;

@Entity
@Table(name = "payment")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NamedQuery(name = FIND_ALL_BY_STATUS, query = "select p from Payment p where paymentStatus = :paymentStatus" +
        " order by acceptedOn asc")
public class Payment implements IEngineWorkItem {

    public static final String FIND_ALL_BY_STATUS = "Payment.FindAllByStatus";
    @Id
    @Column(name = "ident_payment", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paymentIdGenerator")
    @SequenceGenerator(name="paymentIdGenerator", sequenceName = "seq_payment", allocationSize=50)
    private long id;
    @Column(name = "ident_debit_account", nullable = false)
    private Long identDebitAccount;
    @Column(name = "ident_credit_account", nullable = false)
    private Long identCreditAccount;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "ident_currency", nullable = false)
    private Long identCurrency;
    @Column(name = "fill_or_kill", nullable = false)
    private boolean fillOrKill;
    @Column(name = "accepted_on", nullable = false)
    @Builder.Default
    private LocalDateTime acceptedOn = LocalDateTime.now();
    @Column(name = "executed_on")
    private LocalDateTime executedOn;

    @Builder.Default
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

}
