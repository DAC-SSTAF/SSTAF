module mil.sstaf.analyzer {
    exports mil.sstaf.analyzer.messages;

    requires mil.sstaf.core;
    requires mil.sstaf.session;
    requires org.slf4j;

    opens mil.sstaf.analyzer.messages to com.fasterxml.jackson.databind;
}
