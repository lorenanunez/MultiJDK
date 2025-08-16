package dev.lorena.multijdk;

import lombok.Data;

@Data
public class Arguments {
	
	private int version;
	private String jarPath;
	private String[] jarArgs;

}
