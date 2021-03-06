package il.ac.technion.cs.sd.pay.app;

import SYLib.CollectionDoesNotExistException;
import SYLib.ISYLibable;
import SYLib.StorageAlreadyExistsException;
import SYLib.StorageDoesNotExistsException;
import javafx.util.Pair;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.empty;

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
//        String clientCollectionName = "clientSpending";
//        String sellerCollectionName = "sellerProfits";
//        String clientSellerCollectionName = "sales";
//
//        clientSpending = restoreStringMap(clientCollectionName);
//        sellerProfits = restoreStringMap(sellerCollectionName);
//        sales = restorePairMap(clientSellerCollectionName);
        try {
            myLib.openStorage("sales");
            myLib.openStorage("topClientPayments");
            myLib.openStorage("topSellerProfits");
            myLib.openStorage("faveSellerMapping");
            myLib.openStorage("bigClientMapping");
            myLib.openStorage("biggestPaymentsToSellers");
            myLib.openStorage("biggestPaymentsFromClients");

        } catch (StorageAlreadyExistsException ignore) {
        }

    }

//    private Map<Pair<String, String>, Double> restorePairMap(String collectionName) {
//        final Map<Pair<String, String>, Double> stringPairMap = new HashMap<>();
//        try {
//            Collection<String> rawCollection = myLib.restoreCollection(collectionName);
//
//            for (String str : rawCollection) {
//                StringTokenizer strtok = new StringTokenizer(str, "=");
//                String key1 = strtok.nextToken();
//                String key2 = strtok.nextToken();
//                Double value = Double.parseDouble(strtok.nextToken());
//                stringPairMap.put(new Pair<>(key1, key2), value);
//            }
//
//        } catch (CollectionDoesNotExistException | NullPointerException e) {
//            e.printStackTrace();
//            throw new AssertionError();
//        }
//
//        return stringPairMap;
//    }

//    private Map<String, Double> restoreStringMap(String collectionName) {
//        final Map<String, Double> stringDoubleMap = new HashMap<>();
//        try {
//            Collection<String> rawCollection = myLib.restoreCollection(collectionName);
//
//            for (String str : rawCollection) {
//                StringTokenizer stringTokenizer = new StringTokenizer(str, "=");
//                String key = stringTokenizer.nextToken();
//                Double value = Double.parseDouble(stringTokenizer.nextToken());
//                stringDoubleMap.put(key, value);
//            }
//
//        } catch (CollectionDoesNotExistException | NullPointerException e) {
//            e.printStackTrace();
//            throw new AssertionError();
//        }
//
//        return stringDoubleMap;
//    }

    @Override
    public boolean paidTo(String clientId, String sellerId) {
//        return sales.containsKey(new Pair<>(clientId, sellerId));
        try {
            return myLib.get("sales", clientId + "=" + sellerId).isPresent();
        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public OptionalDouble getPayment(String clientId, String sellerId) {
//        Double dbl = sales.get(new Pair<>(clientId, sellerId));
//        if (dbl == null) return OptionalDouble.empty();
//        else return OptionalDouble.of(dbl);
        try {
            Optional<String> opt = myLib.get("sales", clientId + "=" + sellerId);
            return opt.map(s -> OptionalDouble.of(Double.parseDouble(s))).orElseGet(OptionalDouble::empty);
        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }
        return OptionalDouble.empty();
    }

//    //Descending in value, Ascending in ID
//    public int sellerOrClientComparator(Pair<String, Double> e1, Pair<String, Double> e2) {
//        if (!e1.getValue().equals(e2.getValue())) return e2.getValue().compareTo(e1.getValue());
//        return e1.getKey().compareTo(e2.getKey());
//    }

    @Override
    public List<String> getBiggestSpenders() {
//        return clientSpending.entrySet().stream().map(x -> new Pair<>(x.getKey(), x.getValue())).sorted(this::sellerOrClientComparator)
//                .map(Pair::getKey).collect(Collectors.toList());
        LinkedList<String> biggest = new LinkedList<>();
        try {
            for (int i = 0; i < 10; i++) {

                Optional<String> opt = myLib.get("topClientPayments", i);
                if (!opt.isPresent()) break;

                biggest.addLast(opt.get());
            }

        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }

        return biggest;
    }

    @Override
    public List<String> getRichestSellers() {
//        return sellerProfits.entrySet().stream().map(x -> new Pair<>(x.getKey(), x.getValue()))
//                .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());

        LinkedList<String> biggest = new LinkedList<>();
        try {
            for (int i = 0; i < 10; i++) {

                Optional<String> opt = myLib.get("topSellerProfits", i);
                if (!opt.isPresent()) break;

                biggest.addLast(opt.get());
            }

        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }

        return biggest;

    }

    @Override
    public Optional<String> getFavoriteSeller(String clientId) {
//        List<String> temp = sales.entrySet().stream()
//                .filter(x -> x.getKey().getKey().equals(clientId))
//                .map(x -> new Pair<>(x.getKey().getValue(), x.getValue()))
//                .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());
//
//        if (temp.size() == 0) return Optional.empty();
//        else return Optional.of(temp.get(0));
        try {
            return myLib.get("faveSellerMapping", clientId);
        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }

        return Optional.empty();

    }

    @Override
    public Optional<String> getBiggestClient(String sellerId) {
//        List<String> temp = sales.entrySet().stream()
//                .filter(x -> x.getKey().getValue().equals(sellerId))
//                .map(x -> new Pair<>(x.getKey().getKey(), x.getValue()))
//                .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());
//
//        if (temp.size() == 0) return empty();
//        else return Optional.of(temp.get(0));

        try {
            return myLib.get("bigClientMapping", sellerId);
        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }


    @Override
    public Map<String, Integer> getBiggestPaymentsToSellers() {

        Map<String, Integer> result = new HashMap<>();
        try {
            for (int i = 0; i < 10; i++) {
                Optional<String> opt = myLib.get("biggestPaymentsToSellers", i);
                if(!opt.isPresent()) break;
                result.put(opt.get().split("=")[0], Integer.parseInt(opt.get().split("=")[1]));
            }

            return result;

        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }

        return result;

    }

    @Override
    public Map<String, Integer> getBiggestPaymentsFromClients() {
        Map<String, Integer> result = new HashMap<>();
        try {
            for (int i = 0; i < 10; i++) {
                Optional<String> opt = myLib.get("biggestPaymentsFromClients", i);
                if(!opt.isPresent()) break;
                result.put(opt.get().split("=")[0], Integer.parseInt(opt.get().split("=")[1]));
            }

            return result;

        } catch (StorageDoesNotExistsException e) {
            e.printStackTrace();
        }

        return result;


    }

//
//    private Map<String, Integer> getTop10Map(List<Pair<String, Double>> resultList) {
//        resultList = resultList.stream().sorted(this::sellerOrClientComparator).collect(Collectors.toList());
//        Map<String, Integer> resultMap = new HashMap<>();
//        for (int i = 0; i < 10; i++) {
//            try {
//                Pair<String, Double> currentPair = resultList.get(i);
//                resultMap.put(currentPair.getKey(), (currentPair.getValue()).intValue());
//            } catch (IndexOutOfBoundsException ignore) {
//                break;
//            }
//        }
//        return resultMap;
//    }


}
