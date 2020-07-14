package de.hackermuehle.pdfpresenter.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Preferences {
	
	private Properties _coreProperties;
	private File _file;
	private String _path;
	
	public Preferences() {
		_path = ".";
		_coreProperties = new Properties();
		loadCoreProperties();
	}
	
	private void loadCoreProperties() {
		FileInputStream input = null;
		try {
			_file = new File(_path, "core.properties");
			if (!_file.exists())
				_file.createNewFile();
			
			input = new FileInputStream(_file);
			_coreProperties.load(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void removePreference(String key) {
		_coreProperties.remove(key);
	}
	
	public void setPreference(String key, String value) {
		_coreProperties.setProperty(key, value);
	}
	
	
	/**
	 * Returns null if preference not found
	 * @param key
	 * @return
	 */
	public String getPreference(String key) {
		return _coreProperties.getProperty(key);
	}
	
	
	public void saveToDisk() {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(_file);
			_coreProperties.store(output, "---No comment---");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
}
