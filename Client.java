package Project2;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;

public class Client {
	
	private ObjectOutputStream output;//sending message to server
	private ObjectInputStream input;// receiving message from server
	private ServerSocket server; 
	private String serverIP;
	private static int portNumber;
	private Socket connection;
	private String message = "";
	private int realMsgFlag;
	
	
	public Client(String host) {
		serverIP = host;
	}

	public static void main(String args[]) {
		String s = args[0];//sever IP
		portNumber = Integer.parseInt(args[1]);
		Client client = new Client(s);//local host
		client.startRunning();
	}

	
	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		} catch(EOFException eofException) {
			System.out.println("\n Client terminated connection");
		} catch(IOException ioException) {
			ioException.printStackTrace();
		} finally {
			closeCrap();//close socket
		}
	}
	
	private void connectToServer() throws IOException {
		connection = new Socket(InetAddress.getByName(serverIP), portNumber);// port # make socket
		System.out.println("Connected to: " + connection.getInetAddress().getHostName());
	}
	
	//set up streams to send and receive message
		private void setupStreams() throws IOException {
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			input = new ObjectInputStream(connection.getInputStream());
			System.out.println("\n Dude your streams are now good to go! \n");
		}
	
		//while chatting with server
		private void whileChatting() throws IOException {
			
			//ask user to start the game 
			Scanner userInput = new Scanner(System.in);
			while(true) {
				System.out.println("Ready to start game? (y/n): ");
				String option = userInput.nextLine();
				if(option.equals("y")) {
					//send empty message
					String startingMessage = "0"; 
					sendMessage(startingMessage);
					break;
				}
			}
			
			//the first message received from server
			
			try {
				message = (String)input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			char fakeMsgFlag = message.charAt(0);
		    realMsgFlag = (int) fakeMsgFlag; 
			if(realMsgFlag == 126) {//msgFlag == 0
				int wordLength = Integer.parseInt(message.substring(1, 2));
				int errorCount = Integer.parseInt(message.substring(2, 3));
				String guessingResult = message.substring(3, (3 + wordLength));
				String errorTracking = message.substring(3 + wordLength, (3 + wordLength + errorCount));
				System.out.println(guessingResult);
				System.out.println("Incorrect Guesses:" + errorTracking + "\n");
			}
			
			do {
				try{//what you wanna do while you chatting with someone else
					//let user guess a letter
					Scanner guess = new Scanner(System.in);
					while(true) {
						System.out.print("Letter to guess: ");
						String guessLetter = guess.nextLine();
						if (guessLetter.matches("[A-Za-z]{1}") && guessLetter.length() == 1) {
						    //valid input
							guessLetter = guessLetter.toLowerCase();
							System.out.print(guessLetter + "\n");
							sendMessage("1" + guessLetter);
							
							//receiving result from sever
							message = (String)input.readObject();
						    fakeMsgFlag = message.charAt(0);
							realMsgFlag = (int) fakeMsgFlag; 
							if(realMsgFlag == 126) {//msgFlag == 0
								int wordLength = Integer.parseInt(message.substring(1, 2));
								int errorCount = Integer.parseInt(message.substring(2, 3));
								String guessingResult = message.substring(3, (3 + wordLength));
								String errorTracking = message.substring(3 + wordLength, (3 + wordLength + errorCount));
								System.out.println(guessingResult);
								System.out.println("Incorrect Guesses:" + errorTracking + "\n");
							} else {
								String data = message.substring(1, realMsgFlag + 1);
								System.out.println(data);
							}
							
							break;
						}
						else {
							System.out.println("Invalid  input try again");
						}
					}
				} catch(ClassNotFoundException classNotFoundException) {
					System.out.println("\n I don't know thst object type");
				}
				
			} while(realMsgFlag == 126);
			
			
			
		}
		
		//send message to server
		private void sendMessage(String message) {
			try {
				output.writeObject(message);
				output.flush();
			} catch(IOException ioException) {
				System.out.println("\n something messed up sendting message");
			}
		}
	
		//close the streams and sockets
		private void closeCrap() {
			System.out.println("\n closing crap down..");
			try{
				output.close();
			    input.close();
			    connection.close();
			} catch(IOException ioException) {
					ioException.printStackTrace();
			}
		}
}
