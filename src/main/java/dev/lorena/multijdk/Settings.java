package dev.lorena.multijdk;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Settings {
	
	private List<String> customJDKlocations;
	private Map<String, String> preferredJDKPerFile;

}
