package il.ac.technion.cs.sd.pay.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.pay.app.PayBookInitializer;
import il.ac.technion.cs.sd.pay.app.PayBookReader;
import il.ac.technion.cs.sd.pay.ext.SecureDatabaseModule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    private static PayBookReader setupAndGetReader(String fileName) throws FileNotFoundException {
        String fileContents =
                new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
        Injector injector = Guice.createInjector(new PayBookModule(), new SecureDatabaseModule());
        injector.getInstance(PayBookInitializer.class).setup(fileContents);
        return injector.getInstance(PayBookReader.class);
    }

    @Test
    public void testSimple() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertEquals(Arrays.asList("Foobar", "Boobar", "Moobar"), reader.getRichestSellers());
        assertEquals(OptionalDouble.of(10.0), reader.getPayment("123", "Foobar"));
        assertEquals(Optional.empty(), reader.getFavoriteSeller("124"));
    }

    @Test
    public void clientOrSellerDoNotExistGetPaymentTest() throws Exception {
        PayBookReader reader = setupAndGetReader("large.xml");
        assertEquals(OptionalDouble.empty(), reader.getPayment("1", "Bathtubs"));
        assertEquals(OptionalDouble.empty(), reader.getPayment("HannahB", "1"));
        assertEquals(OptionalDouble.empty(), reader.getPayment("1", "1"));
    }

    @Test
    public void getPaymentTest() throws Exception {
        PayBookReader reader = setupAndGetReader("large.xml");
        assertEquals(OptionalDouble.of(60), reader.getPayment("1234", "Pharmacy"));
        assertEquals(OptionalDouble.of(500), reader.getPayment("BasicBish", "NailsNails"));
    }

    @Test
    public void noPaymentsMadeTest() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertFalse(reader.paidTo("123", "NOSELLER"));
        assertFalse(reader.paidTo("NOCLIENT", "Foobar"));
        assertFalse(reader.paidTo("NOCLIENT", "NOSELLER"));

    }

    @Test
    public void regularPaymentTest() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertTrue(reader.paidTo("123", "Foobar"));
        assertTrue(reader.paidTo("123", "Moobar"));
        assertTrue(reader.paidTo("123", "Boobar"));
    }

    @Test
    public void getRichestSellersTest() throws Exception {
        PayBookReader reader = setupAndGetReader("large.xml");
        assertEquals(Arrays.asList("Bathtubs", "Starbucks", "NailsNails",
                "Movies", "Store", "Pharmacy", "Comics", "KnifeCo", "Primark"), reader.getRichestSellers());
    }

    @Test
    public void biggestSpendersInRightOrdersTest() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertEquals(Collections.singletonList("123"), reader.getBiggestSpenders());
    }

    @Test
    public void biggestSpendersInRightOrdersTest2() throws Exception {
        PayBookReader reader = setupAndGetReader("large.xml");
        assertEquals(Arrays.asList("HannahB", "BasicBish", "Student201", "1234"), reader.getBiggestSpenders());
    }

    @Test
    public void biggestSpendersInRightOrdersWithConflictTest() throws Exception {
        PayBookReader reader = setupAndGetReader("conflict.xml");
        assertEquals(Arrays.asList("HannahB", "BasicBish", "1234", "Student201"), reader.getBiggestSpenders());
    }

    @Test
    public void getRichestSellersConflictTest() throws Exception {
        PayBookReader reader = setupAndGetReader("conflict.xml");
        assertEquals(Arrays.asList("Bathtubs", "Starbucks", "Movies",
                "NailsNails", "Store", "Pharmacy", "KnifeCo", "Primark"), reader.getRichestSellers());
    }

    @Test
    public void getFavouriteSellerSmallTest() throws Exception{
        PayBookReader reader = setupAndGetReader("small.xml");
        assertEquals(Optional.of("Foobar"), reader.getFavoriteSeller("123"));

    }

    @Test
    public void getFavouriteSellerClientDoesntExistTest() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertEquals(Optional.empty(), reader.getFavoriteSeller("I told you I was sick"));
        reader = setupAndGetReader("large.xml");
        assertEquals(Optional.empty(), reader.getFavoriteSeller("Why?! why?! why?! why Hornine?! why?!"));
        reader = setupAndGetReader("conflict.xml");
        assertEquals(Optional.empty(), reader.getFavoriteSeller("Keep your pinkies at 90 degrees! more tea please"));
    }

    @Test
    public void getFavouriteSellerClientExistsNoConflictTest() throws Exception {
        PayBookReader reader = setupAndGetReader("large.xml");
        assertEquals(Optional.of("Bathtubs"), reader.getFavoriteSeller("HannahB"));
    }

    @Test
    public void getFavouriteSellerClientExistsConflictTest() throws Exception {
        PayBookReader reader = setupAndGetReader("conflict2.xml");
        assertEquals(Optional.of("Bathtubs"), reader.getFavoriteSeller("1234"));
    }

    @Test
    public void getBiggestClientSmallTest() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertEquals(Optional.of("123"), reader.getBiggestClient("Moobar"));
        assertEquals(Optional.of("123"), reader.getBiggestClient("Foobar"));
        assertEquals(Optional.of("123"), reader.getBiggestClient("Boobar"));
    }

    @Test
    public void getBiggestClientNoSellerTest() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        assertEquals(Optional.empty(), reader.getBiggestClient("Buzzfeed"));
    }

    @Test
    public void getBiggestClientConflictTest() throws Exception {
        PayBookReader reader = setupAndGetReader("conflict2.xml");
        assertEquals(Optional.of("1234"), reader.getBiggestClient("Bathtubs"));
    }

    @Test
    public void getBiggestPaymentsToSellersSmallTest() throws Exception {
        PayBookReader reader = setupAndGetReader("small.xml");
        HashMap<String, Integer> sellers = new HashMap<>();
        sellers.put("Foobar", 10);
        sellers.put("Moobar", 3);
        sellers.put("Boobar", 5);
        assertEquals(sellers, reader.getBiggestPaymentsToSellers());
    }

    @Test
    public void getBiggestPaymentsToSellersLargeTest() throws Exception {
        PayBookReader reader = setupAndGetReader("large.xml");
        HashMap<String, Integer> sellers = new HashMap<>();
        sellers.put("Store", 120);
        sellers.put("Pharmacy", 60);
        sellers.put("Movies", 400);
        sellers.put("KnifeCo", 12);
        sellers.put("Bathtubs", 1200);
        sellers.put("Comics", 30);
        sellers.put("NailsNails", 500);
        sellers.put("Starbucks", 750);
        sellers.put("Primark", 4);
        assertEquals(sellers, reader.getBiggestPaymentsToSellers());
    }

    @Test
    public void getBiggestPaymentsFromClientsOnlyZerosTest() throws Exception {
        PayBookReader reader = setupAndGetReader("zeros.xml");
        Map<String, Integer> payments = new HashMap<>();
//        payments.put("1234", 0);
//        payments.put("HannahB", 0);
//        payments.put("Student201", 0);
//        payments.put("BasicBish", 0);
        assertEquals(payments, reader.getBiggestPaymentsFromClients());
    }

    @Test
    public void getBiggestPaymentsFromClientsNoZerosTest() throws Exception {
        PayBookReader reader = setupAndGetReader("large.xml");
        Map<String, Integer> payments = new HashMap<>();
        payments.put("1234", 120);
        payments.put("HannahB", 1200);
        payments.put("Student201", 215);
        payments.put("BasicBish", 750);
        assertEquals(payments, reader.getBiggestPaymentsFromClients());
    }

    @Test
    public void getBiggestPaymentsFromClientsMixedTest() throws Exception {
        PayBookReader reader = setupAndGetReader("mixed.xml");
        Map<String, Integer> payments = new HashMap<>();
        payments.put("1234", 120);
        payments.put("HannahB", 1200);
        payments.put("Student201", 215);
        payments.put("BasicBish", 750);
//        payments.put("Zero1", 0);
//        payments.put("Zero2", 0);
//        payments.put("Zero3", 0);
//        payments.put("Zero4", 0);
//        payments.put("Zero5", 0);
        assertEquals(payments, reader.getBiggestPaymentsFromClients());
    }
}
