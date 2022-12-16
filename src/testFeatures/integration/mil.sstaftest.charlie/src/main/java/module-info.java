module mil.sstaftest.charlie {
    exports mil.sstaftest.charlie;
    requires mil.sstaf.core;
    
    provides mil.sstaf.core.features.Feature with mil.sstaftest.charlie.CharlieProvider;
}
