package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Clase servidor
public class ChatServer {

    // Propiedades
    // Array dinámico para guardar los clientes conectados
    private static CopyOnWriteArrayList<ChatThread> clients
            = new CopyOnWriteArrayList<>();

    // método principal
    public static void main(String[] args) {
        int serverPort = 9876;

        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Servidor de chat iniciado en el puerto " + serverPort);

            // Bucle aceptando clientes de chat
            while (true) {
                Socket clientSocket = serverSocket.accept();

                ChatThread chatThread = new ChatThread(clientSocket);
                clients.add(chatThread);

                new Thread(chatThread).start();
            }

        } catch (IOException ex) {
            System.out.println("Error de E/S al iniciar el servidor");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    // Método sincronizado para broadcast
    public static synchronized void broadcast(String message, ChatThread sender) {
        for (ChatThread client : clients) {
            if (client != sender) {
                client.sendmessage(message);
            }
        }
    }

    // Clase hilo
    static class ChatThread implements Runnable {

        // Propiedades
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        private String username;

        // Constructor
        ChatThread(Socket socket) {
            this.socket = socket;
        }

        // Método run()
        @Override
        public void run() {
            try {
                // Capturar los streams de entrada y salida
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                // Registrar al usuario
                output.println("Bienveido al chat.");
                output.println("Ingresa tu nombre de usuario: ");
                username = input.readLine();

                broadcast("El suario [" + username + "] se ha unido al chat.", this);

                String message;
                while ((message = input.readLine()) != null) {
                    broadcast("[" + username + "]: " + message, this);
                }
            } catch (IOException ex) {
                System.out.println("Error en una conexión: " + ex.getMessage());
            } finally {
                // El usuario abandona el chat.
                // Actualizar la lista de clientes
                try {
                    clients.remove(this);
                    broadcast("El usuario [" + username + "] ha abandonado el chat.", this);
                    socket.close();
                } catch (IOException ex) {
                    System.out.println("Error al cerrar una conexión: " + ex.getMessage());
                }
            }
        }

        // Método para enviar un mensaje
        public void sendmessage(String message) {
            output.println(message);
        }
    }

}