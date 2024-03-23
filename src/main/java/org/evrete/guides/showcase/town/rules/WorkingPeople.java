package org.evrete.guides.showcase.town.rules;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.Fact;
import org.evrete.dsl.annotation.Rule;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.dsl.annotation.Where;
import org.evrete.guides.showcase.town.dto.Entity;
import org.evrete.guides.showcase.town.dto.WorldTime;

@RuleSet
public class WorkingPeople extends Commons {
    public static int STATE_WORKING = 1;
    public static int STATE_WORK_END = 2;

    /**
     * <p>
     *     Working people's day planning. The rule sets when a person heads off to work
     *     and when it ends today.
     * </p>
     * @param ctx    context
     * @param person person
     * @param time   time
     */
    @Rule(value = "Working. Day planning", salience = 1000)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.flags.day_planning == true",
            "$p.properties.work != null",
    })
    public void dayPlanning(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        // In an hour the person goes off to work
        Entity travelTo = person.getProperty("work");
        int travelAt = time.seconds() + HOUR;
        // When work ends
        int workEndTime = travelAt + 8 * HOUR; // Travel adjusted


        person
                .set("day_planning", false)
                .set("travel_to", travelTo)
                .set("travel_at", travelAt)
                .set("w_work_end", workEndTime)
                .set("w_state", STATE_WORKING)
        ;
        LOGGER.fine("Working. Day planning complete: " + person.hashCode() + " at " + time);
        ctx.update(person);
    }

    /**
     * <p>
     *     Working people's day planning. The rule sets when a person heads off to work
     *     and when it ends today.
     * </p>
     * @param ctx    context
     * @param person person
     * @param time   time
     */
    @Rule(value = "Working. End of work", salience = 800)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.properties.work != null",
            "$p.properties.location == $p.properties.work",
            "$p.integers.w_state == STATE_WORKING",
            "$t.seconds > $p.integers.w_work_end",
    })
    public void workEnd(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        person
                .set("travel_to", person.getProperty("home"))
                .set("travel_at", time.seconds())
                .set("w_state", STATE_WORK_END)
        ;

        LOGGER.fine("Working. End of work: " + person.hashCode() + " at " + time);
        ctx.update(person);
    }

}
