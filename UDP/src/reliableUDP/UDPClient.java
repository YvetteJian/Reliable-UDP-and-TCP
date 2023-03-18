package reliableUDP;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;



public class UDPClient {

    public static void main(String[] args) throws IOException {

        ArrayList<Integer> sequence = new ArrayList<>();

        //create a socket object on port 9999
        DatagramSocket socket = new DatagramSocket(9999);


        //send handshake
        byte[] data = "handshake".getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 8888);
        socket.send(packet);

        //if receive handshake received, that means successfully connected
        byte[] buf = new byte[1024];
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        int length = packet.getLength();
        data = packet.getData();
        String s = new String(data, 0, length);
        if (s.equals("handshake received")){
            System.out.println(s);
        }else{
            System.out.println("handshake fail,socket closing");
            socket.close();
        }

        //send command
        String command;

        //if no command, close socket

        if (args.length == 0){
            System.out.println("invalid command");
            socket.close();
            return;
        }

        //if it begins with "get", just send the file name to server.
        if (args.length == 2 && args[0].equals("get")){
            command = args[1];
            data = command.getBytes();
            packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 8888);
            socket.send(packet);
        }else if(args.length==1 && args[0].equals("index")){
            command = "index";
            data = command.getBytes();
            packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 8888);
            socket.send(packet);
        }else {
            System.out.println("invalid input");
            socket.close();
            return;
        }

        //receive data from server
        while (true){
            byte[] buff = new byte[1024];
            packet = new DatagramPacket(buff, buff.length);
            socket.receive(packet);
            int length1 = packet.getLength();
            data = packet.getData();
            String server_data = new String(data, 0, length1);
            String[]  data_array = server_data.split(":");

            //if it is a flag of end or error, close socked.

            if (data_array.length ==2 && data_array[1].equals("end")){
                System.out.println("finish,closing socket");
                socket.close();
                break;
            }
            if (data_array.length ==2 && data_array[1].equals("error")){
                System.out.println(data_array[1]);
                System.out.println("can not find file, closing socket");
                socket.close();
                break;
            }

            //right sequence,send success ACK, print content
            if (Integer.parseInt(data_array[0]) == sequence.size() +1 ){
                String ACK = (Integer.parseInt(data_array[0])+1) +":"+"success";
                byte[] send_ack = ACK.getBytes();
                packet = new DatagramPacket(send_ack, send_ack.length, InetAddress.getLocalHost(), 8888);
                socket.send(packet);
                sequence.add(Integer.parseInt(data_array[0]));
                String show = server_data.replaceFirst((data_array[0]+":"), "");
                System.out.println(show);
            }
            //if it is not in right sequence, send fail ack.
            else{
                String ACK = data_array[0] +":"+"fail";
                byte[] send_ack = ACK.getBytes();
                packet = new DatagramPacket(send_ack, send_ack.length, InetAddress.getLocalHost(), 8888);
                socket.send(packet);
                System.out.println("so bad not in correct sequence, server will resend data");
            }

        }



    }


}
