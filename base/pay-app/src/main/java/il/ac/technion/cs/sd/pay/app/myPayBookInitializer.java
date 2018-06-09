package il.ac.technion.cs.sd.pay.app;

import SYLib.ISYLibable;
import SYLib.StorageAlreadyExistsException;
import SYLib.StorageDoesNotExistsException;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

/**
 * Created by Yoav Zuriel on 5/3/2018.
 */
public class myPayBookInitializer implements PayBookInitializer {
    private ISYLibable myLib;
    private Map<String, Double> clientSpending;
    private Map<String, Double> sellerProfits;
    private Map<String, Double> sales;

    @Inject
    public myPayBookInitializer(ISYLibable lib) {
        this.myLib = lib;
    }

    //Descending in value, Ascending in ID
    public int sellerOrClientComparator(Pair<String, Double> e1, Pair<String, Double> e2) {
        if (!e1.getValue().equals(e2.getValue())) return e2.getValue().compareTo(e1.getValue());
        return e1.getKey().compareTo(e2.getKey());
    }


    @Override
    public void setup(String xmlData) {
        try {
            readXML(xmlData);

            setupSales();

            setupTopClientPayment();
            setupTopSellerProfits();


            Map<String, String> map1 = new HashMap<>();
            Map<String, String> map2 = new HashMap<>();

            myLib.openStorage("faveSellerMapping");

            for (String clientName : clientSpending.keySet()) {
                List<String> temp = sales.entrySet().stream()
                        .filter(x -> x.getKey().split("=")[0].equals(clientName))
                        .map(x -> new Pair<>(x.getKey().split("=")[1], x.getValue()))
                        .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());

                if (temp.size() > 0) {
                    myLib.addEntry("faveSellerMapping", clientName, temp.get(0));
                    map1.put(clientName, temp.get(0));
                }
            }

            myLib.openStorage("bigClientMapping");

            for (String sellerName : sellerProfits.keySet()) {
                List<String> temp = sales.entrySet().stream()
                        .filter(x -> x.getKey().split("=")[1].equals(sellerName))
                        .map(x -> new Pair<>(x.getKey().split("=")[0], x.getValue()))
                        .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());

                if (temp.size() > 0) {
                    myLib.addEntry("bigClientMapping", sellerName, temp.get(0));
                    map2.put(sellerName, temp.get(0));
                }
            }


            myLib.openStorage("biggestPaymentsToSellers");
            List<Pair<String, Double>> resultList = new LinkedList<>();

            for (String sellerId : sellerProfits.keySet()) {
                if (map2.get(sellerId) != null) {
                    String clientId = map2.get(sellerId);
                    double amount = sales.get(clientId + "=" + sellerId);
                    resultList.add(new Pair<>(sellerId, amount));

                }
            }
            Integer pos;
            pos = 0;
            for (Map.Entry<String, Integer> entry : getTop10Map(resultList).entrySet()) {
                myLib.addEntry("biggestPaymentsToSellers", pos, entry.getKey() + "=" + entry.getValue());
                pos++;
            }


            myLib.openStorage("biggestPaymentsFromClients");
            resultList = new LinkedList<>();

            for (String clientId : clientSpending.keySet()) {
                if (map1.get(clientId) != null) {
                    String sellerId = map1.get(clientId);
                    double amount = sales.get(clientId + "=" + sellerId);
                    resultList.add(new Pair<>(clientId, amount));
                }
            }

