package com.watchtogether.dbaccess;

public enum SexEnum {
	MALE,
	FEMALE,
	NONE;
	
	public static SexEnum getSexBucket(String sex) {
		if (sex.equalsIgnoreCase("male"))
			return SexEnum.MALE;
		else if (sex.equalsIgnoreCase("female"))
			return SexEnum.FEMALE;
		else return SexEnum.NONE;
	}
}
