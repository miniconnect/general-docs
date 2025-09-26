package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.transaction.annotation.Transactional;

@Singleton
@Requires(env = "demo")
public class HoloInit {

    private final EntityManager entityManager;


    public HoloInit(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    @Transactional
    public void onStartup(StartupEvent startupEvent) throws ReflectiveOperationException {
        Class.forName("hu.webarticum.holodb.jpa.JpaMetamodelDriver")
                .getMethod("setMetamodel", Object.class)
                .invoke(null, entityManager.getMetamodel());
    }

}
