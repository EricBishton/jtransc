package java.net;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

public class DatagramSocket implements java.io.Closeable {

	public DatagramSocket() throws SocketException {
		this(new InetSocketAddress(0));
	}

	//protected DatagramSocket(DatagramSocketImpl impl) {
	//	throw new RuntimeException("Not implemented");
	//}

	public DatagramSocket(SocketAddress bindaddr) throws SocketException {
		throw new RuntimeException("Not implemented");
	}

	public DatagramSocket(int port) throws SocketException {
		this(port, null);
	}

	public DatagramSocket(int port, InetAddress laddr) throws SocketException {
		this(new InetSocketAddress(laddr, port));
	}

	native public synchronized void bind(SocketAddress addr) throws SocketException;

	native public void connect(InetAddress address, int port);

	native public void connect(SocketAddress addr) throws SocketException;

	native public void disconnect();

	native public boolean isBound();

	native public boolean isConnected();

	native public InetAddress getInetAddress();

	native public int getPort();

	native public SocketAddress getRemoteSocketAddress();

	native public SocketAddress getLocalSocketAddress();

	native public void send(DatagramPacket p) throws IOException;

	native public synchronized void receive(DatagramPacket p) throws IOException;

	native public InetAddress getLocalAddress();

	native public int getLocalPort();

	native public synchronized void setSoTimeout(int timeout) throws SocketException;

	native public synchronized int getSoTimeout() throws SocketException;

	native public synchronized void setSendBufferSize(int size) throws SocketException;

	native public synchronized int getSendBufferSize() throws SocketException;

	native public synchronized void setReceiveBufferSize(int size) throws SocketException;

	native public synchronized int getReceiveBufferSize() throws SocketException;

	native public synchronized void setReuseAddress(boolean on) throws SocketException;

	native public synchronized boolean getReuseAddress() throws SocketException;

	native public synchronized void setBroadcast(boolean on) throws SocketException;

	native public synchronized boolean getBroadcast() throws SocketException;

	native public synchronized void setTrafficClass(int tc) throws SocketException;

	native public synchronized int getTrafficClass() throws SocketException;

	native public void close();

	native public boolean isClosed();

	public DatagramChannel getChannel() {
		return null;
	}

	//native public static synchronized void setDatagramSocketImplFactory(DatagramSocketImplFactory fac) throws IOException;
}
