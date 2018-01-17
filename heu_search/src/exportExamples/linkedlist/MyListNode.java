package linkedlist;

import java.util.concurrent.locks.ReentrantLock;
//MyListNode.java
    //This class implements basic node stored in a linked list .

    class MyListNode
    {
		/*Class Members*/
		public Object _element;			//Node's data
        public MyListNode _next;		//Pointer to next node

		//C'tor - 1
        MyListNode( Object theElement ){ this( theElement, null ); }

		//C'tor - 2
        MyListNode( Object theElement, MyListNode n )
        {
			synchronized ( this )
			{
ReentrantLock lock0 = new ReentrantLock(true);lock0.lock();				this._element = theElement;
				this._next = n;
lock0.unlock();            }

        }

    }
