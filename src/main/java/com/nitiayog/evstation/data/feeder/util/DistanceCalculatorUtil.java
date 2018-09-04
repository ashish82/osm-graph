/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.nitiayog.evstation.data.feeder.util;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.TravelMode;

/**
 */
public class DistanceCalculatorUtil  {
    /**
     * mean radius of the earth
     */
    public final static double R = 6371000; // m
    /**
     * Radius of the earth at equator
     */
    public final static double R_EQ = 6378137; // m
    /**
     * Circumference of the earth
     */
    public final static double C = 2 * PI * R;
    public final static double KM_MILE = 1.609344;

    /**
     * Calculates distance of (from, to) in meter.
     * <p>
     * http://en.wikipedia.org/wiki/Haversine_formula a = sin²(Δlat/2) +
     * cos(lat1).cos(lat2).sin²(Δlong/2) c = 2.atan2(√a, √(1−a)) d = R.c
     */
    public static double calcDist(double fromLat, double fromLon, double toLat, double toLon) {
        double normedDist = calcNormalizedDist(fromLat, fromLon, toLat, toLon);
        return R * 2 * Math.asin(Math.sqrt(normedDist));
    }
    
    public static DistanceMatrixElement getDistanceElement(double fromLat, double fromLon, double toLat, double toLon) {
        /* double normedDist = calcNormalizedDist(fromLat, fromLon, toLat, toLon);
         return R * 2 * asin(sqrt(normedDist));*/
     	GeoApiContext context = new GeoApiContext.Builder()
 				.apiKey("AIzaSyCs26oG0RVyhGX7zE7u7x1T7BlQnzmCPpg").queryRateLimit(20)
 				.build();
 		Gson gson = new GsonBuilder().setPrettyPrinting().create();
 		String[] origins = new String[] {String.valueOf(fromLat+","+fromLon)};
 		String[] destinations = new String[] {String.valueOf(toLat+","+toLon)};
 		DistanceMatrix resul = null;
 		try {
 			resul = DistanceMatrixApi.getDistanceMatrix(context, origins, destinations)
 					.mode(TravelMode.DRIVING).language("en-US").await();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return resul.rows[0].elements[0];
     }


    public static double calcNormalizedDist(double fromLat, double fromLon, double toLat, double toLon) {
        double sinDeltaLat = sin(toRadians(toLat - fromLat) / 2);
        double sinDeltaLon = sin(toRadians(toLon - fromLon) / 2);
        return sinDeltaLat * sinDeltaLat
                + sinDeltaLon * sinDeltaLon * cos(toRadians(fromLat)) * cos(toRadians(toLat));
    }

}
