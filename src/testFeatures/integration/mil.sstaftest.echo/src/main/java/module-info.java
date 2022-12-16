module mil.sstaftest.echo {
    exports mil.sstaftest.echo;
    requires mil.sstaf.core;
    
    provides Runnable with mil.sstaftest.echo.EchoProvider;
    opens mil.sstaftest.echo to mil.sstaf.core;
}
