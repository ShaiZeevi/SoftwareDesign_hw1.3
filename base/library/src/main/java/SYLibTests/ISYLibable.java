package SYLibTests;

import java.util.Collection;
import java.util.Optional;
import java.util.zip.DataFormatException;

/**
 * Created by Yoav Zuriel on 4/16/2018.
 */
public interface ISYLibable {

    void openStorage(String storageName) throws StorageAlreadyExistsException;

    public void saveCollection(String collectionName, Collection<String> collection) throws CollectionAlreadyExistsException;

    public Collection<String> restoreCollection(String collectionName) throws CollectionDoesNotExistException;

    <T> void addEntry(String storageName, T key, String value) throws StorageDoesNotExistsException, DataFormatException;

    <T> Optional<String> get(String storageName, T key) throws StorageDoesNotExistsException;
}
