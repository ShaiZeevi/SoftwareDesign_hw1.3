package il.ac.technion.cs.sd.pay.app;

import SYLib.CollectionDoesNotExistException;
import SYLib.ISYLibable;
import javafx.util.Pair;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Yoav Zuriel on 5/3/2018.
 */
public class myPayBookReader implements PayBookReader {

    private ISYLibable myLib;
    private Map<String, Double> clientSpending;
    private Map<String, Double> sellerProfits;
    private Map<Pair<String, String>, Double> sales;

    @Inject
    public myPayBookReader(ISYLibable myLib) {
        this.myLib = myLib;
        String clientCollectionName = "clientSpending";
        String sellerCollectionName = "sellerProfits";
        String clientSellerCollectionName = "sales";

        clientSpending = restoreStringMap(clientCollectionName);
        sellerProfits = restoreStringMap(sellerCollectionName);
        sales = restorePairMap(clientSellerCollectionName);
    }

    private Map<Pair<String, String>, Double> restorePairMap(String collectionName) {
        final Map<Pair<String, String>, Double> stringPairMap = new HashMap<>();
        try {
            Collection<String> rawCollection = myLib.restoreCollection(collectionName);

            for (String str : rawCollection) {
                StringTokenizer strtok = new StringTokenizer(str, "=");
                String key1 = strtok.nextToken();
                String key2 = strtok.nextToken();
                Double value = Double.parseDouble(strtok.nextToken());
                stringPairMap.put(new Pair<>(key1, key2), value);
            }

        } catch (CollectionDoesNotExistException | NullPointerException e) {
            e.printStackTrace();
            throw new AssertionError();
        }

        return stringPairMap;
    }

    private Map<String, Double> restoreStringMap(String collectionName) {
        final Map<String, Double> stringDoubleMap = new HashMap<>();
        try {
            Collection<String> rawCollection = myLib.restoreCollection(collectionName);

            for (String str : rawCollection) {
                StringTokenizer stringTokenizer = new StringTokenizer(str, "=");
                String key = stringTokenizer.nextToken();
                Double value = Double.parseDouble(stringTokenizer.nextToken());
                stringDoubleMap.put(key, value);
            }

        } catch (CollectionDoesNotExistException | NullPointerException e) {
            e.printStackTrace();
            throw new AssertionError();
        }

        return stringDoubleMap;
    }

    @Override
    public boolean paidTo(String clientId, String sellerId) {
        return sales.containsKey(new Pair<>(clientId, sellerId));
    }

    @Override
    public OptionalDouble getPayment(String clientId, String sellerId) {
        Double dbl = sales.get(new Pair<>(clientId, sellerId));
        if (dbl == null) return OptionalDouble.empty();
        else return OptionalDouble.of(dbl);
    }

    //Descending in value, Ascending in ID
    public int sellerOrClientComparator(Pair<String, Double> e1, Pair<String, Double> e2) {
        if(!e1.getValue().equals(e2.getValue())) return e2.getValue().compareTo(e1.getValue());
        return e1.getKey().compareTo(e2.getKey());
    }

    @Override
    public List<String> getBiggestSpenders() {
        return clientSpending.entrySet().stream().map(x -> new Pair<>(x.getKey(), x.getValue())).sorted(this::sellerOrClientComparator)
                .map(Pair::getKey).collect(Collectors.toList());
    }

    @Override
    public List<String> getRichestSellers() {
        return sellerProfits.entrySet().stream().map(x -> new Pair<>(x.getKey(), x.getValue()))
                .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());
    }

    @Override
    public Optional<String> getFavoriteSeller(String clientId) {
        List<String> temp = sales.entrySet().stream()
                .filter(x -> x.getKey().getKey().equals(clientId))
                .map(x -> new Pair<>(x.getKey().getValue(), x.getValue()))
                .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());

        if(temp.size() == 0) return Optional.empty();
        else return Optional.of(temp.get(0));
    }

    @Override
    public Optional<String> getBiggestClient(String sellerId) {
        List<String> temp = sales.entrySet().stream()
                .filter(x -> x.getKey().getValue().equals(sellerId))
                .map(x -> new Pair<>(x.getKey().getKey(), x.getValue()))
                .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());

        if(temp.size() == 0) return Optional.empty();
        else return Optional.of(temp.get(0));
    }


    @Override
    public Map<String, Integer> getBiggestPaymentsToSellers() {
        Map<String, Integer> result = new HashMap<>();

        for(String sellerId : sellerProfits.keySet()){
            if(!getBiggestClient(sellerId).isPresent()) {
                continue;
//                throw new AssertionError("no payment made to seller " + sellerId);
            }

            String clientId = getBiggestClient(sellerId).get();
            double amount = sales.get(new Pair<>(clientId, sellerId));

            result.put(sellerId, (int)amount);
        }
        //TODO: sort and get top 10
        return result;
    }

    @Override
    public Map<String, Integer> getBiggestPaymentsFromClients() {
        HashMap<String, Integer> result = new HashMap<>();

        for(String clientId : clientSpending.keySet()){
            if(!getFavoriteSeller(clientId).isPresent()) {
                continue;
//                throw new AssertionError("client " + clientId + " has made not payment");
            }

            String sellerId = getFavoriteSeller(clientId).get();
            double amount = sales.get(new Pair<>(clientId, sellerId));

            result.put(clientId, (int)amount);
        }
        //TODO: sort and get top 10
        return result;
    }

}
