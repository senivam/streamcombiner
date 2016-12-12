package com.oracle.homework.maxim.nesen.streamcombiner.receiver;

import com.oracle.homework.maxim.nesen.streamcombiner.common.MainConstants;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.model.Data;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.xml.XMLReceiver;
import com.oracle.homework.maxim.nesen.streamcombiner.sender.Sender;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Stram combiner receivers tests
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ReceiverTest {

    private List<String> connections;
    private Observer observer;
    private final static ExecutorService newSenderExecutor = Executors.newSingleThreadExecutor();

    @BeforeClass
    public static void setUpSender() {
        int port = 8088;
        final Properties props = new Properties();
        props.setProperty(MainConstants.DATE_GENERATION_PROPERTY_NAME,"250");
        props.setProperty(MainConstants.XML_ELEMENTS_AMOUNT_PROPERTY_NAME,"1000");
        final Sender sender  = new Sender(port,props);
        newSenderExecutor.execute(sender);
        try {
            Thread.sleep(10000);//ensure sender is started before calling recipients
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() {
        connections = new ArrayList<String>();
        connections.add("localhost:8088");
    }

    @Test
    public void testSingleThread1000Inputs() {

        System.out.println("********************");
        final ExecutorService receiverExecutor = Executors.newSingleThreadExecutor();
        System.out.println("Testing XML generation using 1 thread and 4 data changes. Expected result is sum of 0 to 999 elements divided into 4 data cells");
        System.out.println("The sum is: "+999*1000/2);
        System.out.println("The sum per cell is: 1 cell: "+249*250/2);
        System.out.println("                     2 cell: "+(499*500-249*250)/2);
        System.out.println("                     3 cell: "+(750*749-499*500)/2);
        System.out.println("                     4 cell: "+(999*1000-750*749)/2);
        receiverExecutor.execute(new XMLReceiver(connections.get(0), observer = new Observer(1)));
        try {
            receiverExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(0, observer.getCounter());
        Assert.assertEquals(4, observer.getContent().getJsonPrepare().size());
        final List<Data> dataList = observer.getContent().getJsonPrepare();
        Assert.assertEquals(249*250/2, dataList.get(0).getAmount().intValue());
        Assert.assertEquals((499*500-249*250)/2, dataList.get(1).getAmount().intValue());
        Assert.assertEquals((750*749-499*500)/2, dataList.get(2).getAmount().intValue());
        Assert.assertEquals((999*1000-750*749)/2, dataList.get(3).getAmount().intValue());
        BigDecimal amount = BigDecimal.ZERO;
        for (final Data data :dataList) {
            amount = amount.add(data.getAmount());
        }
        Assert.assertEquals(999*1000/2, amount.intValue());
        receiverExecutor.shutdown();
        System.out.println("********************");
        System.out.println("Test passed");
    }

    @Test
    public void testDoubleThread1000Inputs() {
        System.out.println("********************");
        System.out.println("Testing XML generation using 2 thread and 4 data changes. Expected result is sum of 0 to 999 elements in two threads");
        System.out.println("The sum is (for 2 threads): "+999*1000);
        final ExecutorService receiverExecutor = Executors.newFixedThreadPool(2);
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer = new Observer(2))));
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer)));
        try {
            receiverExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(0, observer.getCounter());
        final List<Data> dataList = observer.getContent().getJsonPrepare();
        BigDecimal amount = BigDecimal.ZERO;
        for (final Data data :dataList) {
           amount = amount.add(data.getAmount());
        }
        Assert.assertEquals(999*1000, amount.intValue());
        receiverExecutor.shutdown();
        System.out.println("********************");
        System.out.println("Test passed");
    }

    @Test
    public void testTripleThread1000Inputs() {
        System.out.println("********************");
        System.out.println("Testing XML generation using 2 thread and 4 data changes. Expected result is sum of 0 to 999 elements in three threads");
        System.out.println("The sum is (for 3 threads): "+999*1000*3/2);
        final ExecutorService receiverExecutor = Executors.newFixedThreadPool(3);
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer = new Observer(3))));
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer)));
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer)));
        try {
            receiverExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(0, observer.getCounter());
        final List<Data> dataList = observer.getContent().getJsonPrepare();
        BigDecimal amount = BigDecimal.ZERO;
        for (final Data data :dataList) {
           amount = amount.add(data.getAmount());
        }
        Assert.assertEquals(999*1000*3/2, amount.intValue());
        receiverExecutor.shutdown();
        System.out.println("********************");
        System.out.println("Test passed");
    }

    @Test
    public void testQuadThread1000Inputs() {
        System.out.println("********************");
        System.out.println("Testing XML generation using 2 thread and 4 data changes. Expected result is sum of 0 to 999 elements in four threads");
        System.out.println("The sum is (for 4 threads): "+999*1000*2);
        final ExecutorService receiverExecutor = Executors.newFixedThreadPool(4);
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer = new Observer(4))));
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer)));
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer)));
        receiverExecutor.execute((new XMLReceiver(connections.get(0), observer)));
        try {
            receiverExecutor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(0, observer.getCounter());
        final List<Data> dataList = observer.getContent().getJsonPrepare();
        BigDecimal amount = BigDecimal.ZERO;
        for (final Data data :dataList) {
           amount = amount.add(data.getAmount());
        }
        Assert.assertEquals(999*1000*2, amount.intValue());
        receiverExecutor.shutdown();
        System.out.println("********************");
        System.out.println("Test passed");
    }

    @AfterClass
    public static void afterTestAreDone() {
        newSenderExecutor.shutdown();
    }

}
