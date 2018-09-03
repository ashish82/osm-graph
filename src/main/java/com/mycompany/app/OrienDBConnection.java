package com.mycompany.app;

import com.orientechnologies.orient.core.command.script.OCommandFunction;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import java.util.Optional;

public class OrienDBConnection {

    private static OrientGraphNoTx graphNoTx = new OrientGraphNoTx(ServiceConstants.DB_URL + ServiceConstants.DB_FOLDER, ServiceConstants.DB_USERNAME, ServiceConstants.DB_PASSWORD);

    public static void clearDb() {
        graphNoTx.command(
                new OCommandFunction(ServiceConstants.RESET_COMMAND)).execute();
        try {
            System.out.println("Refreshing Simulator database...");
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static OrientGraphNoTx getOianDBConnection(){
        if(graphNoTx!=null){
          return graphNoTx;
        }
        return graphNoTx = new OrientGraphNoTx(ServiceConstants.DB_URL + ServiceConstants.DB_FOLDER, ServiceConstants.DB_USERNAME, ServiceConstants.DB_PASSWORD);
    }

}
