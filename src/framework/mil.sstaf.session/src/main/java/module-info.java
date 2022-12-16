module mil.sstaf.session {
    exports mil.sstaf.session.messages;
    exports mil.sstaf.session.control;

    requires transitive mil.sstaf.core;
    requires org.slf4j;
    
    requires mil.sstaf.blackboard.api;
    opens mil.sstaf.session.control to com.fasterxml.jackson.databind;
    opens mil.sstaf.session.messages to com.fasterxml.jackson.databind;

}
