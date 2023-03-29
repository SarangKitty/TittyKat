package com.ericsson.so.networklayer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ericsson.so.networklayer.Constants;
import com.ericsson.so.networklayer.NlsMessageHandler;
import com.ericsson.so.networklayer.NWH_Handler.NeDetailsModel;
import com.ericsson.so.networklayer.Utils.StringHandling;
import com.ericsson.so.networklayer.Utils.Utils;
import com.ericsson.so.networklayer.inventoryservice.InventoryImportBean;
import com.ericsson.so.networklayer.inventoryservice.InventoryModel;
import com.ericsson.so.networklayer.inventoryservice.SOAKernelDetails;
import com.ericsson.so.networklayer.inventoryservice.SoaFunctionBlock;
import com.ericsson.so.networklayer.log.SOLoggerFactory;


/**
 * @author Shubhadip Bera
 * 
 * This class is defined to handle the Database connection for Adapter NE List
 * Also do all required database related operations 
 * 	-Add New Adapter NE
 * 	-Delete New Adapter NE
 * 	-Update New Adapter NE
 * 	-Validate Adapter Entry
 *  
 */

public class Inventory_DBConnection extends BaseDBConnection implements AdapterDBInterface
{
	private static final Logger log = Logger.getLogger (Inventory_DBConnection.class.getName());
	Utils utHand = new Utils();
	StringHandling strHand = new StringHandling();
	private NlsMessageHandler messages = NlsMessageHandler.getMsgInstance();

	public Inventory_DBConnection(){
		log.debug("Inventory_DBConnection....");
	}
	
	public class AdapterInfo {
		String adapterType;
		String adapterHostIp;
		String adapterInstance;
		String adapterId;
		
		public AdapterInfo(String adapterType, String adapterHostIp, String adapterInstance, String adapterId) {
			this.adapterType = adapterType;
			this.adapterHostIp = adapterHostIp;
			this.adapterInstance = adapterInstance;
			this.adapterId = adapterId;
		}

		public String getAdapterType() {
			return adapterType;
		}

		public void setAdapterType(String adapterType) {
			this.adapterType = adapterType;
		}

		public String getAdapterHostIp() {
			return adapterHostIp;
		}

		public void setAdapterHostIp(String adapterHostIp) {
			this.adapterHostIp = adapterHostIp;
		}
		
		public String getAdapterInstance() {
			return adapterInstance;
		}

		public void setAdapterInstance(String adapterInstance) {
			this.adapterInstance = adapterInstance;
		}

		public String getAdapterId() {
			return adapterId;
		}

		public void setAdapterId(String adapterId) {
			this.adapterId = adapterId;
		}

		@Override
	    public boolean equals(Object object)
	    {
			if(object instanceof AdapterInfo)
			{
				return ( this.adapterType.equals(((AdapterInfo)object).getAdapterType()) &&
						 this.adapterHostIp.equals(((AdapterInfo)object).getAdapterHostIp()) &&
						 this.adapterInstance.contentEquals(((AdapterInfo)object).getAdapterInstance())
						);
			}
			return false;
	    }
		
		@Override
		public int hashCode() 
		{
			return adapterType.hashCode() *
				   adapterHostIp.hashCode() *
				   adapterInstance.hashCode();
		}
	}

	/**
	 * This method returns the adater type and host ip put in the set of class AdapterInfo
	 * 
	 * @author esthdeb
	 * @param colName
	 * @return
	 */
	public Set<AdapterInfo> availableListFromKerneltable() {
		log.debug("availableListFromKerneltable ++");
		Set<AdapterInfo> adapters = new HashSet<AdapterInfo>();

		Connection con=null;
		Statement stmt=null;
		ResultSet results=null;

		try {
			con=getDBConnection();
			if(con != null){
				stmt=con.createStatement();

				String selection = "SELECT * FROM KERNEL_SYSTEMS_TABLE WHERE SYSTEM_TYPE = 'ADAPTER' ORDER BY KERNEL_NAME";
				log.debug(Constants.SEL_QUERY_PRINT+selection);
				results = stmt.executeQuery(selection );
				//set flag as valid, if entry found
				while (results.next()){
					String adapterInstance = results.getString("KERNEL_NAME") + "-" + results.getString("KERNEL_ID");
					adapters.add(new AdapterInfo(results.getString("KERNEL_NAME"), results.getString("HOST_NAME"), adapterInstance, results.getString("KERNEL_ID")));
				}
			}
		}catch (SQLException sqle){
			log.debug("Error !!availableListFromKerneltable : SQLException :",sqle);
		}catch (Exception e){
			log.debug("Error !!availableListFromKerneltable : Exception : ",e);
		}
		finally{
			close(results,stmt,con);
		}
		log.debug("adapters size -> " + adapters.size());
		return adapters;
	}
	
