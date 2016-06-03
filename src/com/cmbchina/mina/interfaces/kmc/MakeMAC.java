package com.cmbchina.mina.interfaces.kmc;

import com.cmbchina.mina.abstracts.IoWork;
import com.cmbchina.mina.interfaces.factory.KMCWorkMngr;
import com.cmbchina.mina.proto.HsmRequest;
import com.cmbchina.mina.proto.HsmResponse;

public class MakeMAC implements IoWork {
	public static void register() {
		KMCWorkMngr.addWork("MakeMAC", new MakeMAC());
	}

	@Override
	public String request(Object request) {
		return "NCMUMA1580551B4E5427BC" + request;
	}

	@Override
	public Object response(Object response) { 
		return response.toString().substring(8, response.toString().length()-1);
	}

}
