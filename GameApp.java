
import java.util.*;
import java.net.*;
import java.io.*;

public class GameApp {

    static public final int SERVER = 1;
    static public final int CLIENT = 2;
    static public final int UDP = 1;
    static public final int TCP = 2;
    static int type = 0;
    static int protocol = 0;
    static int wellKnownPort = 2000;
    static Scanner scan = new Scanner(System.in);
    static Random random = new Random();

    static int _nextInt() {
        String line = scan.nextLine();
        while (!isNum(line)) {
            line = scan.nextLine();
        }
        return Integer.parseInt(line);
    }

    static int _nextPort() {
        String line = scan.nextLine();

        while (!isPort(line)) {
            System.out.println("Invalid port Nr. Try again:");
            line = scan.nextLine();
        }

        return Integer.parseInt(line);
    }

    public static void main(String[] args) throws IOException {
        while (true) {
            do {
                System.out.print("(1) Server or (2) client? > ");
                type = _nextInt();
            } while (type != SERVER && type != CLIENT);

            if (type == SERVER) {
                System.out.print("Server port: ");
                wellKnownPort = _nextPort();
            }

            do {
                System.out.print("(1) UDP or (2) TCP? > ");
                protocol = _nextInt();
            } while (protocol != UDP && protocol != TCP);

            if (type == SERVER) {
                if (protocol == UDP) {
                    udpServer(wellKnownPort);
                } else {
                    tcpServer(wellKnownPort);
                }
                System.out.println("Could not start server with port. Try again.");
            } else {
                if (protocol == UDP) {
                    udpClient();
                } else {
                    tcpClient();
                }
            }
        }
    }

    private static boolean isPort(String str) {
        if (isNum(str)) {
            if (Integer.parseInt(str) > 0 && Integer.parseInt(str) <= 65535) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNum(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static void udpServer(int port) {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            boolean busy = false;
            int numberToGuess = 0;
            long startTime = 0;
            InetAddress clientAddress = null;
            int clientPort = 0;
            System.out.println("UDP server running at port " + port);
            while (true) {
                byte[] requestBuffer = new byte[512];
                DatagramPacket request = new DatagramPacket(requestBuffer, requestBuffer.length);
                socket.receive(request);
                String message = new String(requestBuffer, 0, request.getLength());

                if (busy && System.currentTimeMillis() - startTime >= 30000) {
                    busy = false;
                    if (request.getAddress().equals(clientAddress) && request.getPort() == clientPort) {
                        String answer = "TIMEOUT OCCURED";
                        byte[] buffer = answer.getBytes();
                        DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                        socket.send(response);
                        socket.receive(request);
                        message = new String(requestBuffer, 0, request.getLength());
                    }
                }
                if (busy) {
                    if (request.getAddress().equals(clientAddress) && request.getPort() == clientPort) {
                        if (isNum(message)) {
                            String answer;
                            if (Integer.parseInt(message) == numberToGuess) {
                                answer = "CORRECT";
                                busy = false;
                            } else if (Integer.parseInt(message) > numberToGuess) {
                                answer = "HI";
                            } else {
                                answer = "LO";
                            }
                            byte[] buffer = answer.getBytes();
                            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                            socket.send(response);
                            startTime = System.currentTimeMillis();
                        } else {
                            String answer = "YOU ARE DISCONNECTED";
                            byte[] buffer = answer.getBytes();
                            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                            socket.send(response);
                            busy = false;
                        }

                    } else {
                        String answer = "BUSY";
                        byte[] buffer = answer.getBytes();
                        DatagramPacket response = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
                        socket.send(response);
                    }
                } else {
                    if (message.equals("HELLO")) {
                        startTime = System.currentTimeMillis();
                        String answer = "WELCOME";
                        byte[] buffer = answer.getBytes();
                        busy = true;
                        clientPort = request.getPort();
                        clientAddress = request.getAddress();
                        DatagramPacket response = new DatagramPacket(buffer, buffer.length, request.getAddress(), clientPort);
                        socket.send(response);
                        numberToGuess = random.nextInt(100);
                    } else {
                        String answer = "YOU ARE DISCONNECTED. CONNECT AGAIN AND TYPE HELLO";
                        byte[] buffer = answer.getBytes();
                        DatagramPacket response = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
                        socket.send(response);
                    }
                }
            }
        } catch (SocketException ex) {
            //someone disconnected, accept new instead
        } catch (IOException ex) {
            
        }

    }

    static void udpClient() {
        System.out.println("UDP client");
        System.out.println("-------------------");

            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setSoTimeout(1000);
                Scanner scanner = new Scanner(System.in);

                System.out.println("");
                String addAddressText = "ADD SERVER ADDRESS";
                System.out.println(addAddressText);
                InetAddress address = InetAddress.getByName(scanner.nextLine());
                System.out.println("");
                String addPortText = "ADD YOUR PORT";
                System.out.println(addPortText);
                String portText = scanner.nextLine();
                int port = Integer.parseInt(portText);
                byte[] requestBuffer;
                DatagramPacket request;
                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    System.out.println("Write your input: ");
                    String guess = scanner.nextLine().toUpperCase();
                    while (true) {
                        requestBuffer = guess.getBytes();
                        request = new DatagramPacket(requestBuffer, requestBuffer.length, address, port);
                        socket.send(request);
                        socket.receive(response);
                        String answer = new String(buffer, 0, response.getLength());
                        System.out.println(answer);
                        if (!(answer.equals("WELCOME") || answer.equals("HI") || answer.equals("LO"))) {
                            break;
                        }
                        System.out.println("");
                        System.out.println("Write your input: ");
                        guess = scanner.nextLine();
                    }
                    String answerQuestion;
                    System.out.println("Continue with settings? (n = no, anything else = yes): ");
                    answerQuestion = scanner.nextLine();
                    answerQuestion = answerQuestion + "y";
                    if (answerQuestion.charAt(0) == 'n') {
                        break;
                    }
                }
            }   catch (SocketTimeoutException ex) {
                System.out.println("Timeout error: Server is not responding");
            } catch (IOException ex) {
                System.out.println("Invalid adress. Try again.");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid portNr. Try again");
            }
    }