	public List<SOAKernelDetails> populateSOAKernelDetails() {
		log.debug("populateSOAKernelDetails ++");
		List<SOAKernelDetails> soaKernelsFromDb = new ArrayList<SOAKernelDetails>();
		
		Connection con=null;
		Statement stmt=null;
		ResultSet results=null;

		try {
			con=getDBConnection();
			if(con != null){
				stmt=con.createStatement();

				String selection = "SELECT * FROM KERNEL_SYSTEMS_TABLE WHERE SYSTEM_TYPE = 'SOA'";
				log.debug(Constants.SEL_QUERY_PRINT+selection);
				results = stmt.executeQuery(selection );
				
				while (results.next()){
					String hostName = results.getString("HOST_NAME");
					String kernelSystem = results.getString("KERNEL_NAME");
					String soaDomain = results.getString("DOMAIN");
					SOAKernelDetails soaKernelDetails = new SOAKernelDetails(hostName, kernelSystem, soaDomain);
					
					soaKernelsFromDb.add(soaKernelDetails);
				}
			}
		}catch (SQLException sqle){
			log.debug("Error !!populateSOAKernelDetails : SQLException :",sqle);
		}catch (Exception e){
			log.debug("Error !!populateSOAKernelDetails : Exception : ",e);
		}
		finally{
			close(results,stmt,con);
		}
		
		log.debug("soaKernelsFromDb size -> " + soaKernelsFromDb.size());
		return soaKernelsFromDb;
	}
	
	public void insertIntoPortDetailTable(SoaFunctionBlock soaFunctionBlock) {
		log.debug("insertIntoPortDetailTable ++");
		
		Connection con=null;
		ResultSet results=null;
		PreparedStatement pstmt  = null;
		try
		{
			con=getDBConnection();
			if (con != null)
			{
				String selection = "INSERT INTO NE_PORTDETAILS_TABLE (NE_NAME,FB_LABEL,FB_NAME,FB_TYPE,FB_INSTANCE,SLOT,TECHNOLOGY,ACTUAL_EQIP,NOMINAL_EQIP,PORT_ILOOP,PORT_ELOOP,ADMIN_STATUS,OPER_STATUS) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";			
				pstmt=con.prepareStatement(selection);
				pstmt.setString(1, soaFunctionBlock.getNob().name());
				pstmt.setString(2, soaFunctionBlock.getLabel());
				pstmt.setString(3, soaFunctionBlock.getName());
				pstmt.setString(4, Integer.toString(soaFunctionBlock.getFbType()));
				pstmt.setString(5, Integer.toString(soaFunctionBlock.getFbInstance()));
				pstmt.setString(6, Integer.toString(soaFunctionBlock.getSlotPosition()));
				pstmt.setString(7, soaFunctionBlock.getTechnology());
				pstmt.setString(8, soaFunctionBlock.isActuallyEquipped());
				pstmt.setString(9, soaFunctionBlock.isNominallyEquipped() ? "YES" : "NO");
				pstmt.setString(10, Integer.toString(soaFunctionBlock.getPortILoop()));
				pstmt.setString(11, Integer.toString(soaFunctionBlock.getPortELoop()));
				pstmt.setString(12, soaFunctionBlock.getAdminStatus() == 0 ? "ACTIVE" : soaFunctionBlock.getAdminStatus() == 1 ? "INACTIVE" : "UNKNOWN");
				pstmt.setString(13, soaFunctionBlock.getOperationalState() == 0 ? "ACTIVE" : soaFunctionBlock.getOperationalState() == 1 ? "INACTIVE" : "UNKNOWN");
				
				int row = pstmt.executeUpdate();
			}
			else
			{
				log.debug("insertIntoPortDetailTable -> Database NOT connected !!!");
			}
		}
		catch (SQLException sqle)
		{
			log.debug("NE_PORTDETAILS_TABLE DB Connection : SQLException :",sqle);
		}
		catch (Exception e)
		{
			log.debug("NE_PORTDETAILS_TABLE DB Connection : Exception :",e);
		}
		finally
		{
			close(results,pstmt,con);
		}
	}
	
