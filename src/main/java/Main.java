package main.java;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

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
                double localTimestamp = (System.currentTimeMillis()/1000.0) + 2208988800.0;
                socket.receive(packet);
                NtpMessage msg = new NtpMessage(packet.getData());
                double roundTripDelay = (localTimestamp-msg.getOriginateTimestamp()) -
                    (msg.getTransmitTimestamp() - msg.getReceiveTimestamp());
                double localClockOffset = ((msg.getReceiveTimestamp() - msg.getOriginateTimestamp()) -
                    (msg.getTransmitTimestamp() - localTimestamp)) / 2;
                System.out.println("Got reply");
                System.out.println(msg.toString());
                System.out.println("Local timestamp: " + NtpMessage.timestampToString(localTimestamp));
                System.out.println("Round-trip delay: " + new DecimalFormat("0.00").format(roundTripDelay*1000) + "ms");
                System.out.println("Local clock offset: " + new DecimalFormat("0.00").format(localClockOffset*1000) + "ms");

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