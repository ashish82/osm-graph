package com.nitiayog.evstation.data.feeder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import com.nitiayog.evstation.data.feeder.processor.OsmDataProcessor;

import crosby.binary.osmosis.OsmosisReader;

public class ApplicationBootstrap
{
    public static void main( String[] args ) throws FileNotFoundException {
    	String fileName="";
		if(args == null || args.length == 0){
			System.out.println("Please specify osm/pbf path");
			Scanner sc = new Scanner(System.in);
			fileName = sc.nextLine();
			sc.close();
		}else{
			fileName = args[0];
		}
		if(fileName== null || "".equals(fileName)){
			throw new IllegalArgumentException("File Path missing");
		}
		InputStream inputStream = new FileInputStream(fileName);
		OsmosisReader reader = new OsmosisReader(inputStream);
		reader.setSink(new OsmDataProcessor());
		reader.run();
    }
}
