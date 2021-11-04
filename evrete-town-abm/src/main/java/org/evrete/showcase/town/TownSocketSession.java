package org.evrete.showcase.town;

import org.evrete.api.*;
import org.evrete.showcase.shared.AbstractSocketSession;
import org.evrete.showcase.town.model.World;
import org.evrete.showcase.town.model.WorldTime;
import org.evrete.showcase.town.ui.*;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TownSocketSession extends AbstractSocketSession {
    private State sessionState;

    Map<String, String> ruleNameIdMap = new HashMap<>();
    private final Knowledge knowledge;

    TownSocketSession(Session session) {
        super(session);
        this.knowledge = AppContext.knowledge();
        int ruleId = 0;
        List<JsonRule> rules = new ArrayList<>();
        for (Rule r : knowledge.getRules()) {
            String id = "rule-id-" + ruleId;
            String name = r.getName();
            this.ruleNameIdMap.put(name, id);
            rules.add(new JsonRule(id, name));
            ruleId++;
        }

        RunConfig initialConfig = new RunConfig();
        initialConfig.rules.addAll(rules);
        send("CONFIGURATION", initialConfig);
    }

    @Override
    public void process(String type, String payload) {
        switch (type) {
            case "VIEWPORT":
                setViewport(fromJson(payload, Viewport.class));
                break;
            case "START":
                RunConfig config = fromJson(payload, RunConfig.class);
                start(config);
                break;
            case "NEXT":
                next(Integer.parseInt(payload));
                break;
            case "STOP":
                stop();
                send("END");
                break;
            case "UPDATE":
                RunConfig cfg = fromJson(payload, RunConfig.class);
                if (sessionState != null) {
                    sessionState.update(cfg);
                }

                break;
            default:
                send("ERROR", "Unknown command: '" + type + "'");
        }
    }

    void setViewport(Viewport viewport) {
        if (sessionState != null) {
            sessionState.visualState.setViewport(viewport);
        }
    }

    void start(RunConfig config) {
        if (sessionState != null) {
            this.sessionState.close();
        }

        World world = World.factory(AppContext.MAP_DATA, config.population);
        send("LOG", String.format(
                "Data initialized. residents: %d, working: %d%%, homes: %d, businesses: %d",
                world.population.size(),
                config.workingPercent,
                world.homes.size(),
                world.businesses.size())
        );
        StatefulSession session = knowledge.newStatefulSession(ActivationMode.CONTINUOUS);
        this.sessionState = new State(session, world, config, ruleNameIdMap);
        send("STATE", this.sessionState.state());
    }

    private void next(int interval) {
        if (this.sessionState != null) {
            if (sessionState.time.seconds() < 3600 * 24 * 7) {
                send("STATE", this.sessionState.nextTime(interval));
            } else {
                send("END", "Session timeout");
                stop();
            }
        }
    }

    @Override
    public void closeSession() {
        if (sessionState != null) {
            sessionState.close();
            sessionState = null;
        }
    }

    void stop() {
        closeSession();
    }

    // A collection of state variables associated with a session
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
