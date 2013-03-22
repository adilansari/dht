package edu.buffalo.cse.cse486586.simpledht;

class Node {
	 
    String data;
    Node prev;
    Node next;
 
    public Node( String data ) {
        this.data = data;
    }
 
    public Node( String data, Node prev, Node next ) {
        this.data = data;
        this.prev = prev;
        this.next = next;
    }
}
 
public class LinkedList {
 
    Node root;
 
    public synchronized void add( String data ) {
        if( root == null ) {
            root = new Node( data );
            root.prev = root;
            root.next = root;
        } else {
            root.next = new Node( data, root, root.next );
            if( root.prev == root ) {
            	root.prev = root.next;
            } else {
                root.next.next.prev = root.next;
            }
        }
    }
 
  
    public synchronized boolean contains( String data ) {
        if( root != null ) {
            Node itr = root; 
            while(itr.next != root) {
            	if(itr.data == data)
            		return true;
            	itr = itr.next;
            }
        }
        return false;
    }
 
    public synchronized Node get( String data ) {
        if( root != null ) {
        	Node itr = root; 
            do {
                if( itr.data.equals( data ) ) {
                    return itr;
                }
                itr = itr.next;
            } while( itr != root );
        }
        return null;
    }
}