package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import android.util.Log;

class Multicast implements Runnable {
	Socket sock=null;
	Message o;
	int[] ports= {11108, 11112, 11116};
	static final String TAG= "adil multicast";
	
	Multicast(Message m) {
		this.o=m;
	}

	public void run() {
		for(int s : ports) {
			//if(s != SimpleDhtMainActivity.avd_port) {
				try {
					this.sock= new Socket("10.0.2.2",s);
					ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
					out.writeObject(o);
					out.flush();
					out.close();
					sock.close();
				} catch (IOException e) {
					Log.e(TAG, "Multicast fail "+e.getMessage());
				}
			//}
		}
	}
}

class send implements Runnable {
	Socket sock= null;
	Message o;
	int port;
	static final String TAG="adil send";
	
	send(Message m, int port) {
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
			Log.e(TAG, "Send fail "+e.getMessage());
		}
	}
}