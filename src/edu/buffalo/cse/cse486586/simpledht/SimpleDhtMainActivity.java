package edu.buffalo.cse.cse486586.simpledht;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class SimpleDhtMainActivity extends Activity {
	
	static String avd_name;
	static String Node_id;
	static int avd_number;
	String TAG= "adil";
	static int avd_port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dht_main);
        
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.button3).setOnClickListener(
                new OnTestClickListener(tv, getContentResolver()));
        
        new Thread(new Runnable() {
			public void run() {
		    	String portStr = get_portStr();
		    	Node_id = portStr;
		    	Log.v(TAG, portStr);

				if(portStr.equals("5554")) {
					avd_name= "avd0";
					avd_number= 0;
					avd_port= 11108;					
				}
				else if(portStr.equals("5556")) {
					avd_name= "avd1";
					avd_number=1;
					avd_port= 11112;
				}
				else if(portStr.equals("5558")) {
					avd_name= "avd2";
					avd_number= 2;
					avd_port= 11116;
				}
				else
					Log.d(TAG, "AVD portStr is neither 5554 nor 5556");
			}        	
        }).start();
          
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

}
