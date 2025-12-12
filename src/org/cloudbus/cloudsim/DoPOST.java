package org.cloudbus.cloudsim;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DoPOST {

    public static void main(String[] args) {
	String baseurl = "http://127.0.0.1:8080/EventChannel/";
	String content = "";

	if(args.length != 3) {
	    System.out.println("Usage: java DoPOST <key> <content type> <value>");
	    System.exit(0);
	}

	try {

	baseurl += args[0]; //.../EventChannel/<key>
	URL url = new URL(baseurl);
	HttpURLConnection request = (HttpURLConnection)url.openConnection();

	request.setUseCaches(false);
	request.setDoOutput(true);
	request.setDoInput(true);

	request.setFollowRedirects(false);
	request.setInstanceFollowRedirects(false);

	request.setRequestProperty("Content-Type", args[1]);
	request.setRequestProperty("Content-Length", 
				   String.valueOf(args[2].length()));

	request.setRequestMethod("POST");
	OutputStreamWriter post = 
	    new OutputStreamWriter(request.getOutputStream());
	post.write(args[2]);
	post.flush();

	int retcode = request.getResponseCode();
	System.out.println("Ret Code: " + retcode);

	if(retcode == 200) {
	    BufferedReader in = 
		new BufferedReader(new InputStreamReader(request.getInputStream()));
	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
		content += inputLine;
	    }
	    System.out.println("Content: " + content);
	    post.close();
	    in.close();
	}
	} catch (IOException e) {
	e.printStackTrace();
	}
    }
}

