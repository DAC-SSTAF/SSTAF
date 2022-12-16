module mil.sstaftest.util {
    exports mil.sstaftest.util;

    requires mil.sstaf.core;
    
    requires org.junit.jupiter.api;

    opens mil.sstaftest.util to org.junit.platform.commons;

}
