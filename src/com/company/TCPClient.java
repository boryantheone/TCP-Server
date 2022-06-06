package com.company;

import java.io.*;
import java.net.*;
import java.util.Scanner;

//1. Реализовать приложения клиент и сервер (0 – TCP протокол).
//2. Реализовать в клиенте указание адреса и порта сервера, так: 3 – из файла настроек.
//3. Реализовать указание порта для сервера, так: 2 – из командной строки.
//4. Сообщения, получаемые клиентом с сервера должны записываться в файл
//«Журнала клиента» путь к которому определяется следующим образом: 2 – из командной строки.
//5. Сообщения, получаемые сервером от клиента должны записываться в файл
//«Журнала сервера» путь к которому определяется следующим образом: 3 – из файла настроек.

public class TCPClient implements Runnable {
    private static String clientJournal;
    private static int port;
    private static String host;

    public TCPClient(String clientJournal, int port, String host){
        this.clientJournal = clientJournal;
        this.port = port;
        this.host = host;
    }
    public void run() {
        String response = "";
        while (true) {
            try {
                Socket socket = new Socket(host, port);

                System.out.println("Введите выражение: ");

                OutputStream out = socket.getOutputStream();
                OutputStreamWriter streamWriter = new OutputStreamWriter(out);
                PrintWriter pWriter = new PrintWriter(streamWriter);

                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    String message = scanner.nextLine();

                    pWriter.println(message);
                    pWriter.flush();

                    if (message.contains("="))
                        break;
                }

                InputStream in = socket.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(streamReader);
                response = reader.readLine();

                FileWriter writer = new FileWriter(clientJournal, true);
                BufferedWriter bw = new BufferedWriter(writer);

                try {
                    bw.write("Клиент: " + response + "\n");
                } finally {
                    bw.close();
                }
                pWriter.close();
                reader.close();
            } catch (IOException e) {
                System.err.println("исключение: " + e.toString());
            }

            System.out.println("Клиент получил результат: " + response);
        }
    }

    public static void main(String[] args) {
        BufferedReader readerIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("Введите путь к журналу клиента");
            clientJournal = readerIn.readLine();
            File clientJournalFile = new File(clientJournal);
            try{
                if (!clientJournalFile.exists())
                    clientJournalFile.createNewFile();
            } catch (IOException e){
                System.out.println(e);
                return;
            }
            System.out.println("Введите порт");
            port = Integer.parseInt(readerIn.readLine());
            System.out.println("Введите адрес");
            host = readerIn.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TCPClient ja = new TCPClient(clientJournal, port, host);
        Thread th = new Thread(ja);
        th.start();
    }


}
