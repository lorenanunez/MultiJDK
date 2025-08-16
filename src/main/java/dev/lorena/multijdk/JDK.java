package dev.lorena.multijdk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JDK implements Comparable<JDK> {

	private int version;
	private String path;
	private String vendor;
	
	@Override
	public int compareTo(JDK o) {
		int versionCompare = Integer.compare(this.version, o.version);
		if (versionCompare != 0) {
			return versionCompare;
		}
		return this.vendor.compareToIgnoreCase(o.vendor);
	}
	
}