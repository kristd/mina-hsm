package com.cmbchina.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.cmbchina.mina.abstracts.IoSocket;
import com.cmbchina.mina.conf.SocketJSONConf;
import com.cmbchina.mina.filter.keepalive.HsmKeepAliveFilterFactory;
import com.cmbchina.mina.utils.GlobalVars;
import com.cmbchina.mina.utils.JSONUtil;


public class ServiceSocket extends IoSocket {
	private static IoAcceptor m_acceptor;
	private static IoHandlerAdapter m_handler;
	
	private static String m_ip;
	private static int m_port;
	private static int m_maxConn;
	
	SocketJSONConf m_serviceconf;
	
	private static Object m_lock = new Object();
	
	private static class InstanceHolder {
		static final ServiceSocket INSTANCE = new ServiceSocket();
	}
	
	private ServiceSocket() {
		m_serviceconf = new SocketJSONConf();
		m_acceptor = new NioSocketAcceptor();
	}
	
	public static ServiceSocket instance() {
		return InstanceHolder.INSTANCE;
	}
	
	public boolean init() throws Exception {
		m_serviceconf.loadObject(JSONUtil.parserJSONArray(ResourceMngr.getServiceConfigData(GlobalVars.SERVICE_CFG)));
		
		m_ip = m_serviceconf.socketAddr();
		m_port = m_serviceconf.socketPort();
		m_maxConn = m_serviceconf.socketMaxConn();
		
		m_acceptor.getSessionConfig().setReadBufferSize(m_serviceconf.socketBufferSize());
		m_acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, m_serviceconf.socketIdleTime());
		
		if(m_serviceconf.logger()) {
			setupLoggerFilter();
		}
		
		if(m_serviceconf.threadPool()) {
			setupThreadsFilter();
		}
		
		if(m_serviceconf.codeflter()) {
			setupCodecFilter();
		}
		
		if(m_serviceconf.keepalive()) {
			setupKeepaliveFilter();
		}
		
		m_handler = new ServiceHandler();
		m_acceptor.setHandler(m_handler);
		
		return true;
	}
	
	protected void setupLoggerFilter() {
		m_acceptor.getFilterChain().addLast("logger", new LoggingFilter());
	}
	
	protected void setupThreadsFilter() {
		m_acceptor.getFilterChain().addLast("threads", new ExecutorFilter(Executors.newCachedThreadPool()));
	}
	
	protected void setupCodecFilter() {
		PrefixedStringCodecFactory codfilter = new PrefixedStringCodecFactory(Charset.forName(m_serviceconf.codeflterEncoding()));
		codfilter.setEncoderPrefixLength(m_serviceconf.codeflterEncodePrefix());
		codfilter.setDecoderPrefixLength(m_serviceconf.codeflterDecodePrefix());
		m_acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codfilter));
	}
	
	protected void setupKeepaliveFilter() {
		KeepAliveFilter servKeepAliveFilterFactory = new KeepAliveFilter(new HsmKeepAliveFilterFactory(), IdleStatus.BOTH_IDLE);
		servKeepAliveFilterFactory.setRequestInterval(m_serviceconf.keepaliveInterval());
		servKeepAliveFilterFactory.setRequestTimeout(m_serviceconf.keepaliveTimeout());
		servKeepAliveFilterFactory.setForwardEvent(m_serviceconf.keepaliveForward());
		m_acceptor.getFilterChain().addLast("keepalive", servKeepAliveFilterFactory);
	}
	
	public void listen() throws IOException {
		m_acceptor.bind(new InetSocketAddress(m_port));
		System.out.println("[" + m_port + "]service started..");
	}
	
	public void close() {
		if(m_acceptor != null) {
			m_acceptor.dispose();
		}
	}
	
	//CHECK may cause a bug 
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
