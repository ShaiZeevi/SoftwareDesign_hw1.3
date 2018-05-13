package il.ac.technion.cs.sd.pay.app;

import SYlib.CollectionAlreadyExistsException;
import SYlib.ISYLibable;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Yoav Zuriel on 5/3/2018.
 */
public class myPayBookInitializer implements PayBookInitializer {
    private ISYLibable myLib;
    private Map<String, Double> clientSpending;
    private Map<String, Double> sellerProfits;
    private Map<Pair<String, String>, Double> sales;

    @Inject
    public myPayBookInitializer(ISYLibable lib) {
        this.myLib = lib;
    }

    @Override
    public void setup(String xmlData) {
        DocumentBuilderFactory herFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder hisBuilder = herFactory.newDocumentBuilder();
            Document theirsDoc = hisBuilder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
            NodeList ourList = theirsDoc.getElementsByTagName("Client");

            readPayments(ourList);

            String clientCollectionName = "clientSpending";
            myLib.saveCollection(clientCollectionName, clientSpending.entrySet().stream().map(Object::toString).collect(Collectors.toList()));
            String sellerCollectionName = "sellerProfits";
            myLib.saveCollection(sellerCollectionName, sellerProfits.entrySet().stream().map(Object::toString).collect(Collectors.toList()));
            String clientSellerCollectionName = "sales";
            myLib.saveCollection(clientSellerCollectionName, sales.entrySet().stream().map(Object::toString).collect(Collectors.toList()));

        } catch (ParserConfigurationException | SAXException | IOException | CollectionAlreadyExistsException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    private void readPayments(NodeList ourList) {
        clientSpending = new HashMap<>();
        sellerProfits = new HashMap<>();
        sales = new HashMap<>();

        for (int client = 0; client < ourList.getLength(); client++) {
            Element currentClientNode = (Element) ourList.item(client);
            NodeList paymentList = currentClientNode.getElementsByTagName("Payment");
            if (paymentList.getLength() == 0){
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

                if (!sales.containsKey(new Pair<>(clientID, sellerName)))
                    sales.put(new Pair<>(clientID, sellerName), amount);
                else sales.put(new Pair<>(clientID, sellerName), sales.get(new Pair<>(clientID, sellerName)) + amount);

            }
        }

    }
}
