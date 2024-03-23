package org.evrete.guides.showcase.town.rules;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.*;
import org.evrete.guides.showcase.town.RandomUtils;
import org.evrete.guides.showcase.town.dto.Entity;
import org.evrete.guides.showcase.town.dto.World;
import org.evrete.guides.showcase.town.dto.WorldTime;

@RuleSet
public class NonWorkingPeople extends Commons {
    public static int STATE_INITIAL = 1;
    public static int STATE_HAVING_FUN = 2;
    public static int STATE_END_OF_DAY = 3;
    private double stayHomeProbability;

    private World world;

    @EnvironmentListener("stay-home-probability")
    public void initWorkingProbability(double p) {
        this.stayHomeProbability = p;
        LOGGER.fine("Leisure at home probability is set to: " + p);
    }


    @EnvironmentListener("world")
    public void initWorld(World world) {
        this.world = world;
    }


    /**
     * <p>
     *     Non-working people's day planning. The rule decides whether a person stays home or
     *     goes out today.
     * </p>
     */
    @Rule(value = "Non-working. Day planning", salience = 10)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.flags.day_planning == true",
            "$p.properties.work == null",
    })
    public void dayPlanning(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        // Will the person stay at home the whole day or go out?
        boolean stayHome = RandomUtils.randomBoolean(stayHomeProbability);

        person
                .set("day_planning", false)
                .set("nw_stay_home", stayHome)
                .set("nw_state", STATE_INITIAL)
        ;
        LOGGER.fine("Non-Working. Day planning complete: " + person.hashCode() + " at " + time + ". Stay at home: " + stayHome);
        ctx.update(person);
    }

    /**
     * <p>
     *     If it's decided to go out, this rule sets the target destination and schedule
     *     and when it ends today.
     * </p>
     */
    @Rule(value = "Non-working. Leisure location and timing", salience = 8)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.properties.work == null",
            "$p.flags.nw_stay_home == false",
            "$p.integers.nw_state == STATE_INITIAL",
            "$p.properties.nw_destination == null"
    })
    public void scheduling(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {

        Entity destination =  world.randomBusiness();

        int startTime = time.seconds() + HOUR;
        person
                .set("nw_destination", destination)
                .set("travel_to", destination)
                .set("travel_at", startTime)
        ;

        LOGGER.fine("Scheduled: " + person.hashCode() + " at " + time + " to " + destination);
        ctx.update(person);
    }

    @Rule(value = "Non-working. Arrival", salience = 5)
    @Where({
            "$p.properties.nw_destination != null",
            "$p.properties.location == $p.properties.nw_destination",
            "$p.integers.nw_state == STATE_INITIAL"
    })
    public void arrival(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        int endTime = time.seconds() + HOUR * 3;
        person
                .set("nw_state", STATE_HAVING_FUN)
                .set("nw_end_time", endTime)
        ;

        LOGGER.fine("Having fun: " + person.hashCode() + " at " + time);
        ctx.update(person);
    }

    @Rule(value = "Non-working. Heading home", salience = 1)
    @Where({
            "$p.properties.nw_destination != null",
            "$p.properties.location == $p.properties.nw_destination",
            "$p.integers.nw_state == STATE_HAVING_FUN",
            "$t.seconds > $p.integers.nw_end_time",
    })

    public void returningHome(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        person
                .set("nw_state", STATE_END_OF_DAY)
                .set("travel_to", person.getProperty("home"))
                .set("travel_at", time.seconds())
                .set("nw_destination", null)
        ;
        LOGGER.fine( "Getting home: " + person.hashCode() + " at " + time);
        ctx.update(person);
    }

}
