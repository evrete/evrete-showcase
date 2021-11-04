package org.evrete.showcase.town;

import org.evrete.KnowledgeService;
import org.evrete.api.*;
import org.evrete.showcase.shared.Utils;
import org.evrete.showcase.town.model.Entity;
import org.evrete.showcase.town.model.GeoData;
import org.evrete.showcase.town.model.World;
import org.evrete.showcase.town.model.WorldTime;
import org.evrete.showcase.town.rules.MainRuleset;
import org.evrete.showcase.town.rules.NonWorkingPeople;
import org.evrete.showcase.town.rules.TravelUtils;
import org.evrete.showcase.town.rules.WorkingPeople;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class Debug {
    public static void main(String[] args)throws Exception {
        File f = new File("evrete-town-abm/src/main/webapp/WEB-INF/data.json");
        byte[] bytes =  Files.readAllBytes(f.toPath());
        GeoData data = Utils.fromJson(new String(bytes), GeoData.class);

        KnowledgeService service = new KnowledgeService();

        TypeResolver resolver = service.newTypeResolver();
        Type<Entity> entityType = resolver.declare(Entity.class);
        resolver.wrapType(new Entity.EntityKnowledgeType(entityType));

        Knowledge knowledge = service.newKnowledge("JAVA-CLASS", resolver,
                MainRuleset.class, WorkingPeople.class, TravelUtils.class, NonWorkingPeople.class
        );
        WorldTime time = new WorldTime();
        World world = World.factory(data, 1);

        StatefulSession session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS);
        session.setActivationManager(new ActivationManager() {
            @Override
            public void onAgenda(int sequenceId, List<RuntimeRule> agenda) {
                if(sequenceId > 100) {
                    throw new IllegalStateException();
                }
            }
        });

        session.set("world", world);
        session.set("working-probability", 0.0);
        session.set("stay-home-probability", 1.0);
        session.set("commute-speed", 0.3);
        FactHandle timeHandle = session.insert(time);
        session.insert(world.population);
        session.fire();

        int interval = 60;
        while (time.seconds() < 3600 * 24 * 2) {
            //System.out.println("Time: " + time.seconds());
            session.update(timeHandle, time.increment(interval));
            session.fire();
        }
        System.out.println("==============");
        session.close();
        service.shutdown();

    }
}
