package com.ericsson.so.networklayer.inventoryservice;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.actional.sdk.Log;

public class InventoryModel implements Serializable {

	private static final Logger log = Logger.getLogger (InventoryModel.class.getName());
	private static final long serialVersionUID = -3617584999225004986L;
	
	public int id;
	
	public String type;
	public String neIpAddress;
	
	public String neName;
	public String neLocation;
	
	public String moduleName;
	public String prodNumber;
	public String release;
	public String itemNo;
	public String manufactNo;
	public String manufactYr;
	public String status;
	public String revNo;
	public String position;
	public String polarization;
	
	public String updTime;
	
	public String event;
	public String eventTime;
	
	public InventoryModel() {
		
	}
	
	public InventoryModel(int id, String type, String neIpAddress, String neName, String neLocation, String moduleName,
			String prodNumber, String release, String itemNo, String manufactNo, String manufactYr, String status, String revNo, String position,
			String polarization,String event) {
		super();
		this.id= id;
		this.type = type;
		this.neIpAddress = neIpAddress;
		this.neName = neName;
		this.neLocation = neLocation;
		this.moduleName = moduleName;
		this.prodNumber = prodNumber;
		this.release = release;
		this.itemNo = itemNo;
		this.manufactNo = manufactNo;
		this.manufactYr = manufactYr;
		this.status = status;
		this.revNo = revNo;
		this.position = position;
		this.polarization = polarization;
		this.event=event;
	}

	public void populateUpdTime(String timestamp) {
		String strTime  = "";
		
		try{
			if(timestamp != null) {
				if(timestamp.length() >= 19)
					strTime = timestamp.substring(0, 19);
				else
					strTime = timestamp;
			}			
		}
		catch(Exception e){
			log.error("Exception Occured : ",e);
			//e.printStackTrace();
		}
		this.updTime = strTime;
	}

	//Developer: eohhgas
	public void populateEventTime(String timestamp) {
		String strTime  = "";
		
		try{
			if(timestamp != null) {
				if(timestamp.length() >= 19)
					strTime = timestamp.substring(0, 19);
				else
					strTime = timestamp;
			}			
		}
		catch(Exception e){
			log.error("Exception Occured : ",e);
		}
		this.eventTime = strTime;
	}
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}

	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getNeIpAddress() {
		return neIpAddress;
	}


	public void setNeIpAddress(String neIpAddress) {
		this.neIpAddress = neIpAddress;
	}


	public String getNeName() {
		return neName;
	}


	public void setNeName(String neName) {
		this.neName = neName;
	}


	public String getNeLocation() {
		return neLocation;
	}


	public void setNeLocation(String neLocation) {
		this.neLocation = neLocation;
	}


	public String getModuleName() {
		return moduleName;
	}


	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}


	public String getProdNumber() {
		return prodNumber;
	}


	public void setProdNumber(String prodNumber) {
		this.prodNumber = prodNumber;
	}


	public String getRelease() {
		return release;
	}


	public void setRelease(String release) {
		this.release = release;
	}


	public String getItemNo() {
		return itemNo;
	}


	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}


	public String getManufactNo() {
		return manufactNo;
	}


	public void setManufactNo(String manufactNo) {
		this.manufactNo = manufactNo;
	}


	public String getManufactYr() {
		return manufactYr;
	}

	public void setManufactYr(String manufactYr) {
		this.manufactYr = manufactYr;
	}

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getRevNo() {
		return revNo;
	}


	public void setRevNo(String revNo) {
		this.revNo = revNo;
	}
	
	//used to select unique row in Scanner NE List
  	public String getNeCompositeKey() {
  	    return type + "_" + neIpAddress + "_" + moduleName;
  	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getUpdTime() {
		return updTime;
	}

	public void setUpdTime(String updTime) {
		this.updTime = updTime;
	}
	
	public String getPolarization() {
		return polarization;
	}

	public void setPolarization(String polarization) {
		this.polarization = polarization;
	}
}




