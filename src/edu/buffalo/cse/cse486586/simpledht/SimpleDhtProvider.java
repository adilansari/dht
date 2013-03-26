package edu.buffalo.cse.cse486586.simpledht;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {
	
	private myHelper myDb;
	private SQLiteDatabase db;
	static final String TAG= "adil provider";
	private static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledht.provider";
	private static final String BASE_PATH = myHelper.TABLE_NAME;
	public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY + "/" + BASE_PATH);
	static LinkedList list;
	static SortedMap<String, String> map;
	static ExecutorService pool = Executors.newSingleThreadExecutor();
	static String suc, predec;
	static boolean flag=false;
	public static Socket socket;
	//public static String Node_id;
	
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

   
    public Uri insert(Uri uri, ContentValues values) {
    	//pass on algorithm here
    	final ContentValues v = new ContentValues(values);

    			db = myDb.getWritableDatabase();
    			String hashKey,hashNode,hashPre;
    			long rowId=1;
    			String key =(String) v.get(myHelper.KEY_FIELD);
		
    			try {
    				hashKey = genHash(key);
    				hashNode = genHash(SimpleDhtMainActivity.Node_id);
    				hashPre= genHash(predec);
		
    				if(hashKey.compareTo(hashNode) <= 0 && hashKey.compareTo(hashPre) > 0)
    					rowId= db.replace(myHelper.TABLE_NAME, myHelper.VALUE_FIELD, v);
    				/*else if(flag) {
    					rowId= db.replace(myHelper.TABLE_NAME, myHelper.VALUE_FIELD, v);
    					flag= !flag;
    				}*/
    				else {
    					String firstNode= map.get(map.firstKey());
    					if(SimpleDhtMainActivity.Node_id.equals(firstNode))
    						flag = !flag;
    					
    					String[] pair = {key, (String)v.get(myHelper.VALUE_FIELD)};
    					Message obj = new Message("insert",pair);
    					pool.execute(new Send(obj, getPortAddr(suc)));
    				}	
    			} catch (NoSuchAlgorithmException e) {
    				Log.e(TAG,e.getMessage());
    			}

    			if (rowId > 0) {
    				Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
    				getContext().getContentResolver().notifyChange(newUri, null);
    				//Log.i(TAG, "Insertion success # " + Long.toString(rowId));
    				return newUri;
    			}
    			else {
    				Log.e(TAG, "Insert to db failed");
    				return null;
    			}   	
    }

    @Override
    public boolean onCreate() {
    	Log.v(TAG, "provider created");
    	map=new TreeMap<String, String>();
		myDb = new myHelper(getContext());
		myDb.getWritableDatabase();
    	
    	
		ExecutorService e= Executors.newSingleThreadExecutor();
		e.execute(new Listener());
		      
    	return true;
    }
    
    public static void onJoin(String n_id) {
    	if(!map.containsValue(n_id))
			try {
				String h=new SimpleDhtProvider().genHash(n_id); 
				map.put(h, n_id);
				Log.v(TAG, "inserted "+n_id+" hash "+h);
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG,e.getMessage());
			}
    	
    	list = new LinkedList();
    	for(Map.Entry<String, String> entry: map.entrySet()) {
    		list.add(entry.getValue());
    	}
    	
    	//ExecutorService e= Executors.newSingleThreadExecutor();
    	Node temp= list.root;
    	do {
    		String s= temp.data;
    		int portAddr= getPortAddr(s);
    		String[] nb= new String[2];
    		Node loc= list.get(s);
    		nb[0]= loc.prev.data;
    		nb[1]= loc.next.data;
    		Message msg= new Message("update",SimpleDhtMainActivity.Node_id ,nb);
    		pool.execute(new Send(msg,portAddr));
    		temp=temp.next;
    	} while(temp != list.root);
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
    	Log.d(TAG, "successor "+suc+" predecessor "+predec);
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	
    	db= myDb.getReadableDatabase();
    	Cursor c;
    	//c= db.query(myHelper.TABLE_NAME, new String[]{myHelper.KEY_FIELD,myHelper.VALUE_FIELD}, "key = ?", new String[]{selection}, null, null, null);
    	if(selection != null)
    		c= db.rawQuery("select * from "+myHelper.TABLE_NAME+" where key like '"+selection+"'", null);
    	else
    		c= db.rawQuery("select * from "+myHelper.TABLE_NAME, null);
    	
		return c;
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
	ObjectInputStream in =null;
	static final String TAG = "adil rcvr";
	
	Receiver (Socket s) {
		this.sock= s;
	}
	
	public void run() {
		try {
			in =new ObjectInputStream(sock.getInputStream());
			obj = (Message) in.readObject();
			
			Log.i(TAG, "recvd msg: "+ obj.Node_id + obj.id);
			
			if (obj.id.equals("join")) {
				SimpleDhtProvider.onJoin(obj.Node_id);
			}
			else if (obj.id.equals("update")) {
				SimpleDhtProvider.onUpdate(obj.nbors);
			}
			else if (obj.id.equals("insert")) {
				ContentValues val = new ContentValues();
				val.put(myHelper.KEY_FIELD, obj.cv[0]);
				val.put(myHelper.VALUE_FIELD, obj.cv[1]);
				SimpleDhtMainActivity.mContentResolver.insert(SimpleDhtProvider.CONTENT_URI, val);
			}
		} catch (ClassNotFoundException e) {
			Log.e(TAG, ""+e.getMessage());
		} catch (StreamCorruptedException e) {
			Log.e(TAG, ""+e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, ""+e.getMessage());
		} finally {
			if (in!= null)
				try {
					in.close();
				} catch (IOException e) {
					Log.e(TAG, ""+e.getMessage());
				}
			if(sock!=null)
				try {
					sock.close();
				} catch (IOException e) {
					Log.e(TAG, ""+e.getMessage());
				}	
		}
	}
}

class Listener implements Runnable {
	
	static final String TAG = "adil listen";
	static final int recvPort= 10000;
	ExecutorService e= Executors.newSingleThreadExecutor();
	
	public void run() {
		Socket sock1= null;
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
				e.execute(new Receiver(sock1)); //replace where to send this object
			} catch (IOException e) {
				Log.e(TAG, ""+e.getMessage());
			}
		}
	}
}
