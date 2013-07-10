/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core.customservers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.wso2.carbon.automation.core.utils.httpserverutils.SimpleHttpClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SimpleWebServer extends Thread {
    private volatile boolean running = true;
    private int port;
    protected Log log = LogFactory.getLog(SimpleWebServer.class);

    public SimpleWebServer(int listenPort) {
        port = listenPort;
    }

    public void run() {
        ServerSocket serversocket = null;
        try {
            log.info("Trying to bind to localhost on port " + Integer.toString(port) + "...");
            //make a ServerSocket and bind it to given port,
            serversocket = new ServerSocket(port);
        } catch (Exception e) {
            log.info("\nFatal Error:" + e.getMessage());
            running = false;
            return;
        }
        log.info("Running Simple WebServer!\n");
        //go in a infinite loop, wait for connections, process request, send response
        while (running) {
            log.info("\nReady, Waiting for requests...\n");
            try {
                //this call waits/blocks until someone connects to the port we
                //are listening to
                Socket connectionSocket = serversocket.accept();
                InetAddress client = connectionSocket.getInetAddress();
                log.info(client.getHostName() + " connected to server.\n");

                BufferedReader input =
                        new BufferedReader(new InputStreamReader(connectionSocket.
                                getInputStream()));
                //Prepare a outputStream from us to the client,
                //this will be used sending back our response
                //(header + requested file) to the client.
                DataOutputStream output =
                        new DataOutputStream(connectionSocket.getOutputStream());
                httpHandler(input, output);
            } catch (Exception e) { //catch any errors, and print them
                log.info("\nError:" + e.getMessage());
                running = false;
            }
        }
    }

    private void httpHandler(BufferedReader input, DataOutputStream output) {
        int method = 0; //1 get, 2 head, 0 not supported
        String path = ""; //the various things, what http v, what path,
        String contentType = null;
        String fileName = null;
        String tmp = null;
        try {
            //This is the two types of request we can handle
            //GET /index.html HTTP/1.0
            //HEAD /index.html HTTP/1.0
            tmp = input.readLine(); //read from the stream
            contentType = input.readLine();

            if (!tmp.toUpperCase().startsWith("GET") || tmp.toUpperCase().startsWith("HEAD")) { // not supported
                try {
                    output.writeBytes(constructHttpHeader(501, contentType));
                    output.close();
                    return;
                } catch (Exception e3) { //if some error happened catch it
                    log.info("error:" + e3.getMessage());
                } //and display error
            }

            log.info(tmp);
            String pathOfContext = tmp.substring(tmp.indexOf("/"), tmp.length());
            fileName = pathOfContext.substring(pathOfContext.indexOf("/") + 1, pathOfContext.indexOf(" "));
        } catch (Exception e) {
            log.info("error" + e.getMessage());
        } //catch any exception

        File fileSource = new File("/home/krishantha/svn/trunk/carbon/platform/branches/4.1.0/products/esb" +
                                   "/4.7.0/modules/integration/tests/src/test/resources/artifacts/ESB/other/" +
                                   fileName);

        FileInputStream requestedFile = null;

        try {
            //NOTE that there are several security consideration when passing
            //the untrusted string "path" to FileInputStream.
            //You can access all files the current user has read access to!!!
            //current user is the user running the javaprogram.
            //you can do this by passing "../" in the url or specify absoulute path
            //or change drive (win)

            //try to open the file,
            requestedFile = new FileInputStream(fileSource);
            if (!fileSource.exists()) {
                output.writeBytes(constructHttpHeader(404, contentType));
                //close the stream
                output.close();
            }
        }  //print error to gui
        catch (IOException e1) {
            log.error(e1);
        }

        try {
            //write out the header, 200 ->everything is ok we are all happy.
            output.writeBytes(constructHttpHeader(200, contentType));

            if (tmp.toUpperCase().startsWith("GET")) { //1 is GET 2 is head and skips the body
                byte[] buffer = new byte[1024];
                while (true) {
                    //read the file from filestream, and print out through the
                    //client-outputstream on a byte per byte base.
                    int b = requestedFile.read(buffer, 0, 1024);
                    if (b == -1) {
                        break; //end of file
                    }
                    output.write(buffer, 0, b);
                }
                //clean up the files, close open handles

            }
            output.close();
            requestedFile.close();
        } catch (Exception ignored) {
        }
    }

    //this method makes the HTTP header for the response
    //the headers job is to tell the browser the result of the request
    //among if it was successful or not.
    private String constructHttpHeader(int returnCode, String contentType) {
        String header = "HTTP/1.0 ";
        switch (returnCode) {
            case 200:
                header = header + "200 OK";
                break;
            case 400:
                header = header + "400 Bad Request";
                break;
            case 403:
                header = header + "403 Forbidden";
                break;
            case 404:
                header = header + "404 Not Found";
                break;
            case 500:
                header = header + "500 Internal Server Error";
                break;
            case 501:
                header = header + "501 Not Implemented";
                break;
            case 503:
                header = header + "503 Error";
                break;
        }

        header = header + "\r\n"; //other header fields,
        header = header + "Connection: close\r\n"; //can't handle persistent connections
        header = header + "Server: SimpleWebServer\r\n"; //server name
        header = header + contentType + "\r\n";
        header = header + "\r\n";
        return header;
    }

    public void terminate() {
        running = false;
    }
}