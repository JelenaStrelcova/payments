package jelstr.payment.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accountbalance")
@Getter
@Setter
@NamedQuery(name = AccountBalance.FIND_ALL_CURRENT_BALANCES, query = "select a from AccountBalance a where current = true")
@NamedQuery(name = AccountBalance.FIND_CURRENT_BALANCE, query = "select a from AccountBalance a where current = true and identAccount = :identAccount and identCurrency = :identCurrency")
public class AccountBalance {

    public static final String FIND_ALL_CURRENT_BALANCES = "AccountBalance.FindAllCurrentBalances";
    public static final String FIND_CURRENT_BALANCE = "AccountBalance.FindCurrentBalance";

    @Id
    @Column(name = "ident_accountbalance", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "balanceIdGenerator")
    @SequenceGenerator(name="balanceIdGenerator", sequenceName = "seq_accountbalance", allocationSize=50)
    private long id;
    @Column(name = "ident_account", nullable = false)
    private Long identAccount;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "ident_currency", nullable = false)
    private Long identCurrency;
    @Column(name = "current", nullable = false)
    private boolean current;
    @Column(name = "validfrom", nullable = false)
    private LocalDateTime validFrom;
    @Column(name = "validto")
    private LocalDateTime validTo;

    public AccountBalance copy(){
        AccountBalance copy = new AccountBalance();
        copy.setIdentAccount(this.getIdentAccount());
        copy.setIdentCurrency(this.getIdentCurrency());
        copy.setAmount(this.getAmount());
        return copy;
    }
}
