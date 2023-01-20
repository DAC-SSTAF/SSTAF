/**
 * A module-info.java configuration file is required for all modules.
 */
module mil.sstaf.pyagent.messages {
    requires mil.sstaf.core;
    
    requires org.slf4j;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    exports mil.sstaf.pyagent.messages;

    //
    // Required to enable automatic JSON serialization
    //
    opens mil.sstaf.pyagent.messages to com.fasterxml.jackson.databind;

}