	public List<InventoryModel> getNeInventoryList() {
		log.debug("getNeInventoryList");

		List<InventoryModel> list = new ArrayList<InventoryModel> ();

		Connection con=null;
		Statement stmt=null;
		ResultSet results=null;
		try
		{
			con=getDBConnection();
			if(con != null){
				stmt=con.createStatement();

//				String selection = "SELECT DISTINCT TYPE, NE_IP_ADDRESS, NE_NAME, NE_LOCATION FROM NE_INVENTORY_TABLE ORDER BY TYPE";
				String selection = "SELECT DISTINCT TYPE, NE_IP_ADDRESS, NE_NAME, NE_LOCATION, UPDATE_TIME FROM NE_INVENTORY_TABLE a WHERE a.UPDATE_TIME = (SELECT MAX(UPDATE_TIME) FROM NE_INVENTORY_TABLE b WHERE b.TYPE=a.TYPE AND b.NE_IP_ADDRESS=a.NE_IP_ADDRESS AND b.NE_NAME=a.NE_NAME AND b.NE_LOCATION=a.NE_LOCATION) ORDER BY TYPE";
				
				log.debug(Constants.SEL_QUERY_PRINT+selection);
				results = stmt.executeQuery(selection);

				while(results.next())
				{
					InventoryModel entry = new InventoryModel();

					entry.setType(results.getString("TYPE"));
					entry.setNeIpAddress(results.getString(Constants.NE_IP_ADDRESS));
					entry.setNeName(results.getString(Constants.NE_NAME));
					entry.setNeLocation(results.getString("NE_LOCATION"));
					entry.populateUpdTime(results.getString("UPDATE_TIME"));
					/*
					 * entry.setModuleName(results.getString("MODULE"));
					 * entry.setProdNumber(results.getString("PRODUCT_NUMBER"));
					 * entry.setRelease(results.getString("RELEASE"));
					 * entry.setItemNo(results.getString("ITEM_NO"));
					 * entry.setManufactNo(results.getString("MANUFACT_NO"));
					 * entry.setStatus(results.getString("STATUS"));
					 * entry.setRevNo(results.getString("REV"));
					 */

					/*
					 * if(results.getString("UPDATE_TIME") != null) {
					 * if(results.getString("UPDATE_TIME").length() >= 19)
					 * entry.setUpdTime(results.getString("UPDATE_TIME").substring(0, 19)); else
					 * entry.setUpdTime(results.getString("UPDATE_TIME")); }
					 */

					list.add(entry);
				}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getNeInventoryList: Inventory_DBConnection : SQLException :",sqle);
			return list;
		}
		finally{
			close(results,stmt,con);
		}
		return list;
	}

	
	//Developer :ekasaak
	public List<InventoryModel> getInventoryList(){
		
	
		log.debug(" Inside getInventoryList");
	
		 List<InventoryModel> inventoryList = new ArrayList<InventoryModel>();// add <InventoryModel>
		
		 Connection con=null;
		 Statement stmt=null;
		 ResultSet results=null;
		
		try
		{
		   con=getDBConnection();
			if(con!= null){
			stmt=con.createStatement();
			String query="SELECT * FROM NE_INVENTORY_TABLE";
			log.debug(Constants.SEL_QUERY_PRINT+query);
			results = stmt.executeQuery(query); 
	        	
			while(results.next())
			{
				InventoryModel inventoryModel = new InventoryModel();
				
                inventoryModel.setNeName(results.getString("NE_NAME"));
                inventoryModel.setNeLocation(results.getString("NE_LOCATION"));
                inventoryList.add(inventoryModel);
			
			} 
			}
		}
			catch(SQLException e) {
	            log.debug("Error !! getInventoryList: Inventory_DBConnection :SQLException:",e);
	           return inventoryList;
			}
		finally {
			close(results,stmt,con);
		}
		return inventoryList;
			
	}
		
	//Developer: ekasaak
	// updateNeName function is used to update the name of NE
	
	public void updateNeName(String neName, String newNeName) {
		
		log.debug("Inside updateNeName");
		Connection con=null;
		
		
		try
		{
			con=getDBConnection();
			if(con!= null)
			{
			 PreparedStatement stmt = con.prepareStatement("UPDATE NE_INVENTORY_TABLE SET NE_NAME=? WHERE NE_NAME=?"); 
             stmt.setString(1, newNeName);
             stmt.setString(2, neName);
             stmt.executeUpdate();

			}
		}
        catch(SQLException e)
        {
           log.debug("Error!! updateNeName query issue",e);
        }
    }
		

	public void updateNeLocation(String neName,String neLocation, String newNeLocation) {
		log.debug("Inside updateNeLocation");
		Connection con=null;
		
		
		try
		{
			con=getDBConnection();
			if(con!= null)
			{
           // String query="UPDATE NE_INVENTORY_TABLE SET NE_LOCATION = ? WHERE NE_NAME = ? AND ne_location = ?";
            PreparedStatement stmt = con.prepareStatement("UPDATE NE_INVENTORY_TABLE SET NEW_LOCATION=? WHERE NE_LOCATION=?");
            stmt.setString(1, neName);
            stmt.setString(2, neLocation);
            stmt.setString(3, newNeLocation);
            stmt.executeUpdate();
        } 
		}
			catch(SQLException e) {
				log.debug("Error!! updateNeLocation query issue",e);
        }
    }
	
	public List<InventoryModel> getNeInventoryResult(String type, String neIp, String neLocation) {
		log.debug("getNeInventoryResult : "+type+" / "+neIp+" / "+neLocation);

		List<InventoryModel> list = new ArrayList<InventoryModel> ();

		Connection con=null;
		Statement stmt=null;
		ResultSet results=null;
		try
		{
			con=getDBConnection();
			if(con != null){
				stmt=con.createStatement();

				String selection = "SELECT * FROM NE_INVENTORY_TABLE WHERE TYPE = '"+ type +"' AND NE_IP_ADDRESS = '"+ neIp +"' AND NE_LOCATION = '"+ neLocation +"'";
				log.debug(Constants.SEL_QUERY_PRINT+selection);
				results = stmt.executeQuery(selection);

				while(results.next())
				{
					InventoryModel entry = new InventoryModel();

					entry.setType(results.getString("TYPE"));
					entry.setNeIpAddress(results.getString(Constants.NE_IP_ADDRESS));
					entry.setNeName(results.getString(Constants.NE_NAME));
					entry.setNeLocation(results.getString("NE_LOCATION"));
					entry.setProdNumber(results.getString("PRODUCT_NUMBER"));
					entry.setRelease(results.getString("RELEASE"));
					entry.setItemNo(results.getString("ITEM_NO"));
					entry.setManufactNo(results.getString("MANUFACT_NO"));
					entry.setManufactYr(results.getString("MANUFACT_DATE_YEAR"));
					entry.setStatus(results.getString(Constants.STATUS));
					entry.setRevNo(results.getString("REV"));
					entry.setPosition(results.getString("POSITION"));
					if(results.getString("POLARIZATION") !=null && results.getString("POLARIZATION") != "")
						entry.setModuleName((results.getString("MODULE") + " - "+ results.getString("POLARIZATION")));
					else 
						entry.setModuleName(results.getString("MODULE"));

					/*
					 * if(results.getString("UPDATE_TIME") != null) {
					 * if(results.getString("UPDATE_TIME").length() >= 19)
					 * entry.setUpdTime(results.getString("UPDATE_TIME").substring(0, 19)); else
					 * entry.setUpdTime(results.getString("UPDATE_TIME")); }
					 */

					list.add(entry);
				}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getNeInventoryResult: Inventory_DBConnection : SQLException : ",sqle);
			return list;
		}
		finally{
			close(results,stmt,con);
		}
		return list;
	}

	//Developer: eohhgas
	public List<InventoryModel> getNeInventoryHistory(String type, String neIp) {
		log.debug("getNeInventoryHistory : "+type+" / "+neIp);

		List<InventoryModel> list = new ArrayList<InventoryModel> ();

		Connection con=null;
		ResultSet results=null;
		PreparedStatement pstmt=null;
		try
		{
			con=getDBConnection();
			if(con != null){
				
				String selection = PropertyReader.getInstance().getDbProperties().getProperty("getInventoryHistory");
				pstmt = con.prepareStatement(selection);
				pstmt.setString(1, type);
				pstmt.setString(2, neIp);
				log.debug(Constants.SEL_QUERY_PRINT+printformattedQuery(selection, type,neIp));
				results = pstmt.executeQuery();
				while(results.next())
				{
					InventoryModel entry = new InventoryModel();
					entry.setId(results.getInt("ID"));
					entry.setType(results.getString("TYPE"));
					entry.setNeIpAddress(results.getString(Constants.NE_IP_ADDRESS));
					entry.setNeName(results.getString(Constants.NE_NAME));
					entry.setProdNumber(results.getString("PRODUCT_NUMBER"));
					entry.setRelease(results.getString("RELEASE"));
					entry.setItemNo(results.getString("ITEM_NO"));
					entry.setManufactNo(results.getString("MANUFACT_NO"));
					entry.setManufactYr(results.getString("MANUFACT_DATE_YEAR"));
					entry.setStatus(results.getString(Constants.STATUS));
					entry.setRevNo(results.getString("REV"));
					entry.setPosition(results.getString("POSITION"));
					entry.setModuleName(results.getString("MODULE"));
					entry.setEvent(results.getString("EVENT"));
					entry.populateEventTime(results.getString("UPDATE_TIME"));
					list.add(entry);
				}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getNeInventoryHistory: Inventory_DBConnection : SQLException : ",sqle);
			return list;
		}
		finally{
			close(results,pstmt,con);
		}
		return list;
	}
	
	//Developer: eohhgas
	public List<InventoryModel> getFilteredInventoryHistory(String type, String neIp, Date startDateTimeFilter, Date endDateTimeFilter) {
		log.debug("getFilteredInventoryHistory : "+type+" / "+neIp+" / "+startDateTimeFilter+" / "+endDateTimeFilter);

		List<InventoryModel> list = new ArrayList<InventoryModel> ();

		Connection con=null;
		ResultSet results=null;
		PreparedStatement pstmt=null;
		try
		{
			con=getDBConnection();
			if(con != null){
				
				SimpleDateFormat formatter = new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT);
				formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
				String selection = PropertyReader.getInstance().getDbProperties().getProperty("getFilteredInventoryHistory");
				pstmt = con.prepareStatement(selection);
				pstmt.setString(1, type);
				pstmt.setString(2, neIp);
				pstmt.setString(3, formatter.format(startDateTimeFilter));
				pstmt.setString(4, formatter.format(endDateTimeFilter));
				log.debug(Constants.SEL_QUERY_PRINT+printformattedQuery(selection, type,neIp,formatter.format(startDateTimeFilter),formatter.format(endDateTimeFilter)));
				results = pstmt.executeQuery();
				while(results.next())
				{
					InventoryModel entry = new InventoryModel();
					entry.setId(results.getInt("ID"));
					entry.setType(results.getString("TYPE"));
					entry.setNeIpAddress(results.getString(Constants.NE_IP_ADDRESS));
					entry.setNeName(results.getString(Constants.NE_NAME));
					entry.setProdNumber(results.getString("PRODUCT_NUMBER"));
					entry.setRelease(results.getString("RELEASE"));
					entry.setItemNo(results.getString("ITEM_NO"));
					entry.setManufactNo(results.getString("MANUFACT_NO"));
					entry.setManufactYr(results.getString("MANUFACT_DATE_YEAR"));
					entry.setStatus(results.getString(Constants.STATUS));
					entry.setRevNo(results.getString("REV"));
					entry.setPosition(results.getString("POSITION"));
					entry.setModuleName(results.getString("MODULE"));
					entry.setEvent(results.getString("EVENT"));
					entry.populateEventTime(results.getString("UPDATE_TIME"));
					list.add(entry);
				}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getFilteredInventoryHistory: Inventory_DBConnection : SQLException : ",sqle);
			return list;
		}
		finally{
			close(results,pstmt,con);
		}
		return list;
	}
	//Developer: eohhgas
	public boolean deleteHistory(int id) {
		log.debug("deleteHistory for : " + id);

		Connection con = null;
		PreparedStatement pstmt = null;
		String selection = "";
		try {
			con = getDBConnection();
				selection = PropertyReader.getInstance().getDbProperties().getProperty("delInventoryHistory");
				pstmt = con.prepareStatement(selection);
				pstmt.setInt(1,id);
				
				log.debug("deleteHistory delete query: "
						+ printformattedQuery(selection,String.valueOf(id)));
			
			int row = pstmt.executeUpdate();
			return true;
		} catch (SQLException sqle) {
			log.error("deleteHistory: Inventory_DBConnection : SQLException :", sqle);
			return false;
		} finally {
			close(null, pstmt, con);
		}
	}
	
	@Override
	public String getAdapterHostIp(String adapterType, String adapterNe) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getAdapterPort(String adapterType, String adapterNeIp) {
		log.debug("getAdapterPort ++");
		log.debug("adapterType -> " + adapterType);
		log.debug("adapterNeIp -> " + adapterNeIp);
		
		String retValue = Constants.UNKNOWN_STRING;
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet results=null;
		String selection = null;

		try{
			con=getDBConnection();			
			selection = PropertyReader.getInstance().getDbProperties().getProperty("getkernelInstance");
			pstmt = con.prepareStatement(selection);
			pstmt.setString(1, adapterType);
			pstmt.setString(2, adapterNeIp);
			log.debug(Constants.SEL_QUERY_PRINT+printformattedQuery(selection, adapterType,adapterNeIp));
			results = pstmt.executeQuery();
			while (results.next()){				
				retValue = results.getString("KERNEL_INSTANCE");
				break;
			}
			log.debug("Kernel Instance -> "+retValue);
			
			String[] kernelAndID = retValue.split("-");
			log.debug("kernelAndID ->" + Arrays.toString(kernelAndID));
			
			if(kernelAndID[1] == null || kernelAndID[1].isBlank()) {
				log.debug("SOME ISSUE FOUND IN RETRIEVING THE KERNEL ID !!!! CHECK !!!!");
			} else {
				retValue = fetchAdapterPortFromID(kernelAndID[1], con);
			}
		}catch (Exception e){
			log.debug("Error !!getAdapterPort : Adapter_DBConnection : Exception : ", e);
			return retValue;
		}finally{
			close(results,pstmt,con);
		}
		return retValue;
	}
	
	/**
	 * This method retrieves the specific Kernel's Port from the Id provided via DB query to KERNEL_SYSTEMS_TABLE
	 * 
	 * @author esthdeb
	 * @param id
	 * @param con
	 * @return
	 */
	public String fetchAdapterPortFromID(String id, Connection con) {
		log.debug("fetchAdapterPortFromID ++");
		log.debug("id -> " + id);
		
		String retValue = Constants.UNKNOWN_STRING;
		PreparedStatement pstmt=null;
		ResultSet results=null;
		String selection = null;

		try{		
			selection = PropertyReader.getInstance().getDbProperties().getProperty("getkerneIPort");
			pstmt = con.prepareStatement(selection);
			pstmt.setString(1, id);
			log.debug(Constants.SEL_QUERY_PRINT+printformattedQuery(selection, id));
			results = pstmt.executeQuery();
			while (results.next()){				
				retValue = results.getString("PORT");
				break;
			}
			log.debug("Kernel Port -> "+retValue);
			
		}catch (Exception e){
			log.debug("Error !!fetchAdapterPortFromID : Adapter_DBConnection : Exception : ", e);
			return retValue;
		}finally{
			try { 
				if (results != null) results.close(); 
			} catch (SQLException e) {
				log.error("Exception in closing resultset");
			};
			try { 
				if (pstmt != null) pstmt.close(); 
			} catch (SQLException e) {
				log.error("Exception in closing statement");
			};
		}
		return retValue;
	}


	@Override
	public String getAdapterStatus(String adapterType, String adapterHostIp) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getAdapterNeName(String adapterType, String adapterHostIp) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getAdapterNeStatus(String adapterType, String adapterHostIp, String adapterNe) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getOnlineNeIpAddress(AdapterInfo adapter) {
		log.debug("getOnlineNeIpAddress : "+adapter.getAdapterType() + "/" + adapter.getAdapterHostIp());

		String neIp = "Unknown";

		Connection con=null;
		Statement stmt=null;
		ResultSet results=null;
		String selection = null;
		try
		{
			con=getDBConnection();
			if(con != null){
				stmt=con.createStatement();

				selection = "SELECT * FROM NE_ENTRY_TABLE WHERE SCAN_TYPE = '"+ adapter.getAdapterType() + "' AND SCAN_HOST_IP = '" + adapter.getAdapterHostIp()
								+ "' AND KERNEL_INSTANCE = '" + adapter.getAdapterInstance() +"' AND NE_STATUS = 'online'";
				log.debug(Constants.SEL_QUERY_PRINT+selection);
				results = stmt.executeQuery(selection );

				while(results.next())
				{
					neIp = results.getString(Constants.NE_IP_ADDRESS);					
					break;
				}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getOnlineNeIpAddress: Inventory_DBConnection : SQLException :",sqle);
			return neIp;
		}
		finally{
			close(results,stmt,con);
		}
		log.debug("getOnlineNeIpAddress : neIp = "+neIp);
		return neIp;
	}

	//Developer: eohhgas
	public String getOnlineNeIpAddress(AdapterInfo adapter,List<String> selectedIp) {
		log.debug("getOnlineNeIpAddress : "+adapter.getAdapterType() + "/" + adapter.getAdapterHostIp());

		String neIp = "Unknown";
		List<String> neIpFromResultSet = new ArrayList<>();
		Connection con=null;
		//Statement stmt=null;
		PreparedStatement pstmt=null;
		ResultSet results=null;
		String selection = null;
		try
		{
			con=getDBConnection();
				if(con != null){
					selection = PropertyReader.getInstance().getDbProperties().getProperty("getSelectedOnlineNeIp");
					log.debug("selection : "+selection);
					pstmt = con.prepareStatement(selection);
					pstmt.setString(1, adapter.getAdapterType());
					pstmt.setString(2, adapter.getAdapterHostIp());
					pstmt.setString(3, adapter.getAdapterInstance());
					pstmt.setString(4, Constants.ONLINE);
					log.debug(Constants.SEL_QUERY_PRINT+printformattedQuery(selection,adapter.getAdapterType(),adapter.getAdapterHostIp(), adapter.getAdapterInstance()));
					results = pstmt.executeQuery();
					log.debug("results : "+results);
			
					if(results!=null )
					{
						while(results.next())
						{
							neIpFromResultSet.add(results.getString(Constants.NE_IP_ADDRESS));
						}
					}
					log.debug("getOnlineNeIpAddress : neIpFromResultSet = "+neIpFromResultSet);
					
					if(neIpFromResultSet!=null && !neIpFromResultSet.isEmpty())
					{
						for(String str : selectedIp)
						{
							for(String ip:neIpFromResultSet) 
							{
								if(StringUtils.equals(str,ip))
								{
									neIp=str;
									break;
								}
							}	
						}
					}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getOnlineNeIpAddress: Inventory_DBConnection : SQLException :",sqle);
			return neIp;
		}
		finally{
			close(results,pstmt,con);
		}
		log.debug("getOnlineNeIpAddress : neIp = "+neIp);
		return neIp;
	}
	
	//Developer: eohhgas
	public List<String> getNeIpsPerInstance(List<InventoryModel> selectedNePerAdapterType,AdapterInfo adapter) {
		
		List<String> selectedNeIpPerInstance = new ArrayList<>();
		List<String> neIpFromResultSet = new ArrayList<>();
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet results=null;
		String selection = null;
		try {
			con=getDBConnection();
			if(con != null){
				selection = PropertyReader.getInstance().getDbProperties().getProperty("getNeMappedWithKernelInstance");
				log.debug("selection : "+selection);
				pstmt = con.prepareStatement(selection);
				pstmt.setString(1, adapter.getAdapterInstance());
				log.debug(Constants.SEL_QUERY_PRINT+printformattedQuery(selection, adapter.getAdapterInstance()));
				results = pstmt.executeQuery();
				log.debug("results : "+results);
				
				if(results!=null )
				{
					while(results.next())
					{
						neIpFromResultSet.add(results.getString(Constants.NE_IP_ADDRESS));
					}
				}
				log.debug("getNeIpsPerInstance : neIpFromResultSet = "+neIpFromResultSet);
				
				if(neIpFromResultSet!=null && !neIpFromResultSet.isEmpty())
				{
					for(InventoryModel obj : selectedNePerAdapterType)
					{
						for(String ip:neIpFromResultSet) 
						{
							if(StringUtils.equals(obj.getNeIpAddress(),ip))
							{
								selectedNeIpPerInstance.add(obj.getNeIpAddress());
							}
						}	
					}
				}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getNeIpsPerInstance: Inventory_DBConnection : SQLException :",sqle);
			return selectedNeIpPerInstance;
		}
		finally{
			close(results,pstmt,con);
		}
		log.debug("getNeIpsPerInstance : selectedNeIpPerInstance = "+selectedNeIpPerInstance);
		return selectedNeIpPerInstance;
	}
	
	public void deleteInventoryRecords() {
		log.debug("deleteInventoryRecords");
		Connection con=null;
		PreparedStatement pstmt=null;
		ResultSet results=null;
		String selection = PropertyReader.getInstance().getDbProperties().getProperty("delete_inv");
		try {
			con=getDBConnection();
//			stmt=con.createStatement();
//
//			selection = "DELETE FROM NE_INVENTORY_TABLE";
//			log.debug ("Database All entry deleted: " + selection);
//			stmt.executeQuery(selection );
			pstmt=con.prepareStatement(selection);
			int row = pstmt.executeUpdate();
			log.debug ("Database All entry deleted sql is: " + selection+ " and no of rows deleted is/are = "+row);
		}catch (SQLException sqle){
			log.debug("Error !!deleteInventoryRecords : SQLException :",sqle);
		}catch (Exception e){
			log.debug("Error !!deleteInventoryRecords : Exception : ",e);
		}
		finally{
			close(results,pstmt,con);
		}
		
	}

	public void deleteInventoryRecordsForAdapter(String adapter) {
		log.debug("deleteInventoryRecords");
		Connection con=null;
		Statement stmt=null;
		ResultSet results=null;
		String selection = "";
		try {
			con=getDBConnection();
			stmt=con.createStatement();

			selection = "DELETE FROM NE_INVENTORY_TABLE WHERE TYPE = '"+adapter+"'";
			log.debug ("Database entry deleted for adapter: " + selection);
			stmt.executeQuery(selection );

		}catch (SQLException sqle){
			log.debug("Error !!deleteInventoryRecordsForAdapter : SQLException :",sqle);
		}catch (Exception e){
			log.debug("Error !!deleteInventoryRecordsForAdapter : Exception : ",e);;
		}
		finally{
			close(results,stmt,con);
		}
		
	}
	
	/**
	 * DB call to get last updated time for that particular
	 * ne ip of a adapter.
	 * @param adapterType
	 * @param neIp
	 * @param neName
	 * @param neLoc
	 * @return latest Timestamp.
	 */
	public Timestamp getLatestUpdateTimeForNeIp(String adapterType,String neIp, String neName,String neLoc) {
		log.debug("Inventory DB Connection getLatestUpdateTimeForNeIp neIp= "+neIp+" / neName="+neName+" /neLoc="+neLoc+" /adapterType="+adapterType);
		Timestamp latestUpdatedTime = null;

		Connection con=null;
		Statement stmt=null;
		ResultSet results=null;
		String selection = null;
		try
		{
			con=getDBConnection();
			if(con != null){
				stmt=con.createStatement();
				selection = "SELECT MAX(UPDATE_TIME)  as LAST_UPDATED_TIME FROM NE_INVENTORY_TABLE where TYPE= '"+ adapterType +
						"' AND NE_IP_ADDRESS = '" +neIp +"' AND NE_NAME = '"+neName+"' AND NE_LOCATION = '"+
						neLoc+"'  ORDER BY UPDATE_TIME";
				log.debug(Constants.SEL_QUERY_PRINT+selection);
				results = stmt.executeQuery(selection );
				while(results.next()) {
					latestUpdatedTime = results.getTimestamp("LAST_UPDATED_TIME");					
				}
			}
		}
		catch (SQLException sqle)
		{
			log.debug("Error !!getLatestUpdateTimeForNeIp: Inventory_DBConnection : SQLException :",sqle);
			return latestUpdatedTime;
		}
		finally{
			close(results,stmt,con);
		}
		log.debug("Inventory DB Connection getLatestUpdateTimeForNeIp latestUpdatedTime = "+latestUpdatedTime);
		return latestUpdatedTime;
	}

	@Override
	public String getAdapterStatus(String adapterType, String adapterHostIp, String port) {
		return null;
	}
	
	public List<InventoryImportBean> getInventoryResultForCSV(){
		String selection = PropertyReader.getInstance().getDbProperties().getProperty("readNeInventoryDatabase");
		Connection con=null;
		ArrayList<Object> params = new ArrayList<>();
		List<InventoryImportBean>inventoryList=new ArrayList<InventoryImportBean>();
		
		try
		{
			con=getDBConnection();
			if(con!=null) {
			inventoryList=DBUtility.queryPreparedStatement(con,selection, 
					new BeanListHandler<InventoryImportBean>(InventoryImportBean.class),params);
			}
		}
		catch (SQLException sqle)
		{
			log.debug("SqlException Occcured : ",sqle);
		}
		finally{
			close(null,null,con);
		}
		
		return inventoryList;
	}
	
	public void insertInventoryDataBulk(List<InventoryImportBean> inventoryList) throws SQLException{
		Connection con=null;
		String insertInventorySql = PropertyReader.getInstance().getDbProperties().getProperty("insertDataInventory");
		
		try {
			con=getDBConnection();
			Object[][] params = inventoryList.stream()
                    .map(rec -> new Object[] {
                    		rec.getId(),
                    		rec.getType(),
                    		rec.getNe_ip_address(),
                    		rec.getNe_name(),
                    		rec.getNe_location(),
                    		rec.getModule(),
                    		rec.getProduct_number(),
                    		rec.getRelease(),
                    		rec.getItem_no(),
                    		rec.getManufact_no(),
                    		rec.getStatus(),
                    		rec.getRev(),
                    		rec.getPosition(),
                    	//	rec.getPolarization() Not required.
                    		rec.getManufact_date_year()})
                    .toArray(Object[][]::new);
			DBUtility.batch(con, insertInventorySql, params);
		}
		catch(SQLException sqlEx) {
			log.error("SQLException occured while Inserting bulk Inventory Data",sqlEx);
		}
		finally {
			close(null,null,con);
		}
	}
	
	public NeDetailsModel getNeDetails(String ipAddress, String neName) {
		
		NeDetailsModel neDetailsResult = new NeDetailsModel();
		Connection con=null;
		String selection = PropertyReader.getInstance().getDbProperties().getProperty("getNeDataForNEDetails");
		ResultSet results=null;
		PreparedStatement pstmt  = null;
		
		try {
			con=getDBConnection();
			pstmt=con.prepareStatement(selection);
			pstmt.setString(1, ipAddress);
			pstmt.setString(2, neName);
			results=pstmt.executeQuery();
			
			if(results.next()) {
				
				neDetailsResult.setNeLocation(results.getString("NE_LOCATION"));
				neDetailsResult.setNeIpAddress(results.getString("NE_IP_ADDRESS"));
				neDetailsResult.setNeName(results.getString("NE_NAME"));
				neDetailsResult.setLatitude(results.getString("LATITUDE"));
				neDetailsResult.setLongitutde(results.getString("LONGITUDE"));
				neDetailsResult.setNeStatus(results.getString("NE_STATUS"));
				if(results.getString("UPDATE_TIME") != null) {
					 if(results.getString("UPDATE_TIME").length() >= 19)
						 neDetailsResult.setUpdTime(results.getString("UPDATE_TIME").substring(0, 19)); 
					 else
						 neDetailsResult.setUpdTime(results.getString("UPDATE_TIME")); 
					 }
			}
			
		}
		catch(SQLException sqlEx) {
			log.error("SQLException occured while Inserting bulk Inventory Data",sqlEx);
		}
		finally {
			close(results,pstmt,con);
		}
		return neDetailsResult;
	}

	
}