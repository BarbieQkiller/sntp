package main.java;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws IOException {
        // byte[] buf = {36, 1, 0, -25,  0,  0,  0, 0, 
        //                0, 0, 0,   2, 80, 80, 83, 0, 
        //              -30, 67, -18, 83, 0, 0, 0, 0, 
        //              -30, 67, -18, 83, -10, 69, -96, 0, 
        //              -30, 67, -18, 84, 42, -83, 46, -63, 
        //              -30, 67, -18, 84, 42, -83, 55, 41};
        // gbg1.ntp.se  
        
        String[] servers = {"gbg1.ntp.se", "gbg2.ntp.se", "mmo1.ntp.se", "mmo2.ntp.se", "sth1.ntp.se", "sth2.ntp.se", "svl1.ntp.se", "svl2.ntp.se"};
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(10000);
        int serverNo = 0;
        while(true) {
            System.out.println("Trying: " + servers[serverNo]);
            InetAddress address = InetAddress.getByName(servers[serverNo]);
            byte[] buf = new NtpMessage().toByteArray();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
            try {
                socket.send(packet);
                System.out.println("Request sent");
                socket.receive(packet);
                NtpMessage msg = new NtpMessage(packet.getData());
                System.out.println("Got reply");
                System.out.println(msg.toString());
            }
            catch (SocketTimeoutException e) {
                serverNo++;
                if(serverNo == servers.length) {
                    break;
                }
                continue;
            }
            break;
        }
        System.out.println("Done");
        socket.close();


    }
}