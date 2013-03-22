package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import android.util.Log;

public class Send implements Runnable {
	Socket sock= null;
	Message o;
	int port;
	static final String TAG="adil send";
	
	Send(Message m, int port) {
		this.o= m;
		this.port = port;
	}
	
	public void run() {
		try {
			this.sock= new Socket("10.0.2.2",port);
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(o);
			out.flush();
			out.close();
			sock.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage().toString());
		}
	}
}