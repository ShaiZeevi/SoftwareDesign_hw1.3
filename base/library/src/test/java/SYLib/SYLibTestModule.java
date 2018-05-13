package SYLib;

import SYlib.ISYLibable;
import SYlib.SYLib;
import com.google.inject.AbstractModule;

/**
 * Created by Yoav Zuriel on 5/13/2018.
 */
public class SYLibTestModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(ISYLibable.class).to(SYLib.class);
//        this.bind(SecureDatabaseFactory.class).toInstance((unused) -> Mockito.mock(SecureDatabase.class));
    }
}
