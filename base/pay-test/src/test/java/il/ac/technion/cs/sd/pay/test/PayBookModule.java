package il.ac.technion.cs.sd.pay.test;

import SYLib.ISYLibable;
import SYLib.SYLibrary;
import com.google.inject.AbstractModule;
import il.ac.technion.cs.sd.pay.app.PayBookInitializer;
import il.ac.technion.cs.sd.pay.app.PayBookReader;
import il.ac.technion.cs.sd.pay.app.myPayBookInitializer;
import il.ac.technion.cs.sd.pay.app.myPayBookReader;
import il.ac.technion.cs.sd.pay.ext.SecureDatabase;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseFactory;
import org.mockito.Mockito;

import java.util.zip.DataFormatException;

import static org.mockito.ArgumentMatchers.any;

// This module is in the testing project, so that it could easily bind all dependencies from all levels.
class PayBookModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PayBookInitializer.class).to(myPayBookInitializer.class);
        bind(PayBookReader.class).to(myPayBookReader.class);
        bind(ISYLibable.class).to(SYLibrary.class);
        //TODO: remove
        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        try {
            Mockito.when(mockDB.get(any())).thenReturn(new byte[]{0});
        } catch (InterruptedException | DataFormatException e) {
            e.printStackTrace();
        }
        bind(SecureDatabaseFactory.class).toInstance((unused) -> mockDB);
    }
}
