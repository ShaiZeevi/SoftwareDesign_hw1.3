package il.ac.technion.cs.sd.pay.test;

import SYLib.ISYLibable;
import com.google.inject.AbstractModule;
import il.ac.technion.cs.sd.pay.app.PayBookInitializer;
import il.ac.technion.cs.sd.pay.app.PayBookReader;
import il.ac.technion.cs.sd.pay.app.myPayBookInitializer;
import il.ac.technion.cs.sd.pay.app.myPayBookReader;

// This module is in the testing project, so that it could easily bind all dependencies from all levels.
class PayBookModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PayBookInitializer.class).to(myPayBookInitializer.class);
        bind(PayBookReader.class).to(myPayBookReader.class);
        //TODO: remove that!!!
//        bind(ISYLibable.class).to(SYLibrary.class);
        bind(ISYLibable.class).toInstance(new FakeLib());
    }
}
