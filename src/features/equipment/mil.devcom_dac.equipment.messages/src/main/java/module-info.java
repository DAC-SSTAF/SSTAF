module mil.devcom_dac.equipment.messages {
    exports mil.devcom_dac.equipment.messages;

    requires transitive mil.sstaf.core;
    requires lombok;
    requires com.fasterxml.jackson.databind;
    opens mil.devcom_dac.equipment.messages to com.fasterxml.jackson.databind;
}
