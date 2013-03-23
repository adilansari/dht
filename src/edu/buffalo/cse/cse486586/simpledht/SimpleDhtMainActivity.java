package edu.buffalo.cse.cse486586.simpledht;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class SimpleDhtMainActivity extends Activity {
	
	static String Node_id;
	String TAG= "adil";
	static int avd_port;
	private ContentResolver mContentResolver;
	private Handler uiHandle= new Handler();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dht_main);
        
        mContentResolver = getContentResolver();
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.button3).setOnClickListener(
                new OnTestClickListener(tv, getContentResolver()));
        
        		
		    	String portStr = get_portStr();
		    	Node_id = portStr;
		    	Message j = new Message("join", Node_id);
		    	SimpleDhtProvider.pool.execute(new Send(j,11108));
          
    }
    
    public void LDump(View view) {
 //   	new Thread (new Runnable(){
 //   		public void run() {
    			Cursor resultCursor = mContentResolver.query(SimpleDhtProvider.CONTENT_URI, null, null, null, null);
    	    	if (resultCursor.moveToFirst()) {
    	    		while (!resultCursor.isAfterLast()) {

    	    			int keyIndex = resultCursor.getColumnIndex("key");
    	                int valueIndex = resultCursor.getColumnIndex("value");
    	    			String returnKey = resultCursor.getString(keyIndex);
    	                String returnValue = resultCursor.getString(valueIndex);
    	                updateTextView(returnKey+" "+returnValue);
    	    			resultCursor.moveToNext();
    	    		}
    	    	}
//    		}
//    	}).start();
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
        return true;
    }
    
    public String get_portStr() {
    	TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    	String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
    	return portStr;
    }
    
    public void updateTextView(String message) {
    	final String msg= message;
    	uiHandle.post(new Runnable() {
    		public void run() {
    			TextView textView = (TextView)findViewById(R.id.textView1);
    			textView.setMovementMethod(new ScrollingMovementMethod());
    	    	//Log.v(TAG, "updating textview");
    	    	textView.append(msg+"\n");
    	    	//Log.v(TAG, "updated textview");
       		}
    	});
    }

}
