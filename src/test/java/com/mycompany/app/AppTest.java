package com.mycompany.app;

import java.util.Arrays;
import java.util.List;

public class AppTest {
	public static void main(String[] args) {
		
		List<String> val=Arrays.asList("1","2","3","4");
		val.parallelStream().forEach(v -> System.out.println("v"+v));
	}
   
}
