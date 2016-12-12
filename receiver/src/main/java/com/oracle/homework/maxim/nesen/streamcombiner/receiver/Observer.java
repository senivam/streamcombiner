package com.oracle.homework.maxim.nesen.streamcombiner.receiver;

import com.oracle.homework.maxim.nesen.streamcombiner.receiver.json.JSONGenerator;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.model.Content;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.model.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBException;

/**
 * Some hybrid observer/consumer implementation - just to monitor clients' execution flows.
 * This mix is chosen because the implementation shall almost not be awere of any producers. It just periodically
 * checks the buffer and performs JSON marshalling. However to properly end the stream the implementation shall know
 * that all producers are done so it can finish. So each producer at the end notifies obesrver about its finish.
 *
 * the JSON generator is run periodically dependent on the buffer state
 */
public class Observer implements IObserver, Runnable {

    /**
     * Buffer to keep some amount of processes data and pass it for marshalling
     */
    private final Content content = new Content();

    /**
     * Notifications / buffer size which initiates marshalling - after that amount buffer is flushed to JSON
     */
    private static final int MAX_NOTIFICATIONS_BUFFER_ALLOWED = 100;

    /**
     * Actual counter which is checked to start JSON marshalling and flush the buffer
     */
    private final AtomicInteger actualBufferSize = new AtomicInteger(MAX_NOTIFICATIONS_BUFFER_ALLOWED);

    /**
     * amount of known producers
     */
    private int recipientsCount = 0;

    /**
     * we lock the buffer for reading when writing and for writing when reading.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * mark to continue work - influenced by producers through wakeup method
     */
    private boolean progress = true;

    /**
     * initiates observer with amount of expected notifications from clients Notification comes as soon as the client is done (all XML stream is parsed)
     *
     */
    public Observer(int recipientsCount) {
        this.recipientsCount = recipientsCount;
    }

    /**
     * Store another amount/timestamp pair in shared Map
     *
     * @param data data
     */
    @Override
    public void addParsedPair(Data data) {
        try {
            lock.readLock().lock();
            content.processNewData(data);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * notifies observe that another producer finished its work
     * when the last producer is done the observer is set to be over as well
     */
    @Override
    public void wakeup() {
        recipientsCount--;  //decrease amount of living receivers.
        if (recipientsCount <= 0) { //if all receivers are dead it's time to finish
            progress = false;              //this indicates that the thread shall finish
            synchronized (this) {
                actualBufferSize.set(0); //reset buffer counter to zero to flush all remaining data to JSON stream.
                                         // Actually here we gain negative buffer
                                         // size because when thread will wake it will perform another decrease of the counter
                notify();                //awake the thread
            }
        }
    }

    /**
     * Method for direct access only (test purposes). Not introduced in the interface
     * @return counter count
     */
    public int getCounter() {
        return actualBufferSize.get();
    }

    /**
     * Method for direct access only (test purposes). Not introduced in the interface
     * @return content holder
     */
    public Content getContent() {
        return content;
    }

    public void run() {
        try {
            final JSONGenerator generator = new JSONGenerator(content);
            while (progress) {

                synchronized (this) {
                    wait();
                    actualBufferSize.decrementAndGet();
                }
                /*
                  The buffer is marshalled to JSON when the counter is equal or less zero.
                   negative counter is also possible because several threads could modify the counter
                   (thus notifying observer and adding data to the buffer)
                    before the lock is achieved
                    That is not critical because even if buffer is slightly bigger than expected it
                    does not influence JSON marshalling
                 */
                if (actualBufferSize.intValue() <= 0) {
                    lock.writeLock().lock();
                    generator.generate();
                    actualBufferSize.set(MAX_NOTIFICATIONS_BUFFER_ALLOWED);
                    content.clearContent();
                    lock.writeLock().unlock();
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
            if (lock.writeLock().isHeldByCurrentThread()) {
                lock.writeLock().unlock();
            }
        }

        System.out.println("\n\n\n\n\n***************");
        System.out.println("*** JSON is generated, Stream is over ***");
        System.out.println("***************");
        System.out.println("**the server stream however keeps running. So you can get connected to it (using CURL for instance) and receive an XML stream**");
        System.out.println("***************");

    }
}
