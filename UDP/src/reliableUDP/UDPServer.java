package reliableUDP;

import java.io.*;
import java.net.*;
import java.util.HashMap;


public class UDPServer {
    public static int sequence_number = 1;
    public static HashMap<Integer, DatagramPacket> buffer = new HashMap<Integer, DatagramPacket>();
    public static void main(String[] args) throws IOException{

        //see if this path is valid .
        File directory = new File(args[0]);
        if (!directory.exists()) {
            System.out.println("This directory not exist.");
            return;
        } else if (!directory.isDirectory()) {
            System.out.println("This is not a directory!");
            return;
        }

        //create a socket object on port 8888

        DatagramSocket socket = new DatagramSocket(8888);

        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try{
            socket.receive(packet);
            int length = packet.getLength();
            byte[] data = packet.getData();
            String s = new String(data, 0, length);
            System.out.println(s);
            if (s.equals("handshake")){
                data = "handshake received".getBytes();
                packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 9999);
                socket.send(packet);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        //receive command
        byte[] buff = new byte[1024];
        DatagramPacket packet2 = new DatagramPacket(buff, buff.length);
        socket.receive(packet2);
        int length = packet2.getLength();
        byte[] data2 = packet2.getData();
        String s = new String(data2, 0, length);
        System.out.println(s);

        String[] msg;

        if (s.equals("index")){
            msg = directory.list();
            for (int i = 0; i < msg.length; i++){
                String message = sequence_number + ":" + msg[i];
                System.out.println(message);
                byte[] send_string = message.getBytes();
                DatagramPacket send_data = new DatagramPacket(send_string, send_string.length, InetAddress.getLocalHost(), 9999);
                socket.send(send_data);
                //store in hashmap, if sending fails, get the packet form it and resend it.
                buffer.put(sequence_number,send_data);
                sequence_number++;

                //receive ACK
                byte[] ACK = new byte[1024];
                DatagramPacket packet_ack = new DatagramPacket(ACK, ACK.length);
                socket.receive(packet_ack);
                int length_ack = packet_ack.getLength();

                byte[] data_ack =packet_ack.getData();
                String ack = new String(data_ack , 0, length_ack);
                String[]  ack_info = ack.split(":");
                System.out.println("data received, next one is "+ack_info [0]);

                //if send fails(not in right sequence),resend the file.
                if (ack_info[1].equals("fail")){
                    DatagramPacket resend_data = buffer.get(sequence_number-1);
                    socket.send(resend_data);
                }
            }

            // finish sending all items in directory
            String end=sequence_number+":"+"end";
            byte[] send_end = end.getBytes();
            DatagramPacket end_send = new DatagramPacket(send_end,send_end.length,InetAddress.getLocalHost(), 9999);
            socket.send(end_send);
            System.out.println("finish sending");
            socket.close();

        }
        else {
            //find if the file exists in directory
            File f = new File(directory,s);
            if (f.exists()) {
                byte[] ok_message = (sequence_number+":"+"OK").getBytes();
                DatagramPacket send_data = new DatagramPacket(ok_message, ok_message.length, InetAddress.getLocalHost(), 9999);
                socket.send(send_data);
                buffer.put(sequence_number,send_data);
                sequence_number++;

                byte[] ACK = new byte[1024];
                DatagramPacket packet_ack = new DatagramPacket(ACK, ACK.length);
                socket.receive(packet_ack);
                int length_ack = packet_ack.getLength();

                byte[] data_ack =packet_ack.getData();
                String ack = new String(data_ack , 0, length_ack);
                String[]  ack_info = ack.split(":");
                System.out.println("data received, next one is "+ack_info [0]);

                //if send fails(not in right sequence),resend the message.
                if (ack_info[1].equals("fail")){
                    DatagramPacket resend_data = buffer.get(sequence_number-1);
                    socket.send(resend_data);
                }

                //read and send file content

                BufferedReader fileIn = new BufferedReader(new FileReader(f));
                while (true){
                    //break down into 1 line for each packet.
                    String content= fileIn.readLine();

                    if (content==null){
                        String end=sequence_number+":"+"end";
                        byte[] send_end = end.getBytes();
                        DatagramPacket end_send = new DatagramPacket(send_end,send_end.length,InetAddress.getLocalHost(), 9999);
                        socket.send(end_send);
                        System.out.println("finish sending");
                        socket.close();
                        break;
                    }

                    System.out.println(sequence_number+":"+content);

                    //add sequence_number to content and then send.

                    byte[] content_send_byte = (sequence_number+":"+content).getBytes();
                    DatagramPacket send_content_byte = new DatagramPacket(content_send_byte, content_send_byte.length, InetAddress.getLocalHost(), 9999);
                    socket.send(send_content_byte);
                    buffer.put(sequence_number,send_content_byte);
                    sequence_number++;

                    //receive ACK

                    ACK = new byte[1024];
                    packet_ack = new DatagramPacket(ACK, ACK.length);
                    socket.receive(packet_ack);
                    length_ack = packet_ack.getLength();

                    data_ack =packet_ack.getData();
                    ack = new String(data_ack , 0, length_ack);
                    ack_info = ack.split(":");
                    System.out.println("data received, next one is "+ack_info [0]);

                    //if send fails(not in right sequence),resend the message.
                    if (ack_info[1].equals("fail")){
                        DatagramPacket resend_data = buffer.get(sequence_number-1);
                        socket.send(resend_data);
                    }
                }
            }
            else{
             String error = sequence_number+":"+ "error";
             byte[]error_byte = error.getBytes();
             DatagramPacket send_error_byte = new DatagramPacket(error_byte , error_byte .length, InetAddress.getLocalHost(), 9999);
             socket.send(send_error_byte);
             System.out.println("can not find the file, close socket");
             socket.close();
            }
        }







        }




    }

