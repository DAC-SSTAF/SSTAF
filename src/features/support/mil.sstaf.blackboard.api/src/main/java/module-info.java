module mil.sstaf.blackboard.api {
    exports mil.sstaf.blackboard.api;

    requires transitive mil.sstaf.core;
    requires org.slf4j;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    opens mil.sstaf.blackboard.api to com.fasterxml.jackson.databind;
}
