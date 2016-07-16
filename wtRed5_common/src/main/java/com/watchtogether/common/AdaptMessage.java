package com.watchtogether.common;

import java.io.Serializable;

public class AdaptMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean add;
	private int count;
	
	public AdaptMessage() {
		super();
	}
	
	public AdaptMessage(boolean add, int count) {
		super();
		this.add = add;
		this.count = count;
	}

	public boolean isAdd() {
		return add;
	}
	public void setAdd(boolean add) {
		this.add = add;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
}
