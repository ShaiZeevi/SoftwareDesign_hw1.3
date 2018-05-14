package SYLib;

import il.ac.technion.cs.sd.pay.ext.SecureDatabase;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.zip.DataFormatException;

/**
 * Created by Yoav Zuriel on 4/16/2018.
 */
public class SYLibrary implements ISYLibable {
    private SecureDatabaseFactory myFactory;
    private SecureDatabase collectionsDB;
    private HashMap<String, SecureDatabase> userDBs;

    @Inject
    public SYLibrary(SecureDatabaseFactory myFactory) {
        this.myFactory = myFactory;
        String myDB = "MapDB";
        collectionsDB = this.myFactory.open(myDB);
        userDBs = new HashMap<>();
    }

    @Override
    public void openStorage(String storageName) throws StorageAlreadyExistsException {
        if (userDBs.containsKey(storageName)) throw new StorageAlreadyExistsException();
        userDBs.put(storageName, myFactory.open(storageName));
    }

    @Override
    public void saveCollection(String collectionName, Collection<String> collection) throws CollectionAlreadyExistsException {
        try {
            if (collectionsDB.get(collectionName.getBytes())[0] == 1) throw new CollectionAlreadyExistsException();
        } catch (InterruptedException | DataFormatException ignored) {
        } catch (NoSuchElementException e) {
            SecureDatabase current = myFactory.open(collectionName);
            byte[] temp = new byte[1];
            temp[0] = 1;
            collectionsDB.addEntry(collectionName.getBytes(), temp);
            Integer i = 0;
            for (String item : collection) {
                current.addEntry(i.toString().getBytes(), item.getBytes());
                i++;
            }
        }
    }

    @Override
    public Collection<String> restoreCollection(String collectionName) throws CollectionDoesNotExistException {
        try {
            collectionsDB.get(collectionName.getBytes());
            SecureDatabase current = myFactory.open(collectionName);
            LinkedList<String> restored = new LinkedList<>();
            for (Integer i = 0; true; i++) {
                try {
                    restored.addLast(new String(current.get(i.toString().getBytes())));
                } catch (NoSuchElementException ignored) {
                    return restored;
                }
            }
        } catch (InterruptedException | DataFormatException ignored) {
        } catch (NoSuchElementException e) {
            throw new CollectionDoesNotExistException();
        }
        return new LinkedList<>();
    }

    @Override
    public <T> void addEntry(String storageName, T key, String value)
            throws StorageDoesNotExistsException, DataFormatException {
        if (!userDBs.containsKey(storageName)) throw new StorageDoesNotExistsException();
        SecureDatabase current = userDBs.get(storageName);
        if (value.getBytes().length >= 100) throw new DataFormatException();
        current.addEntry(key.toString().getBytes(), value.getBytes());
    }

    @Override
    public <T> Optional<String> get(String storageName, T key) throws StorageDoesNotExistsException {
        if (!userDBs.containsKey(storageName)) throw new StorageDoesNotExistsException();
        SecureDatabase current = userDBs.get(storageName);
        try {
            return Optional.of(new String(current.get(key.toString().getBytes())));
        } catch (InterruptedException | DataFormatException ignored) {
        } catch (NoSuchElementException ignore) {
            return Optional.empty();
        }
        return Optional.empty();
    }

}
