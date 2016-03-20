package rook.ui.environment;

/**
 * Message that contains information about a service
 * 
 * @author Eric Thill
 *
 */
public class ServiceInfo {
	private String library;
	private String type;
	private String hierarchy;
	
	public ServiceInfo(String library, String type, String hierarchy) {
		this.library = library;
		this.type = type;
		this.hierarchy = hierarchy;
	}
	
	public String getLibrary() {
		return library;
	}
	
	public String getType() {
		return type;
	}
	
	public String getHierarchy() {
		return hierarchy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((library == null) ? 0 : library.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceInfo other = (ServiceInfo) obj;
		if (library == null) {
			if (other.library != null)
				return false;
		} else if (!library.equals(other.library))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServiceInfo [library=" + library + ", type=" + type + ", hierarchy=" + hierarchy + "]";
	}

}
