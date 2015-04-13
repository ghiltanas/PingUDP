package ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	private DatagramSocket clientsoc = null;
	private int portServer;
	private String host;
	private boolean stop = false;
	private long start_time = 0;
	private InetAddress address = null;
	private double avgRTT = 0;
	private long minRTT = 10000;
	private long maxRTT = 0;
	private int packetSend = 0;
	private int packetRec = 0;
	private int packetLost = 0;
	private long count_time = 0;
	
	public Client() throws IOException{
		setServer();
	}
	
	public void termina(){
		clientsoc.close();
		
	}
	
	public void calcola(){
		if(packetRec>0){
		avgRTT = (double)count_time/(double)packetRec;
		avgRTT = arrotondamento(avgRTT);
		}
	}
	
	public void stampaRisultati(){
		
		System.out.println("---- PING Statistics ----");
		System.out.println(""+packetSend+ " packets trasmitted, " +packetRec+ " packets received, " +packetLost+ " pacchetti persi.");
		System.out.println("round-trip (ms) min/avg/max = " + minRTT +"/"+avgRTT+"/"+maxRTT);
		termina();
	}	
	
	public double arrotondamento(double x){
		x = Math.floor(x*100);
		x = x/(double)100;
		return x;
		
	}
	
	public void setServer() throws IOException{
		BufferedReader inBuffer = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Inserisci la porta del server: ");
		try {
			String port_TMP = inBuffer.readLine();
			int portTMP = Integer.parseInt(port_TMP);
			if(portTMP<1024 || portTMP>65535){
				System.out.println("numero di porta scorretto!");
				termina();
			}
			else portServer = portTMP;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Inserisci l' host del server: ");
		try {
			String host_tmp = inBuffer.readLine();
			if(host_tmp.compareTo("Localhost")!=0){
				System.out.println("host sconosciuto!");
				termina();
			}
			else host = host_tmp;
			System.out.println("Ok!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		execute();
	}
	
	public void execute() throws IOException{
		try {
			clientsoc = new DatagramSocket();
		}catch (SocketException e) {
			System.out.println("Problemi nella creazione del datagram socket lato client.");
			e.printStackTrace();
		}
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		byte[] Inbuffer = new byte[50];
		byte[] Outbuffer = new byte[50];
		int i = 0;
		while(!stop&&i<10){
			start_time = System.currentTimeMillis();
			String tmp = "PING "+ i + " " + start_time;
			Inbuffer = tmp.getBytes("US-ASCII");
			DatagramPacket myPacket = new DatagramPacket(Inbuffer, tmp.length(), address, portServer);
			DatagramPacket received = new DatagramPacket(Outbuffer,Outbuffer.length);
			clientsoc.send(myPacket);
			packetSend++;
			clientsoc.receive(received);
			long rcv_time = System.currentTimeMillis();
			//System.out.println(rcv_time);
			long diff_time = rcv_time-start_time;
			//System.out.println(diff_time);
			if(diff_time>=150&&diff_time<240){	//sono passati più di 2 secondi ma non scatta il time-out
				System.out.println("* PING " +i+ " non ricevuto.");
				packetLost++;
				i++;
			}
			else if(diff_time<150){ //stampa il ping ricevuto
				count_time = count_time + diff_time;
				if(i==0){
					minRTT = diff_time;
					maxRTT = diff_time;
					avgRTT = diff_time;
				}
				else{
					if(diff_time<minRTT)minRTT = diff_time;
					if(diff_time>maxRTT)maxRTT = diff_time;
						
				}
				String byteToS = new String(received.getData(),0,received.getLength(),"US-ASCII"); 
				System.out.println("minRTT: " +minRTT);
				System.out.println("maxRTT: " +maxRTT);
				System.out.println("avgRTT: " +avgRTT);
				System.out.println(byteToS);
				packetRec++;
				calcola();
				i++;
			}
			else{//scatta il time-out
				packetLost++;
				stop=true;
				calcola();
				stampaRisultati();
			}
		}
		if(!stop){
			stampaRisultati();
		}
	}
	
	
	
	public static void main(String[] args) throws IOException {
		@SuppressWarnings("unused")
		Client c = new Client();
		
	}

}
