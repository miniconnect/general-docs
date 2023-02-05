package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.misc;

import java.io.Serializable;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class IdGenerator implements IdentifierGenerator {
    
    private final Random random = new Random();
    

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return random.nextInt(1000) + 1000L;
    }

}
