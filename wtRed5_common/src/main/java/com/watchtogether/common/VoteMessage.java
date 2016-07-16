package com.watchtogether.common;

import java.io.Serializable;

public class VoteMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Boolean add;
	private final Boolean abstain;

	public VoteMessage(boolean b, boolean abstain) {
		this.add = b;
		this.abstain = abstain;
	}

	public Boolean isAdd() {
		return add;
	}

	public Boolean isAbstain() {
		return abstain;
	}
	
}
