package com.watchtogether.dbaccess;

import java.util.List;
import java.util.Arrays;

public enum LocationEnum {

	USA,
	CANADA,
	EUROPE,
	ASIA,
	AFRICA,
	OTHER;
	
	private static String[] europeanCountriesArray = new String[] {"ALBANIA", "GREECE", "RUSSIA",
			"ANDORRA", "HUNGARY", "ROMANIA", "AUSTRIA", "ICELAND", "SAN MARINO", 	
			"BELGIUM", "IRELAND", "SCOTLAND", "BOSNIA & HERZEGOVINA", "ITALY",
			"SERBIA", "BELARUS", "LATVIA", "SLOVAKIA", "BULGARIA", "LIECHTENSTEIN",
			"SLOVENIA", "CZECH REPUBLIC", "LUXEMBURG", "TURKEY", "CROATIA", "LITHUANIA",
			"SWEDEN", "CYPRUS", "MALTA", "SWITZERLAND", "DENMARK", "MONACO", "SPAIN",
			"ESTONIA", "MOLDOVA", "UKRAINE", "FINLAND",	"NETHERLANDS", "UNITED KINGDOM",
			"FRANCE", "NORWAY", "REPUBLIC OF MACEDONIA", "GERMANY", "POLAND",
			"MONTENEGRO"};
	
	private static String[] africanCountriesArray = new String[] {"ANGOLA", "ETHIOPIA",
		"NIGER", "ALGERIA", "GABON", "NIGERIA", "BENIN", "GAMBIA", "RWANDA", "BOTSWANA",
		"GHANA", "SENEGAL", "BURKINA FASO", "GUINEA", "SEYCHELLES", "BURUNDI", "KENYA",
		"SIERRA LEONE", "CAMEROON", "LESOTHO", "SOMALIA", "CAPE VERDE", "LIBERIA",
		"SOUTH AFRICA", "CENTRAL AFRICAN REPUBLIC", "LIBYA", "SUDAN", "CHAD", 
		"MADAGASCAR", "SWAZILAND", "CONGO", "MALAWI", "TANZANIA", "COMOROS", "MALI",
		"TOGO", "D'IVOIRE",	"MAURITANIA", "TUNISIA", "DJIBOUTI", 
		"MAURITIUS", "UGANDA", "EGYPT", "MOROCCO", "ZAMBIA", "EQUATORIAL GUINEA",
		"MOZAMBIQUE", "ZIMBABWE", "ERITREA", "NAMIBIA", "ST. HELENA"};
	
	private static String[] asianCountriesArray = new String[] {"AZERBAIJAN", "JAPAN",
		"QATAR", "ARMENIA", "JORDAN", "SAUDI ARABIA", "BAHRAIN", "KAZAKHSTAN", 
		"SINGAPORE", "BANGLADESH", "KUWAIT", "SOUTH KOREA", "BHUTAN", "KYRGYZSTAN",
		"SRI LANKA", "BRUNEI", "LAOS", "SYRIA", "BURMA", "LEBANON", "TAIWAN", 
		"CAMBODIA", "MALAYSIA", "TAJIKISTAN", "CHINA", "MALDIVES", "THAILAND", 
		"EAST TIMOR", "MONGOLIA", "TURKEY", "INDIA", "NEPAL", "TURKMENISTAN", 
		"INDONESIA", "NORTH KOREA", "UNITED ARAB EMIRATES", "IRAN", "OMAN", 
		"UZBEKISTAN", "IRAQ", "PAKISTAN", "VIETNAM", "ISRAEL", "PHILIPPINES", 
		"YEMEN"};
	
	private static List<String> europeanCountries = Arrays.asList(europeanCountriesArray);
	
	private static List<String> africanCountries = Arrays.asList(africanCountriesArray);
	
	private static List<String> asianCountries = Arrays.asList(asianCountriesArray);
	
	public static LocationEnum getCountryBucket(String country) {
		country = country.toUpperCase();
		if (country.equalsIgnoreCase("canada")) return LocationEnum.CANADA;
		else if (country.equalsIgnoreCase("usa") ||country.equalsIgnoreCase("u.s.a.")) return LocationEnum.USA;
		else if (europeanCountries.contains(country)) return LocationEnum.EUROPE;
		else if (africanCountries.contains(country)) return LocationEnum.AFRICA;
		else if (asianCountries.contains(country)) return LocationEnum.ASIA;
		else return LocationEnum.OTHER;
	}

	
}
