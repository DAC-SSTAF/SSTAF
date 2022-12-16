module mil.sstaftest.alpha {
    exports mil.sstaftest.alpha;
    requires mil.sstaf.core;
    
    provides mil.sstaf.core.features.Feature with mil.sstaftest.alpha.AlphaProvider;
    opens mil.sstaftest.alpha to mil.sstaf.core;
}
