package com.mycompany.app;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import crosby.binary.osmosis.OsmosisReader;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOsmReader implements Sink {
    private static DecimalFormat df2 = new DecimalFormat(".##");
   private static Map<Long,Vertex> mapOFIDVertex=new HashMap<>();
    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof NodeContainer) {
            Node node = ((NodeContainer) entityContainer).getEntity();
            if (node != null) {
                addLocation(node.getId(), "", node.getLatitude(), node.getLongitude(), "place", 1.0d, 1.0d, 1.0d);
            }
        } else if (entityContainer instanceof WayContainer & false) {
            Way myWay = ((WayContainer) entityContainer).getEntity();
            addRoad(myWay.getWayNodes());
        } else if (entityContainer instanceof RelationContainer && false) {
            // Nothing to do here
            Relation relation = ((RelationContainer) entityContainer).getEntity();
        } else {
            //System.out.println("Unknown Entity!");
        }

    }

    @Override
    public void initialize(Map<String, Object> map) {

    }

    @Override
    public void complete() {
        try {
            System.out.print("Node is completed");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void close() {

    }

    static {
       /* OrientGraphNoTx graph=OrienDBConnection.getOianDBConnection();
        Iterable<Vertex> vertexList=graph.getVertices();
        for (Vertex vertex:vertexList) {
            mapOFIDVertex.put(vertex.getProperty("id"),vertex);
        }*/
    }




    public static Vertex addLocation(long id,String name, Double lat, Double lon, String locType,
                                     Double trafficFactor, Double climateFactor, Double roadQualityFactor) {
        Vertex location = OrienDBConnection.getOianDBConnection().addVertex("class:Location");
        location.setProperty("id",id);
        location.setProperty("name", name);
        location.setProperty("lat", lat);
        location.setProperty("lon", lon);
        location.setProperty("type", locType);
        location.setProperty("trafficFactor", trafficFactor);
        location.setProperty("climateFactor", climateFactor);
        location.setProperty("roadQualityFactor", roadQualityFactor);
        return location;
    }

    public static void addRoad(List<WayNode> wayNodeList){
        OrientGraphNoTx graph=OrienDBConnection.getOianDBConnection();
        WayNode temp=null;
        for (WayNode wayNode:wayNodeList){
            if(temp!=null) {
                //TODO: call distance API and pass the distance below method
                addRoad(wayNode.getNodeId()+"",50.0,mapOFIDVertex.get(temp.getNodeId()),mapOFIDVertex.get(wayNode.getNodeId()));
                temp=wayNode;

            }else{
                temp=wayNode;
            }

        }

    }

    public static Edge addRoad(String name, Double distance, Vertex from, Vertex to) {
        OrientGraphNoTx graph=OrienDBConnection.getOianDBConnection();
        Edge road = graph.addEdge(name, from, to, "Road");
        road.setProperty("name", name);
        road.setProperty("distance", df2.format(distance));
        return road;
    }


    public static void main(String[] args) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("/home/ashish/Documents/personal/try/my-app/new_delhi.pbf");
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(new MyOsmReader());
        reader.run();
    }
}
