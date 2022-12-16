module mil.sstaftest.simplemock {
    exports mil.sstaftest.simplemock;

    requires mil.sstaf.core;

    //
    // Required to make the scripts accessible
    //
    opens mil.sstaftest.simplemock to mil.sstaf.core;
}
