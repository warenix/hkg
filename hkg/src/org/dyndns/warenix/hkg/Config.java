package org.dyndns.warenix.hkg;

public class Config {

	public static final String DOMAIN = "http://m3.hkgolden.com/";

	private static final int MAX_ROBBIN = 3;
	private static int sRoundRobbinCounter = 0;

	/**
	 * get random m{1,2,3} servers
	 * 
	 * @return
	 */
	public static String getRandomDomain() {
		if (sRoundRobbinCounter >= MAX_ROBBIN) {
			sRoundRobbinCounter = 0;
		}
		sRoundRobbinCounter++;
		String domain = String.format("http://m%d.hkgolden.com/",
				sRoundRobbinCounter);
		System.out.println(domain);
		return domain;
	}
}
