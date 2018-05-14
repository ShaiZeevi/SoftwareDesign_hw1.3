package SYLibTests;

import il.ac.technion.cs.sd.pay.ext.SecureDatabase;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by Yoav Zuriel on 5/13/2018.
 */
public class SYLibTests {
    @Test
    public void cantCreateSameStorageTwiceTest() {
        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        try {
            myLib.openStorage("Storage1");
        } catch (StorageAlreadyExistsException e) {
            fail();
        }
        assertThrows(StorageAlreadyExistsException.class, () -> myLib.openStorage("Storage1"));
    }

    @Test
    public void cantSaveTheSameCollectionTwiceTest() {
        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        try {
            Mockito.when(mockDB.get("Collection1".getBytes())).thenReturn(new byte[]{0}, new byte[]{1});
        } catch (InterruptedException | DataFormatException e) {
            fail();
        }
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        try {
            myLib.saveCollection("Collection1", new ArrayList<>());
        } catch (CollectionAlreadyExistsException e) {
            fail();
        }
        assertThrows(CollectionAlreadyExistsException.class, () -> myLib.saveCollection("Collection1", new ArrayList<>()));
    }


    @Test
    public void cantRestoreUnsavedCollectionTest(){
        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        try {
            Mockito.when(mockDB.get("Collection1".getBytes())).thenThrow(new NoSuchElementException());
        } catch (InterruptedException | DataFormatException e) {
            fail();
        }
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        assertThrows(CollectionDoesNotExistException.class, () -> myLib.restoreCollection("Collection1"));
    }

    @Test
    public void restoreCollectionTest() {
        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
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
