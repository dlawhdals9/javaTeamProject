package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//	특정 클라이언트에서 입력한 메시지를 모든 클라이언트로 전송하는 스레드
public class ChatThread extends Thread {
	
	static List<PrintWriter> chatList = Collections.synchronizedList(new ArrayList<PrintWriter>());
	Socket socket;
	PrintWriter printWriter;
	
	
	public ChatThread() {}
	public ChatThread(Socket socket) {
		this.socket = socket;
//		생성자의 인수로 넘겨받은 소켓으로 클라이언트에게 메시지를 전송할 대 사용하는 PrintWriter 클래스 객체를 생성한다.
		try {
			printWriter = new PrintWriter(socket.getOutputStream());
			chatList.add(printWriter);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	@Override
	public void run() {
//		대화명을 저장할 변수를 만든다.
		String nickname = "";
		
		try {
//			접속된 모든 클라이언트가 입력하는 채팅 메시지를 읽어들일 BufferedReader 클래스 객체를 만든다.
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			접속된 모든 클라이언트가 처음 입력하는 메시지를 대화명으로 사용한다.
			nickname = reader.readLine();
//			새로운 클라이언트가 접속했으므로 서버에 접속중인 모든 클라이언트에게 접속 메시지를 전송한다.
			for(PrintWriter pw : chatList) {
				pw.write("#" + nickname + "# 님이 접속했습니다.\n");
				pw.flush();
			}
			
//			서버에 접속된 모든 클라이언트가 입력하는 2번째 메시지 부터는 채팅 메시지이므로 접속을 종료할 때 까지 반복하며 
//			특정 클라이언트에서 입력한 메시지를 서버에 접속된 모든 클라이언트로 전송한다.
			
			while(true) {
//				특정 클라이언트가 입력한 채팅 데이터를 받는다.
				String str = "";
				try {
					str = reader.readLine();
					
				}catch(SocketException e) {
					break;
				}
//				채팅 창을 닫어서 메시지를 받지 못하면 무한 루프를 탈출시킨다.
				if(str == null) {
					break;
				}
//				접속한 모든 클라이언트로 메시지를 전송한다.
				for(PrintWriter pw : chatList) {
					pw.write("[" + nickname + "] >> " + str + "\n");
					pw.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
//			finally 블록으로 왔다는 것은 서버에 접속한 특정 클라이언트가 채팅 창을 닫았거나 오류로 인해 채팅 창이 중지된 경우이므로 채팅 목록에서 제거한다.
			chatList.remove(printWriter);
//			자식을 제외한 나머지 모든 접속자에게 채팅을 종료했다는 메시지를 전송한다.
			for(PrintWriter pw : chatList) {
				pw.write("#" + nickname + "# 님이 채팅을 종료했습니다.\n");
				pw.flush();
			}
//			채팅을 종료했으므로 통신 소켓을 닫는다.
			if(socket != null ) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		
	}
}
