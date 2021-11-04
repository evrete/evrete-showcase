package org.evrete.showcase.town;

import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.api.RhsContext;
import org.evrete.api.Type;
import org.evrete.api.TypeResolver;
import org.evrete.showcase.shared.Utils;
import org.evrete.showcase.town.model.Entity;
import org.evrete.showcase.town.model.GeoData;
import org.evrete.showcase.town.model.World;
import org.evrete.showcase.town.model.WorldTime;
import org.evrete.showcase.town.rules.MainRuleset;
import org.evrete.showcase.town.rules.NonWorkingPeople;
import org.evrete.showcase.town.rules.TravelUtils;
import org.evrete.showcase.town.rules.WorkingPeople;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.function.Consumer;


@WebListener
public class AppContext implements ServletContextListener {
    static GeoData MAP_DATA;
    private static KnowledgeService knowledgeService;
    private static Knowledge knowledge;

    private static KnowledgeService knowledgeService() {
        if (knowledgeService == null) {
            throw new IllegalStateException();
        } else {
            return knowledgeService;
        }
    }

    static Knowledge knowledge() {
        return knowledge;
    }

    private static Knowledge buildKnowledge() throws Exception {
        TypeResolver resolver = knowledgeService().newTypeResolver();
        Type<Entity> entityType = resolver.declare(Entity.class);
        resolver.wrapType(new Entity.EntityKnowledgeType(entityType));

        return knowledgeService().newKnowledge("JAVA-CLASS", resolver,
                MainRuleset.class, WorkingPeople.class, NonWorkingPeople.class, TravelUtils.class
        );

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (knowledgeService == null) {
            Configuration configuration = new Configuration();
            knowledgeService = new KnowledgeService(configuration);

            ServletContext ctx = sce.getServletContext();

            try {
                knowledge = buildKnowledge();
                MAP_DATA = Utils.fromJson(
                        Utils.readResourceAsString(ctx, "/WEB-INF/data.json"),
                        GeoData.class
                );
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Can not initialize context", e);
            }

        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (knowledgeService == null) {
            throw new IllegalStateException();
        } else {
            knowledgeService.shutdown();
        }
    }

    private abstract static class PersonTimeConsumer implements Consumer<RhsContext> {
        abstract void process(Entity person, WorldTime time, World world);

        @Override
        public void accept(RhsContext ctx) {
            Entity person = ctx.get("$person");
            WorldTime time = ctx.get("$time");
            World world = ctx.get("$world");
            process(person, time, world);
            ctx.update(person);
        }
    }
}