
import mil.sstaf.core.features.Feature;
import mil.sstaf.core.features.Handler;
import mil.sstaf.core.features.Agent;
import mil.sstaf.pyagent.impl.PyAgentImpl;
import mil.sstaf.pyagent.api.PyAgent;

@SuppressWarnings("empty")
module mil.sstaf.pyagent.impl {
    exports mil.sstaf.pyagent.impl;

    requires mil.sstaf.core;
    requires mil.sstaf.pyagent.messages;
    requires mil.sstaf.pyagent.api;
    requires org.slf4j;

    /*
     * Unfortunately, ServiceLoader doesn't recognize class hierarchies
     * when looking for a match. Therefore, it is necessary to list
     * all implemented interfaces to ensure a match can be
     * found.
     */
    provides Feature with PyAgentImpl;
    provides Handler with PyAgentImpl;
    provides Agent with PyAgentImpl;
    provides PyAgent with PyAgentImpl;

    /*
     * This statement is necessary to allow SSTAF to inspect the Feature
     * and resolve any @Requires specifications.
     */
    opens mil.sstaf.pyagent.impl to mil.sstaf.core;
}