package edu.buffalo.cse.cse486586.simpledht;

import java.io.*;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	String id;
	String Node_id =SimpleDhtMainActivity.Node_id;
	String suc;
	String predec;
	
	// for 'join' message only
	Message(String id) {
		this.id= id;
	}
	
	//for successor/predecessor 'update'
	Message(String id, String suc, String predec) {
		this.id = id;
		this.suc= suc;
		this.predec= predec;
	}

}