    static void tcpServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("TCP server running at port " + port);
            while (true) {
                int numberToGuess = 0;
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(30000);
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    String message;
                    numberToGuess = random.nextInt(100);
                    message = in.readLine();
                    if (message.equals("HELLO")) {
                        out.println("WELCOME");
                        while ((message = in.readLine()) != null) {
                            if (isNum(message)) {
                                if (Integer.parseInt(message) == numberToGuess) {
                                    out.println("CORRECT");
                                    break;
                                } else if (Integer.parseInt(message) > numberToGuess) {
                                    out.println("HI");
                                } else {
                                    out.println("LO");
                                }
                            } else {
                                out.println("YOU ARE DISCONNECTED");
                                break;
                            }
                        }
                    } else {
                        out.println("YOU ARE DISCONNECTED. CONNECT AGAIN AND TYPE HELLO");
                    }
                } catch (SocketException ex) {
                    //someone disconnected, accept new instead
                } catch (SocketTimeoutException ex) {
                    //client was too slow, accept new instead
                }
            }
        } catch (IOException ex) {
        }
    }

    static void tcpClient() throws IOException {
        // Put your tcp client code here
        System.out.println("TCP client.");
        System.out.println("-------------------");
        Scanner scanner = new Scanner(System.in);
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        System.out.println("");
        String addAddressText = "ADD SERVER ADDRESS";
        System.out.println(addAddressText);
        String address = scanner.nextLine();

        System.out.println("");
        String addPortText = "ADD YOUR PORT";
        System.out.println(addPortText);
        String portText = scanner.nextLine();

        int port = Integer.parseInt(portText);
        while (true) {
            try {   
            System.out.print("input: ");
            String userInput = scanner.nextLine();
            socket = new Socket(address, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            socket.setSoTimeout(1000);
            out.println(userInput.toUpperCase());
            while (true) {
                String answer = in.readLine();
                System.out.println(answer);
                if (!(answer.equals("WELCOME") || answer.equals("HI") || answer.equals("LO"))) {
                    break;
                }
                System.out.println("");
                System.out.print("input: ");
                userInput = scanner.nextLine();
                out.println(userInput);
            }
            out.close();
            in.close();
            socket.close();

            String answerQuestion;
            System.out.println("Continue with settings? (y/n)(anything else is yes): ");
            answerQuestion = scanner.nextLine();
            answerQuestion = answerQuestion + "y";
            if (answerQuestion.charAt(0) == 'n') {
                break;
            }
            } catch (SocketTimeoutException ex) {
            System.out.println("Socket got timed out");

            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            } catch (IOException ex) {
                System.out.println("No connection to server. Try again by typing HELLO.");
                break;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid portNr. Try again");
                break;
            }
        }
    }
}