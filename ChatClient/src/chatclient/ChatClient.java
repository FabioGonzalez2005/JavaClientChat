package chatclient;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private final String serverAddress;
    private final int serverPort;
    private String username;

    public ChatClient(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    public void iniciar() {
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            System.out.println("Conectado al servidor en " + serverAddress + ":" + serverPort);

            // Iniciar hilo para recibir mensajes
            new Thread(new ReceptorMensajes(socket)).start();

            // Configurar flujo de salida para enviar mensajes
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            // Solicitar nombre de usuario
            username = scanner.nextLine();
            salida.println(username);

            // Bucle para enviar mensajes al servidor
            while (scanner.hasNextLine()) {
                salida.println(scanner.nextLine());
            }

        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }

    private static class ReceptorMensajes implements Runnable {
        private final Socket socket;

        public ReceptorMensajes(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    System.out.println(mensaje);
                }
            } catch (IOException e) {
                System.err.println("Error al recibir mensajes: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        String direccionServidor = "localhost";
        int puertoServidor = 9876;

        ChatClient cliente = new ChatClient(direccionServidor, puertoServidor);
        cliente.iniciar();
    }
}
