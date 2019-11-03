package jelstr.payment.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "account")
@NamedQuery(name = Account.FIND_BY_NUMBER,query = "select a from Account a where number = :number")
@Getter
@Setter
public class Account {

    public static final String FIND_BY_NUMBER = "Account.FindByNumber";

    @Id
    @Column(name = "ident_account", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountIdGenerator")
    @SequenceGenerator(name="accountIdGenerator", sequenceName = "seq_account", allocationSize=50)
    private long id;

    @Column(name = "number", nullable = false, unique = true)
    private String number;
}
