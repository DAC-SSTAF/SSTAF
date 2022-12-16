open module mil.sstaftest.maneuver.entityagent.integration {
    exports mil.sstaftest.maneuver.entityagent.integration;
    requires mil.sstaf.core;
    requires org.slf4j;
    requires mil.sstaf.blackboard.api;
    requires org.junit.jupiter.api;
    requires mil.sstaftest.maneuver.entityagent;
    requires mil.sstaftest.maneuver.api;

    uses mil.sstaf.core.features.Feature;
    uses mil.sstaf.blackboard.api.Blackboard;
    uses mil.sstaf.core.features.Agent;
    uses mil.sstaftest.maneuver.entityagent.ManeuverEntityAgent;
}
