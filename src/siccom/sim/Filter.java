package siccom.sim;

import java.io.File;
import java.io.FileFilter;

public class Filter implements FileFilter  
{  
	String prefix;
	String suffix;
	
	public Filter(String prefix, String suffix)
	{
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public boolean accept(File file)  
	{  
      return (file.getName().startsWith(prefix)&&file.getName().endsWith(suffix));
	}
}  