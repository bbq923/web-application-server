package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;
import util.SetCookies;
import util.Splitter;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        	
        	String requestLine = br.readLine();
        	log.debug("request line : {}", requestLine);
        	
        	if (requestLine == null) {
        		return;
        	}
        	
        	String path = Splitter.getPath(requestLine);
        	log.debug("path : {}", path);
        	
        	int contentLength = 0;
        	String line = br.readLine();
        	while (!line.equals("")) {
        		log.debug("header : {}", line);
        		line = br.readLine();
        		if (line.startsWith("Content-Length")) { // Content-Length 값을 구한다.
        			contentLength = Integer.parseInt(line.split(":")[1].trim());
        		}
        	}
        	
        	DataOutputStream dos = new DataOutputStream(out);
        	if (path.equals("/user/create")) { // make signUp response
        		String requestBody = IOUtils.readData(br, contentLength);
        		User newUser = makeUserByQueryString(requestBody);
        		log.debug("new user : {}", newUser);
        		DataBase.addUser(newUser);
        		response302Header(dos, "/index.html");
        	} else if (path.equals("/user/login")) { // make login response page
        		String requestBody = IOUtils.readData(br,  contentLength);
        		log.debug("login body: {}", requestBody);
        		Map<String, String> tryLogin = HttpRequestUtils.parseQueryString(requestBody);
        		if (DataBase.findUserById(tryLogin.get("userId")).getPassword().equals(tryLogin.get("password"))) { // TODO userId 가 없을 때 처리 필요
        			SetCookies.setLoginCookie("logined=true");
        			response302Header(dos, "/index.html");
        		} else {
        			SetCookies.setLoginCookie("logined=false");
        			response302Header(dos, "/user/login_failed.html");
        		}
			} else {
				log.debug("현재 쿠키 값 : {}", SetCookies.getLoginCookie());
				byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
				response200Header(dos, body.length, path);
				responseBody(dos, body);
			}
        	
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    
    
    private User makeUserByQueryString(String queryString) {
    	Map<String, String> qsParsed = HttpRequestUtils.parseQueryString(queryString);
    	return new User(qsParsed.get("userId"), qsParsed.get("password"), 
    			qsParsed.get("name"), qsParsed.get("email"));
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String path) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/" 
            		+ Splitter.getFileExtension(path) // TODO .js file to text/javascript
            		+ ";charset=utf-8\r\n");
            if (!SetCookies.getLoginCookie().equals("")) {
            	dos.writeBytes("Set-Cookie: " + SetCookies.getLoginCookie() + "\r\n");
            }
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, String location) {
    	try {
    		dos.writeBytes("HTTP/1.1 302 Found \r\n");
    		dos.writeBytes("Location: http://localhost:8080" + location + "\r\n");
    		dos.writeBytes("\r\n");
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
