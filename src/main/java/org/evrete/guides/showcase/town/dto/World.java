package org.evrete.guides.showcase.town.dto;


import org.evrete.guides.showcase.town.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class World {
    public List<Entity> homes = new ArrayList<>();
    public List<Entity> businesses = new ArrayList<>();
    public List<Entity> population = new ArrayList<>();

    private World(GeoData data) {
        List<XYPoint> homesInUse = data.homes;
        List<XYPoint> businessesInUse = data.businesses;

        // Add business locations
        for (XYPoint point : businessesInUse) {
            Entity business = new Entity(LocationType.BUSINESS);
            business.set("x", point.x);
            business.set("y", point.y);
            this.businesses.add(business);
        }

        for (XYPoint point : homesInUse) {
            Entity home = new Entity(LocationType.RESIDENTIAL);
            home.set("x", point.x);
            home.set("y", point.y);
            this.homes.add(home);
        }
    }

    public static World factory(GeoData data, int population) {
        if (population > RunConfig.MAX_POPULATION) {
            throw new IllegalArgumentException("Population size exceeds maximum value");
        }

        World world = new World(data);
        for (int i = 0; i < population; i++) {
            Entity resident = new Entity("person");
            resident.set("home", RandomUtils.randomListElement(world.homes));
            world.population.add(resident);
        }

        return world;
    }


    public static int randomGaussian(int mean, int stdDev, int maximum) {
        return RandomUtils.positiveRandom(mean, stdDev, maximum);
    }

    public Entity randomBusiness() {
        return RandomUtils.randomListElement(this.businesses);
    }
}
