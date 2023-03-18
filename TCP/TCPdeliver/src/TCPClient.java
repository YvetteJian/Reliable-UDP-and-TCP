import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
    public static void main(String[] args) throws IOException {

        //connect to server
        Socket socket = new Socket(InetAddress.getLocalHost(),3333);
        System.out.println("client connected to 3333 ");
        String command=null;

        //create output stream(send)
        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

        //if no command, return invalid
        if (args.length == 0){
            System.out.println("invalid command");
            bufferedWriter.close();
            socket.close();
            return;
        }

        //if it begins with ""get", just send the file name to server.
        if (args.length == 2 && args[0].equals("get")){
            command = args[1];
            bufferedWriter.write(command);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }else if(args.length==1 && args[0].equals("index")){
            command = "index";
            bufferedWriter.write(command);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }else {
            System.out.println("invalid input");
            bufferedWriter.close();
            socket.close();
            return;
        }

        //create input stream,(receive)
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        while (true) {
            String content = bufferedReader.readLine();
            if (content==null)
                break;
            System.out.println(content);
        }

        //close everyrthing

        bufferedWriter.close();
        bufferedReader.close();
        socket.close();

    }


}
