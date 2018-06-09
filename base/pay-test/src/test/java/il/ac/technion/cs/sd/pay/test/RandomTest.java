package il.ac.technion.cs.sd.pay.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.pay.app.PayBookInitializer;
import il.ac.technion.cs.sd.pay.app.PayBookReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RandomTest {
    @Rule
    public Timeout globalTimeout = Timeout.seconds(300);

    private static PayBookReader setupAndGetReader(String fileName) throws FileNotFoundException {
        String fileContents =
                new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
        Injector injector = Guice.createInjector(new PayBookModule());
        injector.getInstance(PayBookInitializer.class).setup(fileContents);
        return injector.getInstance(PayBookReader.class);
    }

    @Test(timeout = 30000)
    public void testPaidTo() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        assertFalse(reader.paidTo("3547", "131"));
        assertFalse(reader.paidTo("9463", "472"));
        assertFalse(reader.paidTo("5925", "339"));
        assertFalse(reader.paidTo("4263", "484"));
        assertFalse(reader.paidTo("7881", "281"));
    }

    @Test(timeout = 30000)
    public void testGetPayment() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        assertEquals(OptionalDouble.of(7), reader.getPayment("9602", "425"));
        assertEquals(OptionalDouble.of(31), reader.getPayment("4313", "459"));
        assertEquals(OptionalDouble.of(25), reader.getPayment("5715", "252"));
        assertEquals(OptionalDouble.of(24), reader.getPayment("1362", "631"));
        assertEquals(OptionalDouble.of(5), reader.getPayment("5729", "851"));
    }

    @Test(timeout = 30000)
    public void testBiggestSpenders() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        assertEquals(Arrays.asList("2448", "6716", "1560", "1720", "5261", "5882", "3217", "3471", "4920", "6986"), reader.getBiggestSpenders());
    }

    @Test(timeout = 30000)
    public void testRichestSellers() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        assertEquals(Arrays.asList("1", "425", "46", "84", "159", "497", "679", "383", "389", "444"), reader.getRichestSellers());
    }

    @Test
    public void testFavoriteSeller() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        assertEquals(Optional.of("507"), reader.getFavoriteSeller("383"));
        assertEquals(Optional.of("182"), reader.getFavoriteSeller("9884"));
        assertEquals(Optional.of("800"), reader.getFavoriteSeller("8876"));
        assertEquals(Optional.of("753"), reader.getFavoriteSeller("6007"));
        assertEquals(Optional.of("109"), reader.getFavoriteSeller("2176"));
    }

    @Test(timeout = 30000)
    public void testBiggestClient() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        assertEquals(Optional.of("6491"), reader.getBiggestClient("33"));
        assertEquals(Optional.of("5278"), reader.getBiggestClient("9"));
        assertEquals(Optional.of("3811"), reader.getBiggestClient("664"));
        assertEquals(Optional.of("8500"), reader.getBiggestClient("270"));
        assertEquals(Optional.of("763"), reader.getBiggestClient("623"));
    }

    @Test(timeout = 30000)
    public void testBiggestPaymentsToSellers() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        Map<String, Integer> result = reader.getBiggestPaymentsToSellers();
        assertEquals(50, (int) result.get("463"));
        assertEquals(46, (int) result.get("55"));
        assertEquals(46, (int) result.get("77"));
        assertEquals(45, (int) result.get("310"));
        assertEquals(45, (int) result.get("609"));
        assertEquals(45, (int) result.get("861"));
        assertEquals(44, (int) result.get("171"));
        assertEquals(44, (int) result.get("370"));
        assertEquals(44, (int) result.get("427"));
        assertEquals(44, (int) result.get("514"));
    }

    @Test(timeout = 30000)
    public void testBiggestPaymentsFromClients() throws Exception {
        PayBookReader reader = setupAndGetReader("RandomTest.xml");
        Map<String, Integer> result = reader.getBiggestPaymentsFromClients();
        assertEquals(50, (int) result.get("8622"));
        assertEquals(46, (int) result.get("4274"));
        assertEquals(46, (int) result.get("5623"));
        assertEquals(45, (int) result.get("447"));
        assertEquals(45, (int) result.get("6"));
        assertEquals(45, (int) result.get("7273"));
        assertEquals(44, (int) result.get("1572"));
        assertEquals(44, (int) result.get("1648"));
        assertEquals(44, (int) result.get("2408"));
        assertEquals(44, (int) result.get("2422"));
    }
}