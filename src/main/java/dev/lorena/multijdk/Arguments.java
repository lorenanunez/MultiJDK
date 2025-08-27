package dev.lorena.multijdk;

import java.util.Set;

import lombok.Data;

@Data
public class Arguments {
	
	private int version;
	private String jarPath;
	private Set<String> jvmArgs;
	private Set<String> jarParams;

}
