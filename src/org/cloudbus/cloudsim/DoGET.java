package org.cloudbus.cloudsim;

import java.net.*;
import java.io.*;

public class DoGET {

    public static void main(String[] args) {
	String baseurl = "http://127.0.0.1:8080/EventChannel/";
	String content = "";

	if(args.length != 1) {
	    System.out.println("Usage: java DoGET <key>");
	    System.exit(0);
	}

	try {

	baseurl += args[0];
	URL url = new URL(baseurl);
	HttpURLConnection request = (HttpURLConnection)url.openConnection();

	request.setUseCaches(false);
	request.setDoOutput(true);
	request.setDoInput(true);

	request.setFollowRedirects(false);
	request.setInstanceFollowRedirects(false);

	request.setRequestMethod("GET");

	int retcode = request.getResponseCode();
	System.out.println("Ret Code: " + retcode);
	System.out.println("Content Type: " + 
			   request.getHeaderField("Content-Type"));
	if(retcode == 200) {
	    BufferedReader in = 
		new BufferedReader(new InputStreamReader(request.getInputStream()));
	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
		content += inputLine;
	    }
	    System.out.println("Content: " + content);
	    in.close();
	}
	} catch (IOException e) {
	e.printStackTrace();
	}
    }
}

