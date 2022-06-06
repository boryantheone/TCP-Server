package com.company;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystemException;
import java.util.Scanner;


//Сервер возвращает клиенту результат выражения (допустимые операции «+», «-»).
//Операнды и операции передаются за раз по одному (например, выражение «3.4+1.6-
//5=» нужно передавать с помощью трёх сообщений: «3.4+», «1.6-» и «5=», где «=» -
//признак конца выражения). В случае не возможности разобрать сервером полученную
//строку или при переполнении, возникшем при вычислении полученного выражения,
//сервер присылает клиенту соответствующее уведомление.

public class TCPServer {
    private ServerSocket servSocket;
    static String fileSettingsPath = "/Users/boryantheone/IdeaProjects/Java_Labs/ClientServerSocket/Server/src/fileSettings";
    private static String serverJournal;
    private static int port;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            int PORT = getPortFromFileSettings(fileSettingsPath);
            System.out.println("Введите путь к журналу сервера");
            String serverJournal = scanner.nextLine();
            File serverJournalFile = new File(serverJournal);
            try{
                if (!serverJournalFile.exists())
                    serverJournalFile.createNewFile();
            } catch (IOException e){
                System.out.println(e);
                return;
            }
            if (PORT != -1) {
                TCPServer tcpServer = new TCPServer(PORT, serverJournal);
                tcpServer.go();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TCPServer tcpServer = new TCPServer(port, serverJournal);
        tcpServer.go();
    }

    public TCPServer(int port, String serverJournal){
        try{
            servSocket = new ServerSocket(port);
        }catch(IOException e){
            System.err.println("Не удаётся открыть сокет для сервера: " + e.toString());
        }
        this.serverJournal = serverJournal;
    }
    public void go(){
        class Listener implements Runnable{
            Socket socket;
            public Listener(Socket aSocket){
                socket = aSocket;
            }
            public void run(){
                String response = "";
                String expression = "";
                Boolean sign = null;
                Double intermediate = null;
                Double result = null;
                boolean isError = false;
                try{
                    System.out.println("Слушатель запущен");

                    InputStream in = socket.getInputStream();
                    InputStreamReader streamReader = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(streamReader);

                    FileWriter fileWriter = new FileWriter(serverJournal, true);
                    BufferedWriter bw = new BufferedWriter(fileWriter);

                    while (!expression.contains("=")){
                        expression = reader.readLine();

                        bw.write(expression + "\n");

                        char current;

                        for (int i = 0; i < expression.length(); i++){
                            current = expression.charAt(i);
                            if (Character.isDigit(current) || current == '.')
                                continue;
                            else if (current == '+' || current == '-' || current == '='){
                                String considered = expression.substring(0, i);

                                try {
                                    intermediate = Double.parseDouble(considered);
                                } catch (Exception e){
                                    isError = true;
                                    break;
                                }

                                if (sign != null){
                                    if (sign)
                                        result += intermediate;
                                    else
                                        result -= intermediate;
                                }
                                else result = intermediate;

                                switch (current) {
                                    case '+' -> sign = true;
                                    case '-' -> sign = false;
                                }
                                
                                if (current == '=') {
                                    break;
                                }
                            }
                            else{
                                isError = true;
                                break;
                            }
                        }
                        if (isError) {
                            response = "Ошибка в выражении!\n";
                            break;
                        }
                        response = result.toString();
                    }
                    bw.close();

                    OutputStream out = socket.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(out);
                    PrintWriter pWriter = new PrintWriter(writer);
                    pWriter.println(response);
                    pWriter.flush();

                }catch(IOException e){
                    System.err.println("Exception: " + e.toString());
                }
            }
        }
        System.out.println("Сервер запущен...");
        while(true){
            try{
                Socket socket = servSocket.accept();
                Listener listener = new Listener(socket);
                Thread thread = new Thread(listener);
                thread.start();
            }catch(IOException e){
                System.err.println("Exception: " + e.toString());
            }
        }
    }

    private static int getPortFromFileSettings(String fileSettingsPath) throws IOException {
        File file = new File(fileSettingsPath);
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);
        String line = reader.readLine();
        if (line.isEmpty())
            throw new FileSystemException("Empty file");
        int port = 0;
        try {
            port = Integer.parseInt(line);
        } catch (Exception e) {
            System.out.println(e.toString());
            return (-1);
        }
        return port;
    }
}