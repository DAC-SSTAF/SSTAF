module mil.devcom_dac.equipment.api {
    exports mil.devcom_dac.equipment.api;

    requires mil.sstaf.core;
    
    requires org.slf4j;
    opens mil.devcom_dac.equipment.api to com.fasterxml.jackson.databind;
}
