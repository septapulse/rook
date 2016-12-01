package rook.core.transport.aeron;

/**
 * Parsed configuration for an {@link AeronRouter}
 * 
 * @author Eric Thill
 *
 */
public class AeronRouterConfig {

	private String directoryName;
	
	public String getDirectoryName() {
		return directoryName;
	}
	
	public AeronRouterConfig setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
		return this;
	}
	
}
