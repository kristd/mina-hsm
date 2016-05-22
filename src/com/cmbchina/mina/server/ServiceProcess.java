package com.cmbchina.mina.server;

import java.io.IOException;

import com.cmbchina.mina.client.HsmClientPool;
import com.cmbchina.mina.utils.GlobalVars;
import com.cmbchina.mina.utils.JSONUtil;

public class ServiceProcess {
	private static void initialize() throws IOException {
	}
	
	private static void startHsmService() {
		HsmClientPool.init();
		HsmClientPool.start();
	}
	
	private static void startListener() throws Exception {
		ServiceSocket.init();
		ServiceSocket.listen();
	}
	
	private static void startDbService() {
		;
	}
	
	public static void start() throws Exception {
		initialize();
		startHsmService();
		startDbService();
		startListener();
	}
	
	public static void stop() {
		;
	}
}
