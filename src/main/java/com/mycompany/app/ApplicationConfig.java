package com.mycompany.app;

import crosby.binary.osmosis.OsmosisReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Hello world!
 *
 */
public class ApplicationConfig
{
    public static void main( String[] args )
    {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/home/ashish/Documents/personal/try/my-app/new_delhi.pbf");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(new MyOsmReader());
        reader.run();
    }
}
