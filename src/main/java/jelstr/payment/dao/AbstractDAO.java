package jelstr.payment.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.hibernate.Session;

import javax.persistence.EntityManager;

@Transactional
@Singleton
public abstract class AbstractDAO<T> {

    @Inject
    private Provider<EntityManager> entityManager;

    private Session getSession() {
        return getEntityManager().unwrap(Session.class);
    }

    protected EntityManager getEntityManager() {
        return entityManager.get();
    }

    public T findById(long id) {
        return getEntityManager().find(getEntityClass(), id);
    }

    public T insert(T entity) {
        getSession().save(entity);
        return entity;
    }

    public T update(T entity) {
        getSession().update(entity);
        return entity;
    }

    protected abstract Class<T> getEntityClass();
}
