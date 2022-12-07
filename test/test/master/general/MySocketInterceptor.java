package master.general;

import com.hazelcast.nio.SocketInterceptor;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

public class MySocketInterceptor implements SocketInterceptor {

	@Override
	public void init(Properties properties) {
		System.out.println();
	}

	@Override
	public void onConnect(Socket socket) throws IOException {
//			socket.getOutputStream().write(memberId.getBytes());
		byte[] bytes = new byte[1024];
		int len = socket.getInputStream().read(bytes);
		String otherMemberId = new String(bytes, 0, len);
		if (!otherMemberId.equals("firstMember")) {
			throw new RuntimeException("Not a known member!!!");
		}
	}
}
