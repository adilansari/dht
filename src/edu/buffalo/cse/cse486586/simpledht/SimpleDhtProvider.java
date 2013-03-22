package edu.buffalo.cse.cse486586.simpledht;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {
	
	private myHelper myDb;
	static final String TAG= "adil provider";
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledht.provider";
	private static final String BASE_PATH = myHelper.TABLE_NAME;
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/" + BASE_PATH);
	static Vector<String> list = new Vector<String>();
	static ExecutorService pool = Executors.newFixedThreadPool(3);
	static String suc, predec;
	
	
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
    	Log.v(TAG, "provider created");
		myDb = new myHelper(getContext());
		myDb.getWritableDatabase();
    	
    	
		ExecutorService e= Executors.newSingleThreadExecutor();
		e.execute(new Listener());
		
		//Message j = new Message("join", SimpleDhtMainActivity.Node_id);
    	//pool.execute(new Send(j,11108));
      
    	return true;
    }
    
    public static void onJoin(String n_id) {
    	list.add(n_id);
    	//ExecutorService e= Executors.newSingleThreadExecutor();
    	for(String s: list) {
    		int portAddr= getPortAddr(s);
    		String[] nb= new String[2];
    		int loc= list.indexOf(s);
    		for(int i =0; i <=1; i++) {
    			nb[i] = list.get((loc + i+1) % list.size());
    		}
    		Message msg= new Message("update",SimpleDhtMainActivity.Node_id ,nb);
    		pool.execute(new Send(msg,portAddr));
    	}
    }
    
    public static int getPortAddr(String n_id) {
    	if(n_id.equals("5554"))
    		return 11108;
    	else if(n_id.equals("5556"))
    		return 11112;
    	else if(n_id.equals("5558"))
    		return 11116;
    	return -1;
    }
    
    public static void onUpdate(String[] a) {
    	suc= a[0];
    	predec = a[1];
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
	//ExecutorService e= Executors.newSingleThreadExecutor();
	
	Receiver (Message s) {
		this.obj= s;
	}
	
	public void run() {
		if (obj.id.equals("join")) {
			SimpleDhtProvider.onJoin(obj.Node_id);
		}
		if (obj.id.equals("update")) {
			SimpleDhtProvider.onUpdate(obj.nbors);
		}
		Log.i("adil rcvr", "recvd msg: "+ obj.Node_id);
	}
}

class Listener implements Runnable {
	
	static final String TAG = "adil listen";
	static final int recvPort= 10000;
	//ExecutorService e= Executors.newSingleThreadExecutor();
	
	public void run() {
		Socket sock1= null;
		ObjectInputStream in =null;
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
					SimpleDhtProvider.pool.execute(new Receiver(obj)); //replace where to send this object
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
}
