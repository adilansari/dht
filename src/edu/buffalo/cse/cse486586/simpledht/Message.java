package edu.buffalo.cse.cse486586.simpledht;

import java.io.*;
import android.util.Log;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	String id;
	String Node_id;
	String[] nbors;
	
	// for 'join' message only
	Message(String id, String Node_id) {
		this.id= id;
		this.Node_id= Node_id;
		//Log.v("adil sender", Node_id);
	}
	
	//for successor/predecessor 'update'
	Message(String id,String Node_id, String[] nbors) {
		this.id = id;
		this.nbors = nbors;
		this.Node_id= Node_id;
	}

}
