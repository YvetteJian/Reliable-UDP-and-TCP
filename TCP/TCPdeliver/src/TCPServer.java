import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static void main(String[] args) throws IOException {

        //see if this path is valid .
        File directory =new File(args[0]);
        if (!directory.exists()){
            System.out.println("This directory not exist.");
            return;
        } else if (!directory.isDirectory()) {
            System.out.println("This is not a directory!");
            return;
        }

        //listen on port and waiting for connection.

        ServerSocket serverSocket = new ServerSocket(3333);
        System.out.println("server on, listening at 3333");
        Socket socket = serverSocket.accept();

        //create socket inputStream(receive)
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String s = bufferedReader.readLine();
        if (s==null){
            bufferedReader.close();
            socket.close();
            return;
        }
        System.out.println(s);

        String[] msg = null;

        //create output stream(send),if command is index,send each item in directory, otherwise try to seek
        //the file, if exists, send ok and each line of the content, if not return error.

        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

        if (s.equals("index")){
            msg = directory.list();
            for (int i = 0; i < msg.length; i++){
                bufferedWriter.write(msg[i]);
                System.out.println(msg[i]);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }else {
            //find if the file exists in directory
            File f = new File(directory,s);

            if (f.exists()) {
                bufferedWriter.write("ok");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                BufferedReader fileContent= new BufferedReader(new FileReader(f));
                while (true){
                    String content = fileContent.readLine();
                    if (content == null){
                        break;
                    }
                    bufferedWriter.write(content);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
            else{
                bufferedWriter.write("error");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }
        //close everything
        bufferedWriter.close();
        bufferedReader.close();
        socket.close();
        serverSocket.close();

    }

}
