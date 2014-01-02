package org.dyndns.warenix.hkg;

public class Config {

	public static final String DOMAIN = "http://m3.hkgolden.com/";

	private static final int MAX_ROBBIN = 3;
	private static int sRoundRobbinCounter = 0;

	static String[] domains = new String[MAX_ROBBIN];

	static {
		for (int i = 0; i < MAX_ROBBIN; ++i) {
			domains[i] = String.format("http://m%d.hkgolden.com/", i);
		}
	}

	/**
	 * get random m{1,2,3} servers
	 * 
	 * @return
	 */
	public static String getRandomDomain() {
		sRoundRobbinCounter++;
		if (sRoundRobbinCounter == MAX_ROBBIN) {
			sRoundRobbinCounter = 0;
		}
		return domains[sRoundRobbinCounter];
	}
}
