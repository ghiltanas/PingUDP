package ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

public class Server {

	private long seed;
	private int port;
	private DatagramSocket clientsoc = null;
	private DatagramSocket serversoc = null; 
	private int latency;
	private boolean stop=false;
	private long start_time;
	private InetAddress client_address;
	private String client_ip;
	private int client_port;
	private String messaggio;
	
	public Server() throws IOException{
		openServer();
	}
	
	public void termina(){
		serversoc.close();
		System.out.println("server chiuso.");
	}
	
	public boolean probs(long s){
		boolean i=false;
		long tmp = new Random().nextInt(100)+1;
		if(tmp>(25+s)) i = true;
		String x = ""+s;
		int tmplat = Integer.parseInt(x);
		latency = (new Random().nextInt(201))+(tmplat*10);
		System.out.println(latency);
		return i;
	}
	
	public void openServer() throws IOException{
		BufferedReader inBuffer = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Inserisci la porta del server: ");
		try {
			String port_tmp = inBuffer.readLine();
			int portTMP = Integer.parseInt(port_tmp);
			if(portTMP<1024 || portTMP>65535){
				System.out.println("numero di porta scorretto!");
				termina();
			}
			else port = portTMP;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Inserisci un seed compreso fra 0 e 10: ");
		try {
			String seed_tmp = inBuffer.readLine();
			long seedTMP = Integer.parseInt(seed_tmp);
			if(seedTMP<0 || seedTMP>10){
				System.out.println("seed scorretto!");
				termina();
			}
			else seed = seedTMP;
			System.out.println("Ok!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		execute();		
	}
	//stampa l'indirizzo IP e la porta del client, il messaggio di PING 
	//e l'azione intrapresa dal server in seguito alla sua ricezione (PING non inviato,oppure PING ritardato di x ms).
	
	public void stampaOK(){
		System.out.println("IP: " + client_ip + " Port: " + client_port + " " + messaggio);
		System.out.println("PING inviato dopo: " + latency);
	}
	
	public void stampaFAIL(){
		System.out.println("IP: " + client_ip + " Port: " + client_port + " " + messaggio);
		System.out.println("PING non inviato");
	}
	
	public void execute() throws IOException{
		try {
			//clientsoc = new DatagramSocket();
			serversoc = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione del datagram socket.");
			e.printStackTrace();
		}
		byte[] Inbuffer = new byte[50];
		byte[] Outbuffer = new byte[50];
		boolean invia_eco = false;
		while(!serversoc.isClosed()){
			start_time = System.currentTimeMillis();
			DatagramPacket received = new DatagramPacket(Inbuffer,Inbuffer.length);
			invia_eco = probs(seed);
			//serversoc.setSoTimeout(30000);
			serversoc.receive(received);
			messaggio = new String(received.getData(),"US-ASCII");
			client_address=received.getAddress();
			DatagramPacket sent = new DatagramPacket(Outbuffer, Outbuffer.length);
			sent = received;
			client_ip = client_address.getHostAddress();
			client_port = received.getPort();
			if(invia_eco){
				try {
					Thread.sleep(latency);
				} catch (InterruptedException e) {
					System.out.println("errore nell' attesa del server");
					e.printStackTrace();
				}
				serversoc.send(sent);//pacchetto inviato
				stampaOK();
			}
			else{
				try {
					Thread.sleep(70);
				} catch (InterruptedException e) {
					System.out.println("errore nell' attesa del server");
					e.printStackTrace();
				}
				serversoc.send(sent);//pacchetto fittizio inviato per sbloccare received bloccante del client
				stampaFAIL();
			}
		 }
		 termina();
	}
	
	public static void main(String[] args) throws IOException {
		
		Server s = new Server();

	}

}
