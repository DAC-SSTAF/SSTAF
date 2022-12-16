module mil.devcom_sc.ansur.messages {
    exports mil.devcom_sc.ansur.messages;

    requires transitive mil.sstaf.core;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    opens mil.devcom_sc.ansur.messages to com.fasterxml.jackson.databind;
}
