package SYLib;

import SYlib.CollectionAlreadyExistsException;
import SYlib.CollectionDoesNotExistException;
import SYlib.ISYLibable;
import SYlib.StorageAlreadyExistsException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseModule;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by Yoav Zuriel on 5/13/2018.
 */
public class SYLibTests {
    @Test
    public void cantCreateSameStorageTwiceTest() {
        Injector injector = Guice.createInjector(new SecureDatabaseModule(), new SYLibTestModule());
        ISYLibable myLib = injector.getInstance(ISYLibable.class);
        try {
            myLib.openStorage("Storage1");
        } catch (StorageAlreadyExistsException e) {
            fail();
        }
        assertThrows(StorageAlreadyExistsException.class, () -> myLib.openStorage("Storage1"));
    }

    @Test
    public void cantSaveTheSameCollectionTwiceTest() {
        Injector injector = Guice.createInjector(new SecureDatabaseModule(), new SYLibTestModule());
        ISYLibable myLib = injector.getInstance(ISYLibable.class);
        try {
            myLib.saveCollection("collection1", new ArrayList<>());
        } catch (CollectionAlreadyExistsException e) {
            fail();
        }
        assertThrows(CollectionAlreadyExistsException.class, () -> myLib.saveCollection("collection1", new ArrayList<>()));
    }

    @Test
    public void cantRestoreUnsavedCollectionTest(){
        Injector injector = Guice.createInjector(new SecureDatabaseModule(), new SYLibTestModule());
        ISYLibable myLib = injector.getInstance(ISYLibable.class);
        assertThrows(CollectionDoesNotExistException.class, () -> myLib.restoreCollection("Collection1"));
    }

    @Test
    public void restoreCollectionTest() {
        Injector injector = Guice.createInjector(new SecureDatabaseModule(), new SYLibTestModule());
        ISYLibable myLib = injector.getInstance(ISYLibable.class);
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer i = 10; i < 100; i++) {
            arrayList.add(i.toString());
        }
        try {
            myLib.saveCollection("arrayList", arrayList);
            Collection<String> collection = myLib.restoreCollection("arrayList");
            Iterator targetIt = collection.iterator();
            for (Object obj : arrayList)
                assertEquals(obj, targetIt.next());
        } catch (CollectionAlreadyExistsException | CollectionDoesNotExistException e) {
            fail();
        }
    }
}
