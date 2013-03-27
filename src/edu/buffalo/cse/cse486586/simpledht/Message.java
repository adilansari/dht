package edu.buffalo.cse.cse486586.simpledht;

import java.io.*;

import android.content.ContentValues;
import android.util.Log;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	String id, Node_id, selection, sortOrder, query_reply;
	String[] nbors;
	String cv[];
	long rowId;
	
	// for 'join' message or //q_reply by replacing Node_id
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
	
	//insert message
	Message(String id, String[] cv) {
		this.id= id;
		this.cv = cv;
		this.Node_id = SimpleDhtMainActivity.Node_id;
	}

	//insert reply
	Message (String id, long rowId) {
		this.id =id;
		this.rowId = rowId;
		this.Node_id = SimpleDhtMainActivity.Node_id;
	}
	
	//query
	Message (String id, String selection, String sortOrder){
		this.id = id;
		this.selection = selection;
		this.sortOrder = sortOrder;
		this.Node_id = SimpleDhtMainActivity.Node_id;
	}
	
}
