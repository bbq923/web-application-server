package util;

public class Splitter {
	
	
	public static String getFileExtension(String path) {
    	return path.substring(path.lastIndexOf(".") + 1);
    }
	
	public static String getPath(String requestLine) {
		return requestLine.split(" ")[1];
	}
	
	public static String getQueryString(String requestLine) {
		return requestLine.split("\\?")[1];
	}
}
