package cn.com.cloudfly.qsee.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class PropertySerializable {

	abstract protected String getPropertyFilePath();
	
	private File getPropertyFile(boolean writeMode){
		File propertyFile=new File(getPropertyFilePath());
		if (!propertyFile.exists()){
			if (!writeMode)
				return null;
			try {
				propertyFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}	
		}
		if (!writeMode){
			propertyFile.setReadOnly();
		}
		return propertyFile;
	}
	
	
	private Properties _properties=null;
	private long _propertiesLastModified=0;
	
	public void setProperty(String name,String value){
		File f=getPropertyFile(true);
		if (_properties==null){
			try {
				_properties=new Properties();
				_properties.load(new FileInputStream(f));
				_propertiesLastModified=f.lastModified();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		_properties.setProperty(name, value);
		try {
			_properties.save(new FileOutputStream(f), "");
			_propertiesLastModified=f.lastModified();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getProperty(String name,String defaultValue){
		File f=getPropertyFile(false);
		if (_properties==null || f==null ||_propertiesLastModified!=f.lastModified()){
			if (f==null)
					return defaultValue;
			
			try {
				if (_properties==null){
					_properties=new Properties();
				}
				_properties.load(new FileInputStream(f));
			} catch (IOException e) {
				return defaultValue;
			}	
		}
		return _properties.getProperty(name, defaultValue);
	}
	
}
