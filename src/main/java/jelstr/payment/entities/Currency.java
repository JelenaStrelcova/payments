package jelstr.payment.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "currency")
@NamedQuery(name = Currency.FIND_BY_CODE,query = "select a from Currency a where code = :code")
@Getter
@Setter
public class Currency {
    public static final String FIND_BY_CODE = "Currency.FindByCode";

    @Id
    @Column(name = "ident_currency", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currencyIdGenerator")
    @SequenceGenerator(name="currencyIdGenerator", sequenceName = "seq_currency", allocationSize=50)
    private long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;
}
