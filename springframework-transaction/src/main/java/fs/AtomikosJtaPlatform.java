package fs;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import lombok.Setter;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

@Setter
public class AtomikosJtaPlatform extends AbstractJtaPlatform {

    private static final long serialVersionUID = 1L;

    private UserTransactionImp userTransactionImp;
    private UserTransactionManager userTransactionManager;

    public AtomikosJtaPlatform() {
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        return userTransactionManager;
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return userTransactionImp;
    }

}
