package org.cloudbus.cloudsim;

import java.net.*;
import java.io.*;

public class B_ProtocoloServidor {
    private static final int WAITING = 0;
    private static final int SENT_NS2 = 1;

    private int state = WAITING;

    public String processInput(String theInput) {
        String theOutput = null;

        if (theInput.equals("GET SIM_NS2")){
        	//Servidor envia dados para o cliente simular no NS2
        	//
        	//Le o conteudo do arquivo com a simulacao
        	
        	
        }//fim if
        
        switch (state) {
        
        	case WAITING: 
        		theOutput = "200 OK";
        		break;
        	default:
        		theOutput = "200 OK";
        
        }//fim switch
        
        return theOutput;
    }
}
