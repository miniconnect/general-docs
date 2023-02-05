package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import hu.webarticum.holodb.jpa.JpaMetamodelDriver;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

@Singleton
public class HoloInit {
    
    private final EntityManager entityManager;
    

    public HoloInit(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    
    @EventListener
    @Transactional
    public void onStartup(StartupEvent startupEvent) {
        JpaMetamodelDriver.setMetamodel(entityManager.getMetamodel());
    }
    
}
