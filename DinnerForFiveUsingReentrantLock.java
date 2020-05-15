import java.util.concurrent.locks.*;

public class DinnerForFiveUsingReentrantLock {

   ReentrantLock lock = new ReentrantLock();
   Condition[] forkReady = new Condition[5];
   boolean[] fork = new boolean[5];

   DinnerForFive() {
      for (int i = 0; i < 5; i++) {
         forkReady[i] = lock.newCondition();
      }
      for (int i = 0; i < 5; i++) {
         fork[i] = true;
      }
   }

   public void getForks(int id) {
      int left = id;
      int right = (id + 1) % 5;

      lock.lock();

      while (!fork[left]) {
         try {
            System.out.println(id + " waiting for fork " + left);
            forkReady[left].await();
         } catch (InterruptedException e) {} // spurious wake-up
      }
      System.out.println(id + " received fork " + left);
      fork[left] = false;

      while (!fork[right]) {
         try {
            System.out.println(id + " waiting for fork " + right);
            forkReady[right].await();
         } catch (InterruptedException e) {} // spurious wake-up
      }
      System.out.println(id + " received fork " + right);
      fork[right] = false;
         
      lock.unlock();
   }

   public void putForks(int id) {
      int left = id;
      int right = (id + 1) % 5;

      lock.lock();

      System.out.println(id + " putting fork " + left);
      
      fork[left] = true;
      if (lock.hasWaiters(forkReady[left])) {
         System.out.println(id + " signal " + left + " can grab fork");
         forkReady[left].signal();
      } 

      System.out.println(id + " putting fork " + right);
      
      fork[right] = true;
      if (lock.hasWaiters(forkReady[right])) {
         System.out.println(id + " signal " + right + " can grab fork");
         forkReady[right].signal();
      } 
      
      lock.unlock();
   }

   public static void main(String[] args) {

      DinnerForFive dinner = new DinnerForFive();

      for (int id = 0; id < 5; id++) {

         int current = id;

         new Thread(() -> {
            while (true) {

               System.out.println(current + " hungry");

               dinner.getForks(current);

               System.out.println(current + " eating");
               try {
                  Thread.sleep(1000);
               } catch (InterruptedException e) {}

               dinner.putForks(current);
               try {
                  Thread.sleep(1000);
               } catch (InterruptedException e) {}

               System.out.println(current + " thinking");
               try {
                  Thread.sleep(1000);
               } catch (InterruptedException e) {}
            }
         }).start();
      }
   }
}