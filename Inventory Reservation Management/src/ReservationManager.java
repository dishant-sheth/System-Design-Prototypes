package src;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import src.exceptions.CannotProcessReservationException;
import src.exceptions.ExpiredReservationProcessingException;
import src.exceptions.InvalidReservationActionException;
import src.exceptions.InvalidReservationException;
import src.exceptions.ProductNotFoundException;
import src.interfaces.InventoryRepository;
import src.models.Reservation;
import src.models.ReservationState;

public class ReservationManager {

    class ReservationExpiry implements Delayed {

        final String reservationId;
        final long expiryAt;

        public ReservationExpiry(final String reservationId, final long expiryAt){
            this.reservationId = reservationId;
            this.expiryAt = expiryAt;
        }

        public String getReservationId(){
            return this.reservationId;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expiryAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.expiryAt, ((ReservationExpiry)o).expiryAt);
        }

    }

    // Reservation ID -> state map
    final Map<String, Reservation> reservationMap;
    final Map<String, ReentrantLock> reservationLockMap;
    final DelayQueue<ReservationExpiry> reservationExpiryQueue;

    class ReservationExpiryJob implements Runnable {

        private final AtomicBoolean kill;

        public ReservationExpiryJob(){
            this.kill = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            while (!this.kill.get()) { 
                try { 
                    ReservationExpiry reservationExpiry = reservationExpiryQueue.take();
                    final String reservationId = reservationExpiry.getReservationId();
                    ReentrantLock reservationReadLock = reservationLockMap.get(reservationId);
                    try {
                        reservationReadLock.lock();
                        Reservation reservation = reservationMap.get(reservationId);
                        if(reservation.getState() != ReservationState.RESERVED){
                            System.out.println("Reservation has already moved past expiry " + reservationId);
                        } else {
                            inventoryRepository.returnInventory(reservation.productId, reservation.quantity);
                            reservation.setState(ReservationState.EXPIRED);
                        }
                    } finally {
                        reservationReadLock.unlock();
                    }
                } catch (InterruptedException ex) {
                    throw new ExpiredReservationProcessingException(ex.getMessage());
                }
            }
        }

        public void kill(){
            this.kill.set(true);
        }
    }

    final InventoryRepository inventoryRepository;
    final Thread expiryManagementJob;

    public ReservationManager(final InventoryRepository inventoryRepository){
        this.inventoryRepository = inventoryRepository;
        this.reservationMap = new ConcurrentHashMap<>();
        this.reservationLockMap = new HashMap<>();
        this.reservationExpiryQueue = new DelayQueue<>();
        this.expiryManagementJob = new Thread(new ReservationExpiryJob());
        this.expiryManagementJob.start();
    }

    public String reserve(final String productId, final int quantity, final long ttlInSeconds){
        if(!inventoryRepository.isValidInventory(productId)){
            throw new ProductNotFoundException(productId);
        }

        // Check if inventory can be reserved.
        try {
            inventoryRepository.reserveInventory(productId, quantity);
        } catch (Exception e) {
            throw new CannotProcessReservationException(e.getMessage());
        }

        final String uuid = UUID.randomUUID().toString();
        final Reservation reservation = new Reservation(uuid, productId, quantity, ttlInSeconds);
        this.reservationMap.put(uuid, reservation);
        this.reservationLockMap.put(uuid, new ReentrantLock());

        // Manage expiry
        reservationExpiryQueue.offer(new ReservationExpiry(uuid, reservation.getExpiry()));
        return uuid;
    }

    public void confirmReservation(final String reservationId){
        if(!reservationMap.containsKey(reservationId)){
            throw new InvalidReservationException(reservationId);
        }

        final ReentrantLock reservationLock = reservationLockMap.get(reservationId);
        try {
            reservationLock.lock();
            final Reservation reservation = reservationMap.get(reservationId);
            if(reservation.getState() != ReservationState.RESERVED){
                throw new InvalidReservationActionException(reservation.getState());
            }

            inventoryRepository.consumeInventory(reservation.productId, reservation.quantity);
            reservation.setState(ReservationState.CONSUMED);
        } catch (final Exception e) {
            throw new CannotProcessReservationException(e.getMessage());
        } finally {
            reservationLock.unlock();
        }

    }

    public void releaseReservation(final String reservationId){
        if(!reservationMap.containsKey(reservationId)){
            throw new InvalidReservationException(reservationId);
        }

        final ReentrantLock reservationLock = reservationLockMap.get(reservationId);
        try {
            reservationLock.lock();
            final Reservation reservation = reservationMap.get(reservationId);
            if(reservation.getState() != ReservationState.RESERVED){
                throw new InvalidReservationActionException(reservation.getState());
            }

            inventoryRepository.returnInventory(reservation.productId, reservation.quantity);
            reservation.setState(ReservationState.RELEASE);
        } catch (final Exception e) {
            throw new CannotProcessReservationException(e.getMessage());
        } finally {
            reservationLock.unlock();
        }
    }


}