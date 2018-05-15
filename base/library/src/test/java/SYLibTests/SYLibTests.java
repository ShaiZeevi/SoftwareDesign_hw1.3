package SYLibTests;

import SYLib.*;
import il.ac.technion.cs.sd.pay.ext.SecureDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import javax.xml.ws.soap.Addressing;
import java.util.*;
import java.util.zip.DataFormatException;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

/**
 * Created by Yoav Zuriel on 5/13/2018.
 */
public class SYLibTests {
    private HashSet<String> names = new HashSet<>();

    public void initNames() {
        names = new HashSet<>();
    }

    private byte[] checkIfCollectionAppearsAndAddIt(byte[] name) {
        if (names.contains(new String(name))) return new byte[]{1};
        names.add(new String(name));
        throw new NoSuchElementException();
    }

    @Test
    public void cantCreateSameStorageTwiceTest() {
        initNames();

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
    public void cantSaveTheSameCollectionTwiceTest() throws InterruptedException {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        Mockito.when(mockDB.get("Collection1".getBytes()))
                .thenAnswer(invocationOnMock -> checkIfCollectionAppearsAndAddIt(invocationOnMock.getArgument(0)));

        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        try {
            myLib.saveCollection("Collection1", new ArrayList<>());
        } catch (CollectionAlreadyExistsException e) {
            fail();
        }

        assertThrows(CollectionAlreadyExistsException.class, () -> myLib.saveCollection("Collection1", new ArrayList<>()));
    }


    @Test
    public void cantRestoreUnsavedCollectionTest() throws InterruptedException {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        Mockito.when(mockDB.get("Collection1".getBytes())).thenThrow(new NoSuchElementException());
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);

        assertThrows(CollectionDoesNotExistException.class, () -> myLib.restoreCollection("Collection1"));
    }

    @Test
    public void restoreCollectionTest() throws InterruptedException {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        Mockito.when(mockDB.get("arrayList".getBytes()))
                .thenAnswer(invocationOnMock -> checkIfCollectionAppearsAndAddIt(invocationOnMock.getArgument(0)));
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        ArrayList<String> arrayList = new ArrayList<>();
        for (Integer i = 10; i < 100; i++) {
            arrayList.add(i.toString());
            Mockito.when(mockDB.get((Integer.valueOf(i - 10)).toString().getBytes())).thenReturn(i.toString().getBytes());
        }
        Mockito.when(mockDB.get("90".getBytes())).thenThrow(new NoSuchElementException());

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

    @Test
    public void addEntryToStorageWhichDoesNotExistTest() {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);

        assertThrows(StorageDoesNotExistsException.class, () -> myLib.addEntry("Storage1", 1, "value"));
    }

    @Test
    public void addEntryWithMoreThan100BytesTest() throws StorageAlreadyExistsException {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        myLib.openStorage("Storage1");

        String badParam = new String(new byte[101]);
        assertThrows(DataFormatException.class, () -> myLib.addEntry("Storage1", 1, badParam));
    }

    @Test
    public void addEntryTwiceUpdatesTest() throws StorageAlreadyExistsException, InterruptedException {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        myLib.openStorage("Storage1");
        Mockito.when(mockDB.get("1".getBytes())).thenReturn("3".getBytes());

        try {
            myLib.addEntry("Storage1", 1, "2");
            myLib.addEntry("Storage1", 1, "3");
            assertEquals(Optional.of("3"), myLib.get("Storage1", 1));
        } catch (StorageDoesNotExistsException | DataFormatException e) {
            fail();
        }
    }

    @Test
    public void getFromStorageWhichDoesNotExistTest() {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);

        assertThrows(StorageDoesNotExistsException.class, () -> myLib.get("Storage1", 1));
    }

    @Test
    public void getEmptyValueTest() throws InterruptedException, StorageAlreadyExistsException, StorageDoesNotExistsException {
        initNames();

        SecureDatabase mockDB = Mockito.mock(SecureDatabase.class);
        ISYLibable myLib = new SYLibrary((unused) -> mockDB);
        Mockito.when(mockDB.get(any())).thenThrow(new NoSuchElementException());
        myLib.openStorage("Storage1");

        assertEquals(Optional.empty(), myLib.get("Storage1", 1));
    }
}
