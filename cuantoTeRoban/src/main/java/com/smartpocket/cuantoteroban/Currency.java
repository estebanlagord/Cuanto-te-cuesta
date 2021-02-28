package com.smartpocket.cuantoteroban;

import static com.smartpocket.cuantoteroban.Utilities.removeAccentsAndMakeLowercase;

public class Currency implements Comparable<Currency>{
	public static final String CODE = "code";
	private final String code;
	private final String name;
	private final String country;
	private String searchTerms = "";
	private int flagIdentifier;
	
	public Currency(String code, String name, String country) {
		super();
		
		if (code == null || code.length() != 3)
			throw new IllegalArgumentException("Currency code must have 3 characters");
		
		this.code = code;
		this.name = name;
		this.country = country;
	}
	
	public Currency(String code, String name, String country, int flagIdentifier) {
		this(code, name, country);
		this.flagIdentifier = flagIdentifier;
	}
	
	public Currency(String code, String name, String country, String searchTerms) {
		this(code, name, country);
		this.searchTerms = searchTerms;
	}
	
	public Currency(String code, String name, String country, String searchTerms, int flagIdentifier) {
		this(code, name, country, searchTerms);
		this.flagIdentifier = flagIdentifier;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getCountry() {
		return country;
	}
	
	public String getSearchTerms() {
		return searchTerms;
	}
	
	public int getFlagIdentifier() {
		//return MainActivity.getInstance().getResources().getIdentifier(code.toLowerCase(Locale.US), "drawable", MainActivity.class.getPackage().getName());
		return flagIdentifier;
	}
	
	public boolean matchesQuery(final String query) {
		if (query == null)
			return true;
		
		String theQuery = removeAccentsAndMakeLowercase(query);
		
		// remove the S at the end
		if (theQuery.length() > 2 && theQuery.endsWith("s"))
			theQuery = theQuery.substring(0, theQuery.length() - 1);
		
		String theName        = removeAccentsAndMakeLowercase(getName());
		String theCountry     = removeAccentsAndMakeLowercase(getCountry());
		String theCode        = removeAccentsAndMakeLowercase(getCode());
		String theSearchTerms = removeAccentsAndMakeLowercase(getSearchTerms());
		
		return     theName.contains(theQuery)
				|| theCountry.contains(theQuery)
				|| theCode.contains(theQuery)
				|| theSearchTerms.contains(theQuery);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Currency other = (Currency) obj;
		if (code == null) {
            return other.code == null;
		} else return code.equals(other.code);
    }

	@Override
	public String toString() {
		return "Currency [code=" + code + ", name=" + name + ", country=" + country + "]";
	}

	@Override
	public int compareTo(Currency another) {
		return this.name.compareTo(another.name);
	}

}
