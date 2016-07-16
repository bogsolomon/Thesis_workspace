package com.watchtogether.dbaccess;

public enum AgeEnum {

	UNDER_20,
	UNDER_25,
	UNDER_30,
	UNDER_35,
	UNDER_40,
	UNDER_45,
	UNDER_50,
	UNDER_55,
	UNDER_60,
	UNDER_65,
	UNDER_70,
	UNDER_75,
	UNDER_80,
	UNDER_85,
	OVER_85,
	UNKNOWN;
	
	public static AgeEnum getAgeBucket(int age) {
		if (age == 0) return AgeEnum.UNKNOWN;
		else if (age <= 20) return AgeEnum.UNDER_20;
		else if (age <= 25)return AgeEnum.UNDER_25;
		else if (age <= 30)return AgeEnum.UNDER_30;
		else if (age <= 35)return AgeEnum.UNDER_35;
		else if (age <= 40)return AgeEnum.UNDER_40;
		else if (age <= 45)return AgeEnum.UNDER_45;
		else if (age <= 50)return AgeEnum.UNDER_50;
		else if (age <= 55)return AgeEnum.UNDER_55;
		else if (age <= 60)return AgeEnum.UNDER_60;
		else if (age <= 65)return AgeEnum.UNDER_65;
		else if (age <= 70)return AgeEnum.UNDER_70;
		else if (age <= 75)return AgeEnum.UNDER_75;
		else if (age <= 80)return AgeEnum.UNDER_80;
		else if (age <= 85)return AgeEnum.UNDER_85;
		else return AgeEnum.OVER_85;
	}
}
