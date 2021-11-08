package org.evrete.showcase.town.rules;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.*;
import org.evrete.showcase.town.RandomUtils;
import org.evrete.showcase.town.model.Entity;
import org.evrete.showcase.town.model.World;
import org.evrete.showcase.town.model.WorldTime;

@RuleSet
public class MainRuleset extends Commons {
    private World world;
    private double shareOfWorkingPeople;

    @EnvironmentListener("world")
    public void initWorld(World world) {
        this.world = world;
        LOGGER.fine("World initialized");
    }

    @EnvironmentListener("working-probability")
    public void initWorkingProbability(double shareOfWorkingPeople) {
        this.shareOfWorkingPeople = shareOfWorkingPeople;
        LOGGER.fine("Share of working people is set to " + shareOfWorkingPeople);
    }


    /**
     * <p>
     * A "configuration" rule, that configures each person's wakeup/bedtime and other preferences.
     * </p>
     *
     * @param ctx    context
     * @param person person
     * @param time   time
     */
    @Rule(value = "Main. Per-person configuration", salience = 10000)
    @Where({
            "$p.flags.configured == false",
            "$t.seconds == 0"
    })
    public void rule0(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        // Waking up at 7am on average with standard deviation of 2 hours, no later than 11pm
        int wakeUpTime = RandomUtils.positiveRandom(7 * HOUR, 3 * HOUR) % DAY;
        // Active period, 16 hrs on average, no longer than 20 hrs
        int bedTime = (wakeUpTime + RandomUtils.positiveRandom(16 * HOUR, 2 * HOUR, 20 * HOUR)) %DAY;
        // Is the person awake initially, at 00:00AM ?
        boolean awake = bedTime < wakeUpTime;
        // The "working" flag defines if the person has a job
        boolean working = RandomUtils.randomBoolean(shareOfWorkingPeople);
        Entity workPlace = working ? world.randomBusiness() : null;

        person
                .set("bedtime", bedTime)
                .set("wakeup", wakeUpTime)
                .set("awake", awake)
                .set("work", workPlace)
                .set("configured", true)
                .set("location", person.getProperty("home"))
        ;

        LOGGER.fine("Person configured: " + person.hashCode());
        ctx.update(person);
    }

    /**
     * <p>
     * If person is at home, not awake, and it's wakeup time, then wake him up and
     * set up for a new day. The <code>day_planning</code> flag lets other rulesets
     * to continua with day planning, which may be different for different person
     * categories.
     * </p>
     *
     * @param ctx    context
     * @param person person
     * @param time   time
     */
    @Rule(value = "Main. Waking up", salience = 8000)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == false",
            "$p.properties.location == $p.properties.home",
            "$t.seconds > $p.integers.wakeup"
    })
    public void rule1(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        int nextWakeUp = person.getNumber("wakeup") + DAY;
        person
                .set("awake", true)
                .set("day_planning", true)
                .set("wakeup", nextWakeUp)
        ;
        LOGGER.fine("Waking up: " + person.hashCode() + " at " + time);

        ctx.update(person);
    }
    /**
     * <p>
     * If person is at home, awake, and it's bedtime, then toggle the awake flag.
     * </p>
     *
     * @param ctx    context
     * @param person person
     * @param time   time
     */
    @Rule(value = "Main. Going to bed", salience = 6000)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.properties.location == $p.properties.home",
            "$t.seconds > $p.integers.bedtime"
    })
    public void rule2(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        int nextBedtime = person.getNumber("bedtime") + DAY;
        person
                .set("awake", false)
                .set("bedtime", nextBedtime)
        ;
        LOGGER.fine("Going to bed: " + person.hashCode() + " at " + time);
        ctx.update(person);
    }

}
