package jelstr.payment.dao;

import com.google.inject.persist.Transactional;
import jelstr.payment.entities.PaymentStatus;
import jelstr.payment.entities.Payment;

import java.util.List;

import static jelstr.payment.entities.Payment.FIND_ALL_BY_STATUS;

public class PaymentDAO extends AbstractDAO<Payment>{

    @Override
    protected Class<Payment> getEntityClass() {
        return Payment.class;
    }

    @Transactional
    public List<Payment> findAllPendingPayments(){
        return getEntityManager().createNamedQuery(FIND_ALL_BY_STATUS, Payment.class)
                .setParameter("paymentStatus", PaymentStatus.PENDING)
                .getResultList();
    }
}
