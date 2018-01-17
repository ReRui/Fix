package account;

import java.util.concurrent.locks.ReentrantLock;
//import java.lang.*;

public class Account {
    double amount;
    String  name;

    //constructor
  public Account(String nm,double amnt ) {
ReentrantLock lock0 = new ReentrantLock(true);lock0.lock();        amount=amnt;
        name=nm;
lock0.unlock();  }
  //functions
  synchronized  void depsite(double money){
      amount+=money;
      }

  synchronized  void withdraw(double money){
      amount-=money;
      }

  synchronized  void transfer(Account ac,double mn){
ReentrantLock lock1 = new ReentrantLock(true);lock1.lock();      amount-=mn;
      //System.out.println("ac.amount is $"+ac.amount);
      if (name.equals("D")) {
	System.out.println("unprotected");
        ac.amount+=mn;//no aquire for the other lock!!
                  //+= might cause problem --it is not atomic.
      } else {
	//System.out.println("protected");
	synchronized (ac) { ac.amount+=mn; }
      }
lock1.unlock();  }

 synchronized void print(){
  }

      }//end of class Acount
