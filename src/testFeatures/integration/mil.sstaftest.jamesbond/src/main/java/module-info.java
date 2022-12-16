module mil.sstaftest.jamesbond {
    exports mil.sstaftest.jamesbond;
    requires mil.sstaf.core;
    
    provides mil.sstaf.core.features.Feature with mil.sstaftest.jamesbond.JamesBond;
    opens mil.sstaftest.jamesbond to mil.sstaf.core;
}
