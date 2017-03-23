package util;

public class SetCookies {
	private static String isLogined = "";
	
	public static void setLoginCookie(String s) {
		isLogined = s;
	}
	
	public static String getLoginCookie() {
		return isLogined;
	}
}
