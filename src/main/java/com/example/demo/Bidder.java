package com.example.demo;


public class Bidder {
	
	private String name;
	private int numb;
	
	
	
	
	public Bidder() {
		super();
	}
	
	public Bidder(String name, int numb) {
		super();
		this.name = name;
		this.numb = numb;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNumb() {
		return numb;
	}
	public void setNumb(int numb) {
		this.numb = numb;
	}
	
	
	
//	public JSONObject toJSON() {
//
//        JSONObject jo = new JSONObject();
//        jo.put("integer", mSomeInt);
//        jo.put("string", mSomeString);
//
//        return jo;
//    }

}
