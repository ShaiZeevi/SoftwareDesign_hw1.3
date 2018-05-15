package il.ac.technion.cs.sd.pay.test;

import SYLib.*;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.zip.DataFormatException;

/**
 * Created by Yoav Zuriel on 5/15/2018.
 */
public class FakeLib implements ISYLibable {
    private HashMap<String, HashMap<String, String>> storages;
    private HashMap<String, Collection<String>> collections;

    @Inject
    FakeLib() {
        storages = new HashMap<>();
        collections = new HashMap<>();
    }

    @Override
    public void openStorage(String storageName) throws StorageAlreadyExistsException {
        if (storages.containsKey(storageName)) throw new StorageAlreadyExistsException();
        storages.put(storageName, new HashMap<>());
    }

    @Override
    public void saveCollection(String collectionName, Collection<String> collection) throws CollectionAlreadyExistsException {
        if (collections.containsKey(collectionName)) throw new CollectionAlreadyExistsException();
        collections.put(collectionName, collection);
    }

    @Override
    public Collection<String> restoreCollection(String collectionName) throws CollectionDoesNotExistException {
        if (!collections.containsKey(collectionName)) throw new CollectionDoesNotExistException();
        return collections.get(collectionName);
    }

    @Override
    public <T> void addEntry(String storageName, T key, String value) throws StorageDoesNotExistsException, DataFormatException {
        if (!storages.containsKey(storageName)) throw new StorageDoesNotExistsException();
        if (value.getBytes().length >= 100) throw new DataFormatException();
        Map<String, String> current = storages.get(storageName);
        current.put(key.toString(), value);
        assert storages.get(storageName).get(key.toString()).equals(value);
    }

    @Override
    public <T> Optional<String> get(String storageName, T key) throws StorageDoesNotExistsException {
        if (!storages.containsKey(storageName)) throw new StorageDoesNotExistsException();
        if (!storages.get(storageName).containsKey(key.toString())) return Optional.empty();
        return Optional.of(storages.get(storageName).get(key.toString()));
    }
}
