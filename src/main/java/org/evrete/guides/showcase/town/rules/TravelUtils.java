package org.evrete.guides.showcase.town.rules;

import org.evrete.api.RhsContext;
import org.evrete.dsl.annotation.*;
import org.evrete.guides.showcase.town.dto.Entity;
import org.evrete.guides.showcase.town.dto.LocationType;
import org.evrete.guides.showcase.town.dto.WorldTime;

/**
 * <p>
 * A Travel module that handles peoples' movements. The ruleset relies on the
 * <code>travel_to</code> and <code>travel_at</code> properties which signify
 * travel destination and travel start time respectfully.
 * </p>
 * <p>
 * In this module, we will mark module-specific properties and flags with
 * the "t_" prefix. None of such properties will be used outside this module.
 * </p>
 */
@RuleSet
public class TravelUtils extends Commons {
    private double speed = 0.3;

    @EnvironmentListener("commute-speed")
    public void setSpeed(double speed) {
        this.speed = speed;
        LOGGER.fine("Commute speed set to: " + this.speed);
    }

    /**
     * <p>
     * Once we see a non-null <code>travel_to</code> property, and the world time exceeds
     * the <code>travel_at</code> value, we mark the person as being in transit. In this module, we
     * will mark module-specific properties and flags with the "t_" prefix. None of such properties
     * should be used outside the travel module.
     * </p>
     *
     * @param ctx    context
     * @param person person
     * @param time   time
     */
    @Rule(value = "Travel. Start", salience = -100)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.properties.travel_to != null",
            "$p.properties.location != $p.properties.travel_to",
            "$t.seconds > $p.integers.travel_at",
            "$p.flags.t_in_transit == false",
    })
    public void initTravel(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        Entity destination = person.getProperty("travel_to");
        Entity current = person.getProperty("location");
        // Compute total travel distance
        int travelDistance = distance(destination, current);
        // Travelled so far is zero
        int travelled = 0;
        // Marking the current location as being a part of the commute route
        Entity startLocation = new Entity(LocationType.COMMUTING);
        startLocation.set("x", current.getNumber("x"));
        startLocation.set("y", current.getNumber("y"));

        // Direction to the destination, a unit vector
        Entity unitVector = unitVector(current, destination);


        person
                .set("location", startLocation)
                //.set("arrived", false)
                .set("t_travel_distance", travelDistance)
                .set("t_travelled", travelled)
                .set("t_next_check", time.seconds() + 1)
                .set("t_travel_vector", unitVector)
                .set("t_from", startLocation)
                .set("t_to", destination)
                .set("t_in_transit", true)
        ;
        LOGGER.fine("Start travelling: " + person.hashCode() + " at " + time + ", from: " + current + " to " + destination + " distance: " + travelDistance);
        ctx.update(person);
    }

    @Rule(value = "Travel. Update location",salience=-110)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.flags.t_in_transit == true",
            "$p.integers.t_travelled < $p.integers.t_travel_distance",
            "$t.seconds > $p.integers.t_next_check"
    })
    public void cycle(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        // Updating distance travelled
        int deltaTime = time.seconds() - person.getNumber("travel_at");
        Entity startPosition = person.getProperty("t_from");
        Entity unitVector = person.getProperty("t_travel_vector");
        Entity pathTravelled = multiply(unitVector, speed * deltaTime);
        Entity newLocation = sum(startPosition, pathTravelled);

        int newTravelled = distance(newLocation, startPosition);
        person
                .set("location", newLocation)
                .set("t_next_check", time.seconds() + 1)
                .set("t_travelled", newTravelled)
        ;
        LOGGER.finest("New location: " + person.hashCode() + " time: " + time + ", location: " + newLocation);
        ctx.update(person);
    }

    @Rule(value = "Travel. Arrival", salience=-120)
    @Where({
            "$p.flags.configured == true",
            "$p.flags.awake == true",
            "$p.flags.t_in_transit == true",
            "$p.integers.t_travelled >= $p.integers.t_travel_distance"
    })
    public void finishTravel(RhsContext ctx, @Fact("$p") Entity person, @Fact("$t") WorldTime time) {
        person
                .set("location", person.getProperty("t_to"))
                .set("travel_to", null)
                .set("t_travel_vector", null)
                .set("t_in_transit", false)
                .set("t_from", null)
                .set("t_to", null)
        ;
        LOGGER.fine("Finish travelling: " + person.hashCode() + " time: " + time);
        ctx.update(person);
    }

    private static int distance(Entity from, Entity to) {
        int dx = to.getNumber("x") - from.getNumber("x");
        int dy = to.getNumber("y") - from.getNumber("y");
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    private static Entity minus(Entity from, Entity to) {
        int dx = to.getNumber("x") - from.getNumber("x");
        int dy = to.getNumber("y") - from.getNumber("y");
        Entity v = new Entity("vector");
        v.set("x", dx);
        v.set("y", dy);
        return v;
    }

    private static Entity sum(Entity v1, Entity v2) {
        int x = v2.getNumber("x") + v1.getNumber("x");
        int y = v2.getNumber("y") + v1.getNumber("y");
        Entity v = new Entity(LocationType.COMMUTING);
        v.set("x", x);
        v.set("y", y);
        return v;
    }

    private static Entity multiply(Entity unitVector, double value) {
        Entity v = new Entity("vector");
        int newX = (int) (unitVector.getDouble("x") * value);
        int newY = (int) (unitVector.getDouble("y") * value);
        v.set("x", newX);
        v.set("y", newY);
        return v;
    }

    private static Entity unitVector(Entity delta) {
        int dx = delta.getNumber("x");
        int dy = delta.getNumber("y");
        double length = Math.sqrt(dx * dx + dy * dy);
        Entity v = new Entity("unit_vector");
        v.set("x", 1.0 * dx / length);
        v.set("y", 1.0 * dy / length);
        return v;
    }

    private static Entity unitVector(Entity from, Entity to) {
        return unitVector(minus(from, to));
    }
}
