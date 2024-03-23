package org.evrete.guides.showcase.town;

import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.api.Type;
import org.evrete.api.TypeResolver;
import org.evrete.guides.showcase.AbstractWebsocketHandler;
import org.evrete.guides.showcase.MessageWriter;
import org.evrete.guides.showcase.ShowcaseUtils;
import org.evrete.guides.showcase.town.dto.Entity;
import org.evrete.guides.showcase.town.dto.GeoData;
import org.evrete.guides.showcase.town.rules.MainRuleset;
import org.evrete.guides.showcase.town.rules.NonWorkingPeople;
import org.evrete.guides.showcase.town.rules.TravelUtils;
import org.evrete.guides.showcase.town.rules.WorkingPeople;

import java.io.IOException;

public class TownWebsocketHandler extends AbstractWebsocketHandler<TownWebsocketSession> {
    private final Knowledge knowledge;
    private final GeoData geoData;

    public TownWebsocketHandler() {
        try {
            KnowledgeService knowledgeService = new KnowledgeService();

            TypeResolver resolver = knowledgeService.newTypeResolver();
            Type<Entity> entityType = resolver.declare(Entity.class);
            resolver.wrapType(new Entity.EntityKnowledgeType(entityType));

            this.geoData = ShowcaseUtils.fromJson(ShowcaseUtils.readResourceAsString("/META-INF/data/town/homes.json"), GeoData.class);
            this.knowledge = knowledgeService.newKnowledge("JAVA-CLASS", resolver,
                    MainRuleset.class, WorkingPeople.class, NonWorkingPeople.class, TravelUtils.class
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected TownWebsocketSession newSession(MessageWriter writer) {

        return new TownWebsocketSession(writer, knowledge, geoData);
    }
}
