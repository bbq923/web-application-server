package util;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class SplitterTest {

	@Test
	public void path() {
		String requestLine = "GET /index.html HTTP/1.1";
		String result = Splitter.getPath(requestLine);
		assertEquals("/index.html", result);
	}

	@Test
	public void queryString() { // signup 시 query string이 제대로 parsing 되는지 test
		String requestLine = "/user/create?userId=bbq923&password=kdo12341&name=kimdaehoon&email=bbq9234%40naver.com";
		String result = Splitter.getQueryString(requestLine);
		assertEquals("userId=bbq923&password=kdo12341&name=kimdaehoon&email=bbq9234%40naver.com", result);
	}
	
	@Test
	public void queryStringParsing() { // query string이 제대로 parsing 되는지 test
		String requestLine = "/user/create?userId=bbq923&password=kdo12341&name=kimdaehoon&email=bbq9234%40naver.com";
		Map<String, String> result = HttpRequestUtils.parseQueryString(Splitter.getQueryString(requestLine));
		assertEquals("bbq923", result.get("userId"));
		assertEquals("kdo12341", result.get("password"));
		assertEquals("kimdaehoon", result.get("name"));
		assertEquals("bbq9234%40naver.com", result.get("email"));
	}
}
