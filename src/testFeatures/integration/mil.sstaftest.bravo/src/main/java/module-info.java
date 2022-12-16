module mil.sstaftest.bravo {
    exports mil.sstaftest.bravo;
    requires mil.sstaf.core;
    
    provides mil.sstaf.core.features.Feature with mil.sstaftest.bravo.BravoProvider;
    opens mil.sstaftest.bravo to mil.sstaf.core;
}
