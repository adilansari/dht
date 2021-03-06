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
import android.database.MatrixCursor;
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
	static ExecutorService pool = Executors.newFixedThreadPool(2);
	static String suc, predec, leader;
	static boolean query_flag=false, ins_flag =false, dump_flag = false;
	public static BlockingQueue<Long> bq = new LinkedBlockingQueue<Long>(1);
	public static BlockingQueue<String> bq2 = new LinkedBlockingQueue<String>(1);
	public static BlockingQueue<HashMap<String, String>> bq_map = new LinkedBlockingQueue<HashMap<String, String>>(1);
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

   
    public Uri insert(Uri uri, ContentValues values) {
    	//pass on algorithm here
    	final ContentValues v = new ContentValues(values);

    	db = myDb.getWritableDatabase();
    	String hashKey,hashNode,hashPre;
    	long rowId=0;
    	String key =(String) v.get(myHelper.KEY_FIELD);
		
    	try {
    		hashKey = genHash(key);
    		hashNode = genHash(SimpleDhtMainActivity.Node_id);
    		hashPre= genHash(predec);
		
    		if(hashKey.compareTo(hashNode) <= 0 && hashKey.compareTo(hashPre) > 0)
    			rowId= db.replace(myHelper.TABLE_NAME, myHelper.VALUE_FIELD, v);
    		else if(SimpleDhtMainActivity.Node_id.equals(leader) && (hashKey.compareTo(hashNode) <= 0 || hashKey.compareTo(hashPre) > 0)) {
    			rowId= db.replace(myHelper.TABLE_NAME, myHelper.VALUE_FIELD, v);
    		}
    		else {
    			String[] pair = {key, (String)v.get(myHelper.VALUE_FIELD)};
    			Message obj = new Message("insert",pair);
    			pool.execute(new Send(obj, getPortAddr(suc)));
    		
    			rowId = bq.take();
    			bq.clear();
    		}	
   		} catch (NoSuchAlgorithmException e) {
   			Log.e(TAG,e.getMessage());
   		} catch (InterruptedException e) {
   			Log.e(TAG,e.getMessage());
		}

    	if (rowId > 0) {
    		if(ins_flag) {
    			Message m = new Message("ins_reply", rowId);
    			pool.execute(new Send(m, getPortAddr(predec)));
    			ins_flag = false;
    		}
    			
    		Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
    		getContext().getContentResolver().notifyChange(newUri, null);
    					
    		return newUri;
    	}
    	else {
    		Log.e(TAG, "Insert to db failed");
    		return null;
    	}   	
    }

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
    	
    	Node temp= list.root;
    	do {
    		String s= temp.data;
    		int portAddr= getPortAddr(s);
    		String[] nb= new String[3];
    		Node loc= list.get(s);
    		nb[0]= loc.prev.data;
    		nb[1]= loc.next.data;
    		nb[2] = map.get(map.firstKey());
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
    	leader = a[2];
    	Log.d(TAG, "successor "+suc+" || predecessor "+predec+" || leader "+ leader);
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
    	
    	db= myDb.getReadableDatabase();
    	Cursor c;
    	MatrixCursor mc = new MatrixCursor(new String[]{myHelper.KEY_FIELD,myHelper.VALUE_FIELD});
    	
    	if(selection != null) {
    		c= db.rawQuery("select * from "+myHelper.TABLE_NAME+" where key like '"+selection+"'", null);
    		String val = null;
    		if (c.getCount()<=0) {
    			//if(!query_flag) {	
    				Message q = new Message("query", selection, sortOrder);
    				pool.execute(new Send(q, getPortAddr(suc)));
    				try {
						val = bq2.take();
						bq2.clear();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			//} else {
    				//the key doesn't exist in the dht, return null
    			//}
    		} else {
    			int colIndex = c.getColumnIndex(myHelper.VALUE_FIELD);
    			c.moveToFirst();
    			val = c.getString(colIndex);
    		}
    		
    		if (query_flag) {
    			Message r = new Message("q_reply", val);
        		pool.execute(new Send(r, getPortAddr(predec)));
    			query_flag = false;	
    		}
    		
    		mc.newRow().add(selection).add(val);
    		System.out.print(false);
    	}
    	else if( selection == null && sortOrder.equals("local")) {
    		c= db.rawQuery("select * from "+myHelper.TABLE_NAME, null);
    		return c;
    	}
    	else {
    		c= db.rawQuery("select * from "+myHelper.TABLE_NAME, null);
    		if (c.moveToFirst()) {
        	    while (!c.isAfterLast()) {
        	    	int keyIndex = c.getColumnIndex("key");
        	        int valueIndex = c.getColumnIndex("value");
        	    	String returnKey = c.getString(keyIndex);
        	        String returnValue = c.getString(valueIndex);
        	        mc.newRow().add(returnKey).add(returnValue);
        	        c.moveToNext();
        	    	}
        	    }
    		query_flag = true;
    		Message chk = new Message("chk","check_msg");
    		pool.execute(new Send(chk, getPortAddr(suc)));
    		try {
				if(bq2.take().equals("false")) {
					bq2.clear();
					Message q = new Message("query", selection, sortOrder);
    				pool.execute(new Send(q, getPortAddr(suc)));				//send query
					HashMap<String, String> hm = bq_map.take();					//wait for map to arrive
					bq_map.clear();
					Set<String> set = hm.keySet();
					Iterator<String> itr = set.iterator();						//parse from map to matrixcursor
					while(itr.hasNext()) {
						String k = (String) itr.next();
						String v = hm.get(k);
						mc.newRow().add(k).add(v);
					}
					query_flag = false;
				}
				if (!dump_flag) {
					if(mc.moveToFirst()) {
						HashMap<String,String> hm = new HashMap<String,String>();	//matrix cursor to map
						while(!mc.isAfterLast()) {
							int keyIndex = mc.getColumnIndex("key");
							int valueIndex = mc.getColumnIndex("value");
							String returnKey = mc.getString(keyIndex);
							String returnValue = mc.getString(valueIndex);
							hm.put(returnKey, returnValue);
							mc.moveToNext();
						}
						Message d = new Message("dump", hm);								//send map to predec
						pool.execute(new Send(d, getPortAddr(predec)));
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
		return mc;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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

	Receiver (Message s) {
		this.obj= s;
	}

	public void run() {
		Log.i("adil rcvr", "recvd msg: "+ obj.Node_id + obj.id);
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
			SimpleDhtProvider.ins_flag = true;
			SimpleDhtMainActivity.mContentResolver.insert(SimpleDhtProvider.CONTENT_URI, val);
		}
		else if(obj.id.equals("ins_reply")) {
			try {
				SimpleDhtProvider.bq.put(obj.rowId);
			} catch (InterruptedException e) {
				Log.e(Listener.TAG, e.getMessage());
			}
		}
		else if(obj.id.equals("query")) {
			SimpleDhtProvider.query_flag = true;
			SimpleDhtMainActivity.mContentResolver.query(SimpleDhtProvider.CONTENT_URI, null, obj.selection, null, obj.sortOrder);
		}
		else if(obj.id.equals("q_reply")) {
			try {
				SimpleDhtProvider.bq2.put(obj.Node_id);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else if(obj.id.equals("chk")) {
			SimpleDhtProvider.pool.execute(new Send(new Message("chk_reply",Boolean.toString(SimpleDhtProvider.query_flag)), SimpleDhtProvider.getPortAddr(SimpleDhtProvider.predec)));
		}
		else if(obj.id.equals("chk_reply")) {
			try {
				SimpleDhtProvider.bq2.put(obj.Node_id);
			} catch (InterruptedException e) {
				Log.e(Listener.TAG, e.getMessage());
			}
		}
		else if(obj.id.equals("dump")) {
			try {
				SimpleDhtProvider.bq_map.put(obj.map);
			} catch (InterruptedException e) {
				Log.e(Listener.TAG, e.getMessage());
			}
		}
	}
}

class Listener implements Runnable {

	static final String TAG = "adil listen";
	static final int recvPort= 10000;
	ExecutorService e= Executors.newFixedThreadPool(2);

	public void run() {
		Socket sock1= null;
		ObjectInputStream in =null;
		ServerSocket servSocket= null;
		try {
			servSocket= new ServerSocket(recvPort);
			Log.v(TAG, "Server Socket port: "+Integer.toString(servSocket.getLocalPort()));
		} catch (IOException e) {
			Log.e(TAG, ""+e.getMessage());
		}

		while(true) {
			try {
				sock1= servSocket.accept();
				in =new ObjectInputStream(sock1.getInputStream());
				Message obj;
				try {
					obj = (Message) in.readObject();
					e.execute(new Receiver(obj)); //replace where to send this object
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