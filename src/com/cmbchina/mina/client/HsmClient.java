package com.cmbchina.mina.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbchina.mina.abstracts.HsmWorkManager;
import com.cmbchina.mina.enums.Status;
import com.cmbchina.mina.interfaces.factory.WorkMngrFactory;
import com.cmbchina.mina.proto.HsmRequest;

public class HsmClient {
	private String m_ip;
	private int m_port;
	private int m_conn;
	private int m_maxconn;
	private int m_minconn;
	private int m_timeout;
	private Status m_status;
	private HsmSocketMngr m_sockMngr;
	private String m_appname;
	private int m_weight;
	private final Object m_lock = new Object();
	private HsmWorkManager m_workMngr;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HsmClient.class);
	
	public HsmClient(String appname, String ip, int port, int timeout, int maxconn) {
		m_appname = appname;
		m_ip = ip;
		m_port = port;
		m_timeout = timeout;
		m_conn = maxconn;
		m_weight = 0;
		m_status = Status.FREE;
		m_sockMngr = new HsmSocketMngr(m_appname, m_ip, m_port, m_timeout, m_conn);
		m_workMngr = WorkMngrFactory.loadWorkManager(m_appname);
	}
	
	public void start() throws Exception {
		LOGGER.info("start HsmClient=" + this.toString());
		m_sockMngr.start();
	}
	
	public Object process(String job, String request) {
		HsmRequest _request = m_workMngr.request(job, request);
		m_sockMngr.getConnection().send("");
		
		return null;
	}
	
	/*
	public Status refresh() {
		int free_num=0;
		int busy_num=0;
		float per = 0;
	
		Iterator<HsmSocket> it = connPool.iterator();
		while(it.hasNext())	{
			HsmSocket conn = (HsmSocket)it.next();
			if(conn.getConnStatus() == HsmSocket.HSM_CONN_FREE)
				free_num++;
			else if(conn.getConnStatus() == HsmSocket.HSM_CONN_BUSY)
				busy_num++;
			else if(conn.getConnStatus() == HsmSocket.HSM_CONN_ERR)
			{
				m_status = Status.ERROR;
				return m_status;
			}
			
		}
		
		per = free_num/m_conn;
		if(per > 0.5) {
			m_status = Status.FREE;
		}
		else {
			m_status = Status.BUSY;
		}
		
		return m_status;
	}
	*/
	
	public Status setHsmStatus(int nfree) {
		return Status.FREE;
	}
	
	public String getStatus() {
		synchronized(m_lock) {
			//refresh();
			return m_status.toString();
		}
	}

	public String getAppName(String name) {
		return m_appname;
	}
}
