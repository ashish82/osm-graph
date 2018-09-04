package com.nitiayog.evstation.data.feeder.processor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import com.google.gson.Gson;
import com.google.maps.model.DistanceMatrixElement;
import com.nitiayog.evstation.data.feeder.dao.OrientDBConnection;
import com.nitiayog.evstation.data.feeder.util.DistanceCalculatorUtil;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import crosby.binary.osmosis.OsmosisReader;

public class OsmDataProcessor implements Sink {
	private static Map<Long,Vertex> vertexGraphIds= new HashMap<>();
	private static Map<Long,Node> vertexNodeIds= new HashMap<>();

	private static Gson GSON = new Gson();

	@Override
	public void process(EntityContainer entityContainer) {
		if (entityContainer instanceof NodeContainer) {
			Node node = ((NodeContainer) entityContainer).getEntity();
			if (node != null)) {// && !node.toString().contains("ncdssdame"
				//	if(GeoUtil.distance(AIIMS[0], AIIMS[1], node.getLatitude(), node.getLongitude(), 'K') <= 40){
				vertexNodeIds.put(node.getId(),node);
				System.out.println("Ploting vertex"+ node.getId());
				//System.out.println("NODE::"+node.toString()+" :: "+gson.toJson(node));
				//	}
				//	System.out.println("NODE::"+node.getId()+ " :: " +node.toString()+ " :: " + ""+node.getLatitude()+ " :: " + node.getLongitude()+ " :: " + "place"+ " :: " +1.0d+1.0d+1.0d);
				// addLocation(node.getId(), "", node.getLatitude(), node.getLongitude(), "place", 1.0d, 1.0d, 1.0d);
			}
		} else if (entityContainer instanceof WayContainer) {
			Way myWay = ((WayContainer) entityContainer).getEntity();
			List<Long> wayNodeIds=myWay.getWayNodes().stream().map(n -> n.getNodeId()).collect(Collectors.toList());
			if(!Collections.disjoint(vertexNodeIds.keySet(),wayNodeIds)){//!myWay.toString().contains("nacdme") &&
				System.out.println("Plotting Way"+GSON.toJson(myWay));
				//Gson gson=new Gson();
				addRoad(myWay);
				//System.out.println("WAY::"+gson.toJson(myWay));
				// System.out.println("WAY::"+myWay.toString());
			}
			// addRoad(myWay.getWayNodes());
		} else if (entityContainer instanceof RelationContainer) {
			// Nothing to do here
			Relation relation = ((RelationContainer) entityContainer).getEntity();
			if(!relation.toString().contains("namcdae")){

			}
			//System.out.println("RELATION::"+relation.toString());
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

	public static Vertex addLocation(long id,String name, Double lat, Double lon, String locType,
			Double trafficFactor, Double climateFactor, Double roadQualityFactor) {
		Vertex location = OrientDBConnection.getOianDBConnection().addVertex("class:Location");
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

	public static void addRoad(Way myWay){
		int indx=0;
		for (WayNode wayNode:myWay.getWayNodes()){
			//TODO: call distance API and pass the distance below method
			//   addRoad(wayNode.getNodeId()+"",50.0,mapOFIDVertex.get(temp.getNodeId()),mapOFIDVertex.get(wayNode.getNodeId()));
			if(indx == 0){
				if(wayNode.getNodeId() == myWay.getWayNodes().get(myWay.getWayNodes().size()-1).getNodeId()){
					//Ignore this way as its a closed loop
					return;
				}
			}else{
				long srcId = myWay.getWayNodes().get(indx-1).getNodeId();
				long destId = wayNode.getNodeId();
				System.out.println("Inside src:"+srcId +" :: dest:"+destId);
				Node src = vertexNodeIds.get(srcId);
				Node dest = vertexNodeIds.get(destId);
				if(src != null && dest != null){
					DistanceMatrixElement calcDist = DistanceCalculatorUtil.getDistanceElement(src.getLatitude(),
							src.getLongitude(),dest.getLatitude(),dest.getLongitude());
					if (!vertexGraphIds.containsKey(src.getId())) {
						System.out.println("Create src Vertex ::"+src.getId());
						Vertex locationVertex = addLocation(src.getId(),getNodeTagName(src), src.getLatitude(), src.getLongitude(),
								"place", 1.0d, 1.0d, 1.0d);
						vertexGraphIds.put(src.getId(),locationVertex);
					} else if (!vertexGraphIds.containsKey(dest.getId())) {
						System.out.println("Create dest Vertex ::"+dest.getId());
						Vertex locationVertex = addLocation(dest.getId(), getNodeTagName(dest), dest.getLatitude(), dest.getLongitude(),
								"place", 1.0d, 1.0d, 1.0d);
						vertexGraphIds.put(dest.getId(),locationVertex);
					}
					System.out.println("EDGE->FROM->"+GSON.toJson(src)+" :: TO->"+  GSON.toJson(dest));
					if(vertexGraphIds.get(srcId)!= null && vertexGraphIds.get(destId)!=null){
						addRoad(wayNode.getNodeId()+"",calcDist.distance.inMeters,calcDist.duration.humanReadable,myWay.getTags(),
								vertexGraphIds.get(srcId),vertexGraphIds.get(destId));
					}
				}
			}
			indx++;
		}

	}

	public static String getNodeTagName(Node node){
		Collection tags = node.getTags();
		Iterator arg2 = tags.iterator();
		while (arg2.hasNext()) {
			Tag tag = (Tag) arg2.next();
			if (tag.getKey() != null && tag.getKey().equalsIgnoreCase("name")) {
				return tag.getValue();
			}
		}
		return "";
	}

	public static Edge addRoad(String name, long inMeters, String humanReadable,Collection<Tag> collection, Vertex from, Vertex to) {
		OrientGraphNoTx graph=OrientDBConnection.getOianDBConnection();
		Edge road = graph.addEdge(name, from, to, "Road");
		road.setProperty("name", name);
		road.setProperty("distance", extracted(inMeters));
		road.setProperty("duration", humanReadable);
		road.setProperty("tags", collection.stream().map(d->d.getKey()+":"+d.getValue()).collect(Collectors.toList()));
		return road;
	}

	private static double extracted(long inMeters) {
		double d = inMeters/1000.0d;
		//System.out.println(d);
		return d;
	}


	public static void main(String[] args) throws FileNotFoundException {
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