            pos = 0;
            for (Map.Entry<String, Integer> entry : getTop10Map(resultList).entrySet()) {
                myLib.addEntry("biggestPaymentsFromClients", pos, entry.getKey() + "=" + entry.getValue());
                pos++;
            }

        } catch (StorageDoesNotExistsException | DataFormatException | StorageAlreadyExistsException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    private void readXML(String xmlData) {
        try {
            DocumentBuilderFactory herFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder hisBuilder;
            hisBuilder = herFactory.newDocumentBuilder();
            Document theirsDoc = hisBuilder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
            NodeList ourList = theirsDoc.getElementsByTagName("Client");
            readPayments(ourList);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }

    private void setupTopSellerProfits() throws StorageAlreadyExistsException, StorageDoesNotExistsException, DataFormatException {
        Integer pos;

        pos = 0;
        myLib.openStorage("topSellerProfits");
        List<String> top10SellerList = sellerProfits.entrySet().stream().map(x -> new Pair<>(x.getKey(), x.getValue()))
                .sorted(this::sellerOrClientComparator).map(Pair::getKey).collect(Collectors.toList());

        for (String entry : top10SellerList.subList(0, Math.min(10, top10SellerList.size()))) {
            myLib.addEntry("topSellerProfits", pos.toString(), entry);
            pos++;
        }
    }

    private void setupTopClientPayment() throws StorageAlreadyExistsException, StorageDoesNotExistsException, DataFormatException {
        myLib.openStorage("topClientPayments");

        List<String> top10ClientsList = clientSpending.entrySet().stream().map(x -> new Pair<>(x.getKey(), x.getValue())).sorted(this::sellerOrClientComparator)
                .map(Pair::getKey).collect(Collectors.toList());

        Integer pos = 0;
        for (String entry : top10ClientsList.subList(0, Math.min(10, top10ClientsList.size()))) {
            myLib.addEntry("topClientPayments", pos.toString(), entry);
            pos++;
        }
    }

    private void setupSales() throws StorageAlreadyExistsException, StorageDoesNotExistsException, DataFormatException {
        myLib.openStorage("sales");

        for (Map.Entry<String, Double> entry : sales.entrySet()) {
            myLib.addEntry("sales", entry.getKey(), entry.getValue().toString());
        }
    }

    private void readPayments(NodeList ourList) {
        clientSpending = new HashMap<>();
        sellerProfits = new HashMap<>();
        sales = new HashMap<>();

        for (int client = 0; client < ourList.getLength(); client++) {
            Element currentClientNode = (Element) ourList.item(client);
            NodeList paymentList = currentClientNode.getElementsByTagName("Payment");
            if (paymentList.getLength() == 0) {
                clientSpending.put(currentClientNode.getAttribute("Id"), 0.);
                continue;
            }
            for (int payment = 0; payment < paymentList.getLength(); payment++) {
                if (paymentList.item(payment).getNodeType() != Node.ELEMENT_NODE) {
                    System.out.println("ERROR with " + payment);
                    continue;
                }
                Element currentPayment = (Element) (paymentList.item(payment));
                String clientID = currentClientNode.getAttribute("Id");
                String sellerName = currentPayment.getElementsByTagName("Id").item(0).getTextContent();
                Double amount = Double.parseDouble(currentPayment.getElementsByTagName("Amount").item(0).getTextContent());

                if (!clientSpending.containsKey(clientID)) clientSpending.put(clientID, amount);
                else clientSpending.put(clientID, clientSpending.get(clientID) + amount);

                if (!sellerProfits.containsKey(sellerName)) sellerProfits.put(sellerName, amount);
                else sellerProfits.put(sellerName, sellerProfits.get(sellerName) + amount);

                if (!sales.containsKey(clientID + "=" + sellerName))
                    sales.put(clientID + "=" + sellerName, amount);
                else sales.put(clientID + "=" + sellerName, sales.get(clientID + "=" + sellerName) + amount);

            }
        }

    }


    private Map<String, Integer> getTop10Map(List<Pair<String, Double>> resultList) {
        resultList = resultList.stream().sorted(this::sellerOrClientComparator).collect(Collectors.toList());
        Map<String, Integer> resultMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            try {
                Pair<String, Double> currentPair = resultList.get(i);
                resultMap.put(currentPair.getKey(), (currentPair.getValue()).intValue());
            } catch (IndexOutOfBoundsException ignore) {
                break;
            }
        }
        return resultMap;
    }
}
