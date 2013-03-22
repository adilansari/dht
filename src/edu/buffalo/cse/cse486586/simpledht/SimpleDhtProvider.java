package edu.buffalo.cse.cse486586.simpledht;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {
	
	static final String TAG= "adil provider";
	static final int recvPort= 10000;
	
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
    	
    	//Listener Thread
    	new Thread(new Runnable() {
        	public void run() {
        		Socket sock1= null;
        		ObjectInputStream in =null;
        		ExecutorService te= Executors.newSingleThreadExecutor();
        		ServerSocket servSocket= null;
        		try {
					servSocket= new ServerSocket(recvPort);
					Log.v(TAG, "Server Socket port: "+Integer.toString(servSocket.getLocalPort()));
				} catch (IOException e) {
					Log.e(TAG, ""+e.getMessage());
					e.printStackTrace();
				}
        		
        		while(true) {
        			try {
        				sock1= servSocket.accept();
        				in =new ObjectInputStream(sock1.getInputStream());
        				Message obj;
        				try {
							obj = (Message) in.readObject();
							te.execute(new Receiver(obj)); //replace where to send this object
						} catch (ClassNotFoundException e) {
							Log.e(TAG, e.getMessage());
						}
       				} 
        			
        			catch (IOException e) {
        				Log.e(TAG, ""+e.getMessage());
        				e.printStackTrace();
        			}
        			finally {
        				if (in!= null)
        					try {
        						in.close();
        					} catch (IOException e) {
        						Log.e(TAG, ""+e.getMessage());
        					}
        				if(sock1!=null)
        					try {
        						sock1.close();
        					} catch (IOException e) {
        						Log.e(TAG, ""+e.getMessage());
        					}	
        			}
        		}
        	}
        }).start();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }  
}

class Receiver implements Runnable {

	Socket sock= null;
	Message obj;
	ExecutorService e= Executors.newSingleThreadExecutor();
	
	Receiver (Message s) {
		this.obj= s;
	}
	
	public void run() {
		Log.i("adil executor", "recvd msg: ");
	}
}
