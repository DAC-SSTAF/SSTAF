module mil.sstaftest.delta {
    exports mil.sstaftest.delta;
    requires mil.sstaf.core;
    
    provides mil.sstaf.core.features.Feature with mil.sstaftest.delta.DeltaProvider;
    opens mil.sstaftest.delta to mil.sstaf.core;
}
