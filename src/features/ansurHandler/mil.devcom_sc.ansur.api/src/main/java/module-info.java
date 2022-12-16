module mil.devcom_sc.ansur.api {
    exports mil.devcom_sc.ansur.api;
    exports mil.devcom_sc.ansur.api.constraints;

    requires mil.sstaf.core;
    requires transitive  mil.devcom_sc.ansur.messages;
    opens mil.devcom_sc.ansur.api to com.fasterxml.jackson.databind;
    opens mil.devcom_sc.ansur.api.constraints to com.fasterxml.jackson.databind;
}
