package org.jongo.util;


import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Picks up a port number from the ephemeral port range.
 * http://en.wikipedia.org/wiki/Ephemeral_port
 * @author Alexandre Dutra
 * 
 */
public class RandomPortNumberGenerator {

	private static final int MAX_PORT_NUMBER = 61000;
	private static final int MIN_PORT_NUMBER = 49152;
	
	/**
	 * Pick a random available port number in the ephemeral port range,
	 * i.e. between {@value #MIN_PORT_NUMBER} inclusively and
	 * {@value #MAX_PORT_NUMBER} exclusively.
	 * @return a random available port number in the ephemeral port range
	 */
	public static int pickAvailableRandomEphemeralPortNumber() {
		return pickAvailableRandomPortNumber(MIN_PORT_NUMBER, MAX_PORT_NUMBER);
	}
	

	/**
	 * Pick a random available port number between 
	 * {@code min} inclusively and {@code max} exclusively.
	 * @return a random available port number between {@code min} inclusively and
	 * {@code max} exclusively
	 */
	public static int pickAvailableRandomPortNumber(int min, int max) {
		while(true){
			int port = pickRandomPortNumber(min, max);
			if(isPortAvailable(port)){
				return port;
			}
		}
	}

	/**
	 * Pick a random port number between {@code min} inclusively and
	 * {@code max} exclusively.
	 * @return a random port number between {@code min} inclusively and
	 * {@code max} exclusively
	 */
	public static int pickRandomPortNumber(int min, int max) {
		return((int)(Math.random() * (max - min))) + min;
	}
	
	/**
	 * Test whether a port number is available (i.e. not bound).
	 * 
	 * @return a random port number between {@code min} inclusively and
	 * {@code max} exclusively
	 */
	public static boolean isPortAvailable(int port) {
	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	    } catch (IOException e) {
	    	return false;
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }
	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	            }
	        }
	    }
        try {
			new Socket("localhost", port);
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return true;
		}
        
	    return false;
	}

}
