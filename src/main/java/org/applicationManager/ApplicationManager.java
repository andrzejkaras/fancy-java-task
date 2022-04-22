package org.applicationManager;

import org.actionLog.*;
import org.container.*;
import org.verifier.*;

/**
 * Facade class for main API.
 */
public class ApplicationManager {
    private final ContainerHub hub;
    private final ActionLog log;
    private final StateVerifier verifier;

    public ApplicationManager(ContainerHub hub, ActionLog log, StateVerifier verifier) {
        this.hub = hub;
        this.log = log;
        this.verifier = verifier;
    }

    public void createContainer(String name) {
        Container container = new Container(name);
        hub.add(container);
    }

    public void createContainer(String name, double capacity) {
        Container container = new Container(name, capacity);
        hub.add(container);
    }

    public void addWater(String name, double amountOfWater) {
        Container container = hub.getByName(name);
        container.addWater(amountOfWater);
        log.append(new LogEntry("ADD", name, amountOfWater));
    }

    public void removeWater(String name, double amountOfWater) {
        Container container = hub.getByName(name);
        container.removeWater(amountOfWater);
        log.append(new LogEntry("SUB", name, amountOfWater));
    }

    public void swap(String from, String to, double amountOfWater) {
        Container fromContainer = hub.getByName(from);
        Container toContainer = hub.getByName(to);

        fromContainer.swap(toContainer, amountOfWater);
        log.append(new LogEntry("SUB", from, amountOfWater));
        log.append(new LogEntry("SUB", to, amountOfWater));
    }

    public ContainerStatsProvider getStatsProvider() {
        return (ContainerStatsProvider) hub;
    }

    public EventStatsProvider getEventStatsProvider() {
        return (EventStatsProvider) log;
    }

    public boolean verifyState(String name) {
        return this.verifier.check(name);
    }
}
