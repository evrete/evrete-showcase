package org.evrete.guides.showcase.town;

import org.evrete.api.*;
import org.evrete.guides.showcase.AbstractWebsocketSession;
import org.evrete.guides.showcase.Message;
import org.evrete.guides.showcase.MessageWriter;
import org.evrete.guides.showcase.ShowcaseUtils;
import org.evrete.guides.showcase.town.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TownWebsocketSession extends AbstractWebsocketSession {
    private State sessionState;

    Map<String, String> ruleNameIdMap = new HashMap<>();

    private final GeoData geoData;
    private final Knowledge knowledge;

    private final List<JsonRule> rules = new ArrayList<>();

    public TownWebsocketSession(MessageWriter writer, Knowledge knowledge, GeoData geoData) {
        super(writer);
        this.geoData = geoData;
        this.knowledge = knowledge;

        int ruleId = 0;
        for (Rule r : knowledge.getRules()) {
            String id = "rid" + ruleId;
            String name = r.getName();
            this.ruleNameIdMap.put(name, id);
            rules.add(new JsonRule(id, name));
            ruleId++;
        }
    }

    @Override
    protected void onCreate() {
        RunConfig initialConfig = new RunConfig();
        initialConfig.rules.addAll(rules);
        writer.writeMessage("CONFIGURATION", initialConfig);
    }

    @Override
    protected void onMessage(String body) {
        Message message = ShowcaseUtils.fromJson(body, Message.class);
        switch (message.type()) {
            case "START" -> start(message.readPayloadAs(RunConfig.class));
            case "NEXT" -> onNext(Integer.parseInt(message.payload()));
            case "UPDATE" -> onUpdate(message.readPayloadAs(RunConfig.class));
            case "VIEWPORT" -> onViewport(message.readPayloadAs(Viewport.class));
            case "STOP" -> onStop();
            default -> throw new IllegalStateException("Unknown message type: " + body);
        }
    }

    private void onUpdate(RunConfig cfg) {
        if (sessionState != null) {
            sessionState.update(cfg);
        }
    }

    void onViewport(Viewport viewport) {
        if (sessionState != null) {
            sessionState.visualState.setViewport(viewport);
        }
    }

    private void onStop() {
        stop();
        writer.writeMessage("END", "Session stopped");
    }

    private void onNext(int interval) {
        if (this.sessionState != null) {
            if (sessionState.time.seconds() < 3600 * 24 * 7) {
                writer.writeMessage("STATE", this.sessionState.nextTime(interval));
            } else {
                writer.writeMessage("END", "Session timeout");
                stop();
            }
        }
    }

    void stop() {
        closeSession();
    }

    public void closeSession() {
        if (sessionState != null) {
            sessionState.close();
            sessionState = null;
        }
    }

    @Override
    protected void onClose() {
        this.closeSession();
    }

    void start(RunConfig config) {
        if (sessionState != null) {
            this.sessionState.close();
        }

        World world = World.factory(geoData, config.population);
        writer.writeMessage("LOG", String.format(
                "Data initialized. Residents: %d, working: %d%%, homes: %d, businesses: %d",
                world.population.size(),
                config.workingPercent,
                world.homes.size(),
                world.businesses.size())
        );
        StatefulSession session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS);
        this.sessionState = new State(session, world, config, ruleNameIdMap);
        writer.writeMessage("STATE", this.sessionState.state());
    }

    private static class State {
        final StatefulSession session;
        FactHandle timeHandle;
        VisualState visualState;
        WorldTime time;
        Viewport viewport;
        final Map<String, String> ruleNameIdMap;
        final Map<String, Long> ruleActivity = new HashMap<>();

        public State(StatefulSession session, World world, RunConfig config, Map<String, String> ruleNameIdMap) {
            this.ruleNameIdMap = ruleNameIdMap;
            this.session = session;
            this.time = new WorldTime();
            this.viewport = config.viewport;
            this.visualState = new VisualState(world, viewport);

            session.setActivationManager(new ActivationManager() {
                @Override
                public void onAgenda(int sequenceId, List<RuntimeRule> agenda) {
                    if (sequenceId > 100) {
                        throw new IllegalStateException("Rules result in infinite loop");
                    }
                }

                @Override
                public void onActivation(RuntimeRule rule, long count) {
                    String ruleId = ruleNameIdMap.get(rule.getName());
                    ruleActivity.merge(ruleId, count, Long::sum);
                }
            });

            session.set("world", world);
            session.insert(world.population);
            timeHandle = session.insert(time);
            update(config);
            session.fire();
        }

        void update(RunConfig config) {
            session.set("working-probability", config.workingPercent / 100.0);
            session.set("stay-home-probability", config.leisureAtHomePercent / 100.0);
            session.set("commute-speed", config.commuteSpeed / 100.0);
        }

        StateMessage nextTime(int delay) {
            this.time.increment(delay);
            session.update(timeHandle, time);
            session.fire();
            return state();
        }

        StateMessage state() {
            return visualState.getState(time).setActivity(ruleActivity);
        }

        void close() {
            session.close();
        }
    }

}
