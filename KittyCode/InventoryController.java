package com.ericsson.so.networklayer.inventoryservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.faces.event.ActionEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.file.UploadedFile;

import com.ericsson.so.networklayer.BaseController;
import com.ericsson.so.networklayer.Constants;
import com.ericsson.so.networklayer.NlsMessageHandler;
import com.ericsson.so.networklayer.Pages;
import com.ericsson.so.networklayer.NWH_Handler.NWHModel;
import com.ericsson.so.networklayer.UserSession.UserSession;
import com.ericsson.so.networklayer.Utils.CsvUtility;
import com.ericsson.so.networklayer.Utils.ThreadMsgHandler;
import com.ericsson.so.networklayer.Utils.Utils;
import com.ericsson.so.networklayer.database.Inventory_DBConnection;
import com.ericsson.so.networklayer.database.Inventory_DBConnection.AdapterInfo;
import com.ericsson.so.networklayer.isis.entry.IsisModel;
import com.ericsson.so.networklayer.logbook.LogbookController;
import com.ericsson.so.networklayer.logbook.LogbookHandler;
import com.ericsson.so.networklayer.logbook.LogbookModel;
import com.ericsson.so.networklayer.restclient.AdapterReqServiceController;
import com.ericsson.so.networklayer.restclient.model.AdapterRequestModel.Operation;
import com.ericsson.so.networklayer.restclient.model.InventoryServiceModel;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

/**
 * @author Shubhadip Bera
 * 
 * 
 **/

@SessionScoped
@ManagedBean
public class InventoryController extends BaseController {
	private static final Logger log = Logger.getLogger(InventoryController.class.getName());
	NlsMessageHandler messages = NlsMessageHandler.getMsgInstance();

	private List<InventoryModel> neInventoryList = new ArrayList<InventoryModel>();
    private List<InventoryModel> selectedNeInventoryList = new ArrayList<InventoryModel>();// use this
	private List<InventoryModel> neInventoryResult = new ArrayList<InventoryModel>();
    private List<InventoryModel> selectedNeInventoryResult = new ArrayList<InventoryModel>();

	private List<InventoryImportBean> importedInventoryBeans = new ArrayList<InventoryImportBean>();
	private UploadedFile uploadedFile;
	private String inventResultNeIp = "";
	private String userWarningMsg = new String();
	private String userAction = new String();

	//ekasaak

	public InventoryModel  selectedNeInventoryList1 ;
	
	/**
	 * @return the selectedNeInventoryList1
	 */
	public InventoryModel getSelectedNeInventoryList1() {
		return selectedNeInventoryList1;
	}
	/**
	 * @param selectedNeInventoryList1 the selectedNeInventoryList1 to set
	 */
	public void setSelectedNeInventoryList1(InventoryModel selectedNeInventoryList1) {
		this.selectedNeInventoryList1 = selectedNeInventoryList1;
	}

	//public List<InventoryModel> selectedInventoryList=new ArrayList<InventoryModel>();// C2 whole thing is added
	private String newInterfaceNameFromUser;
    private String newInterfaceLocationFromUser;
    private Inventory_DBConnection inventoryDbConnection;
	
    /**
	 * @return the selectedInventory
	 *
	private InventoryModel getSelectedInventory() {
		return selectedInventory;
	}
	/**
	 * @param selectedInventory the selectedInventory to set
	 */
	/*public void setSelectedInventory(InventoryModel selectedInventory) {
		
		log.debug("Inside setselectedInventory");
		this.selectedInventory = selectedInventory;
		
		//changes added here C3
		// update the selectedNeInventoryList when a new InventoryModel is selected
		
		selectedInventoryList=new ArrayList<>();
		for(InventoryModel inventory : inventoryList) {
			if(inventory.getNeName().equals(selectedInventory.getNeName())) {
				selectedInventoryList.add(inventory);
			
		}
	}
comment stops
	/**
	 * @return the selectedInventoryList
	 *
	public List<InventoryModel> getSelectedInventoryList() {
		return selectedInventoryList;
	}
	/**
	 * @param selectedInventoryList the selectedInventoryList to set
	 *
	public void setSelectedInventoryList(List<InventoryModel> selectedInventoryList) {
		this.selectedInventoryList = selectedInventoryList;
	}

	

	/**
	 * @return the inventoryList
	 */
	/*public List<InventoryModel> getInventoryList() {
		return inventoryList;
	}
	/**
	 * @param inventoryList the inventoryList to set
	 *
	public void setInventoryList(List<InventoryModel> inventoryList) {
		this.inventoryList = inventoryList;
	}
	
/
	
    
	

	
    /**
	 * @return the newInterfaceNameFromUser
	 */
	public String getNewInterfaceNameFromUser() {
		return newInterfaceNameFromUser;
	}


	/**
	 * @param newInterfaceNameFromUser the newInterfaceNameFromUser to set
	 */
	public void setNewInterfaceNameFromUser(String newInterfaceNameFromUser) {
		this.newInterfaceNameFromUser = newInterfaceNameFromUser;
	}


	/**
	 * @return the newInterfaceLocationFromUser
	 */
	public String getNewInterfaceLocationFromUser() {
		return newInterfaceLocationFromUser;
	}


	/**
	 * @param newInterfaceLocationFromUser the newInterfaceLocationFromUser to set
	 */
	public void setNewInterfaceLocationFromUser(String newInterfaceLocationFromUser) {
		this.newInterfaceLocationFromUser = newInterfaceLocationFromUser;
	}

	
	
    //ekasaak
    
   
	public InventoryController() {
		
		log.debug("InventoryController....");
		inventoryDbConnection=new Inventory_DBConnection();
		neInventoryList=inventoryDbConnection.getInventoryList();
		
	}

	@Override
	public String loadPage() {
		log.debug("Entered loadPage");
		try {
			super.loadPage();
			if (!isAdminCurrentUser()) {
				// return to the non-admin dashboard page
				return Pages.DASHBOARD_PAGE + Constants.FACES_REDIR_TRUE;
			}

			if (selectedNeInventoryList.size() > 0)
				selectedNeInventoryList.clear();
			// clearselectedNeInventoryList for loading Page

			if (neInventoryList.size() > 0)
				neInventoryList.clear();
			// clear the older neInventoryList and reload it using
			// retrieveNeInventoryList();

			retrieveNeInventoryList(); // retreives NEInventoryList from database.
		} catch (Exception e) {
			handleError(e);
		}

		log.debug("Exit loadPage");
		return Pages.INVENTORY_LIST_PAGE + Constants.FACES_REDIR_TRUE;
		// Pages.("/inventory/inventoryHistory.xhtml") + Constants.=
		// ("?faces-redirect=true");
	}

	/**
	 * @Developer eohhgas
	 **/
	public void initInventoryScan() { // initialize Inventory

		log.debug("initInventoryScan");

		Set<AdapterInfo> adapters = null; // set contains no duplicate elements

		Inventory_DBConnection dbConn = null;
		try {
			ExecutorService exservice = Executors.newFixedThreadPool(6);

			/*
			 * creates a new ExecutorService object that is configured to use a thread pool
			 * of 6 threads, which can be used to execute tasks asynchronously in a Java
			 * program.
			 */
			dbConn = new Inventory_DBConnection(); // Inventory_DBConnection dbConn = new Inventory_DBConnection();

			adapters = dbConn.availableListFromKerneltable();

			if (adapters == null) {
				log.debug("adapters is NULL !!!!");
				return;
			}
			log.debug("adapters size -> " + adapters.size());

			for (AdapterInfo adapter : adapters) {

				log.debug("adapter type -> " + adapter.getAdapterType() + ", adapter instance -> "
						+ adapter.getAdapterInstance() + ",adapter hostIP -> " + adapter.getAdapterHostIp());
				try {
					LogbookModel lbm = LogbookHandler.getLogbookInstance().prepareLogbookEntry(
							messages.getString("hInventoryScan"), adapter.getAdapterType(),
							Constants.LOGBOOK_ACTION_NE_INVENTORY, Constants.LOGBOOK_SUCCESS,
							Utils.getLocaleOfHttpRequest(), ThreadMsgHandler.getmOperTrigger(), "");

					if (selectedNeInventoryList.size() > 0
							&& selectedNeInventoryList.size() != neInventoryList.size()) {

						List<InventoryModel> selectedNePerAdapterType = new ArrayList<InventoryModel>();

						log.debug("Size of selected list : " + selectedNeInventoryList.size());
						for (InventoryModel obj : selectedNeInventoryList) {

							if (StringUtils.equals(obj.getType(), adapter.getAdapterType())) {
								selectedNePerAdapterType.add(obj);
							}
						}
						log.debug("Selected Ne per adapter : " + selectedNePerAdapterType);

						if (selectedNePerAdapterType == null || selectedNePerAdapterType.isEmpty())
							continue;

						List<String> selectedNeIpPerInstance = dbConn.getNeIpsPerInstance(selectedNePerAdapterType,
								adapter);

						if (selectedNeIpPerInstance == null || selectedNeIpPerInstance.isEmpty())
							continue;

						ActionService2 srvc = new ActionService2(adapter, selectedNeIpPerInstance, lbm);
						exservice.execute(srvc);
					} else {
						ActionService srvc = new ActionService(adapter, lbm);
						exservice.execute(srvc);
					}
				} catch (Exception e) {
					handleError(e);
				}
			}
			// Recommended to use shutdown().
			// So that Thread will completes the current tasks but did not add any new task
			// for execution
			exservice.shutdown();
		} catch (Exception e) {
			handleError(e);
		} finally {
			if (dbConn != null)
				dbConn = null;
		}
	}

	// Developer: eshuber
	// Class Details: Service class is used to prepare the Threads for the Thread
	// executer service
	// to call actionEntry() methods in different Thread.
	//
	class ActionService implements Runnable {
		AdapterInfo adapter;
		LogbookModel lbm = new LogbookModel();

		public ActionService(AdapterInfo adapter, LogbookModel lbm) {
			this.adapter = adapter;
			this.lbm = lbm;
		}

		@Override
		public void run() {
			Thread.currentThread().setName(Constants.THREAD_ADAPTER_ACTION_ENTRY + "_" + adapter.getAdapterType() + "_"
					+ adapter.getAdapterHostIp() + "_" + adapter.getAdapterInstance());
			try {
				log.debug("Executing inventoryScan() method in Thread with : " + adapter.getAdapterType() + "/"
						+ adapter.getAdapterHostIp() + "/" + adapter.getAdapterInstance());
				log.debug("====================================================================================");
				inventoryScan(adapter, lbm);

			} catch (Exception e) {
				log.debug("Error !!Exception in inventoryScan() request:", e);
			}
		}
	}

	// Developer: eohhgas
	class ActionService2 implements Runnable {
		AdapterInfo adapter;
		LogbookModel lbm = new LogbookModel();
		List<String> selectedIp = new ArrayList<String>();

		public ActionService2(AdapterInfo adapter, List<String> selectedIp, LogbookModel lbm) {
			this.adapter = adapter;
			this.selectedIp = selectedIp;
			this.lbm = lbm;
		}

		@Override
		public void run() {
			Thread.currentThread().setName(Constants.THREAD_ADAPTER_MODIFIED_ACTION_ENTRY + "_"
					+ adapter.getAdapterType() + "_" + adapter.getAdapterHostIp() + "_" + adapter.getAdapterInstance());
			try {
				log.debug("Executing inventoryScan() method with selected Nes in Thread with : "
						+ adapter.getAdapterType() + "/" + adapter.getAdapterHostIp() + "/"
						+ adapter.getAdapterInstance() + "/" + selectedIp);
				log.debug("====================================================================================");
				inventoryScan(adapter, selectedIp, lbm);

			} catch (Exception e) {
				log.debug("Error !!Exception in inventoryScan() with selected Nes request:", e);
			}
		}
	}

	// Developer: eshuber
	// Functional Details: actionEntry() is used to trigger a Health operational
	// request for individual Adapter entry
	//
	// Result: na
	private void inventoryScan(AdapterInfo adapter, LogbookModel lbm) {
		log.debug("inventoryScan : " + adapter.getAdapterType() + " - " + adapter.getAdapterHostIp() + " / " + lbm);

		InventoryServiceModel serviceMessage = null;
		Inventory_DBConnection dbConn = null;
		try {
			dbConn = new Inventory_DBConnection();
			String reqAdapterNeIp = dbConn.getOnlineNeIpAddress(adapter);
			// List<String> reqAdapterNeIps = dbConn.getOnlineNeIpAddress(adapter);
			// for(String reqAdapterNeIp : reqAdapterNeIps) {
			// lbm.setEntryRef(lbm.getEntryRef()+":"+reqAdapterNeIp);
			log.debug("inventoryScan >> for Adapter = " + adapter.getAdapterType() + ", Adapter Instance ="
					+ adapter.getAdapterInstance() + ",sample NE Ip = " + reqAdapterNeIp + ", lbm.getEntryRef()="
					+ lbm.getEntryRef());
			serviceMessage = new InventoryServiceModel(adapter.getAdapterType());

			AdapterReqServiceController.getAdapterReqHandler().submitAdapterRequest(adapter.getAdapterType(),
					reqAdapterNeIp, "NE_INVENTORY_TABLE", Operation.INVENTORY, serviceMessage, lbm);
			// lbm.setEntryRef(adapter.getAdapterType());
			// }
		} catch (Exception e) {
			log.debug("Error !!InventoryControler: Exception in inventoryScan : ", e);
			/***********************************************************************************************/
			lbm.setOperStatus(Constants.LOGBOOK_FAIL);
			lbm.setOperResult(ThreadMsgHandler.getmExcpOcrd());
			lbm.setOperExtResult(e.toString());
			LogbookHandler.getLogbookInstance().prepareLogbookEntryFromModel(lbm);
			/*******************
			 * M
			 ****************************************************************************/
		} finally {
			if (serviceMessage != null)
				serviceMessage = null;

			if (dbConn != null)
				dbConn = null;

		}
	}

	// Developer: eohhgas
	private void inventoryScan(AdapterInfo adapter, List<String> selectedIp, LogbookModel lbm) {
		log.debug("inventoryScan with selected Nes : " + adapter.getAdapterType() + " - " + adapter.getAdapterHostIp()
				+ " / " + lbm + " / " + selectedIp);

		InventoryServiceModel serviceMessage = null;
		Inventory_DBConnection dbConn = null;
		try {
			dbConn = new Inventory_DBConnection();

			String reqAdapterNeIp = dbConn.getOnlineNeIpAddress(adapter, selectedIp);
			log.debug("reqAdapterNeIp : " + reqAdapterNeIp);

			if (StringUtils.isBlank(reqAdapterNeIp))
				return;

			log.debug("inventoryScan with selected Nes >> for Adapter = " + adapter.getAdapterType()
					+ ", Adapter Instance =" + adapter.getAdapterInstance() + ",selected NE Ip = " + selectedIp
					+ ", reqAdapterNeIp= " + reqAdapterNeIp + ", lbm.getEntryRef()=" + lbm.getEntryRef());
			serviceMessage = new InventoryServiceModel(adapter.getAdapterType(), adapter.getAdapterInstance(),
					selectedIp);

			AdapterReqServiceController.getAdapterReqHandler().submitAdapterRequest(adapter.getAdapterType(),
					reqAdapterNeIp, "NE_INVENTORY_TABLE", Operation.SELECTED_INVENTORY, serviceMessage, lbm);

		} catch (Exception e) {
			log.debug("Error !!InventoryControler: Exception in inventoryScan with selected Nes: ", e);
			/***********************************************************************************************/
			lbm.setOperStatus(Constants.LOGBOOK_FAIL);
			lbm.setOperResult(ThreadMsgHandler.getmExcpOcrd());
			lbm.setOperExtResult(e.toString());
			LogbookHandler.getLogbookInstance().prepareLogbookEntryFromModel(lbm);
			/*******************
			 * M
			 ****************************************************************************/
		} finally {
			if (serviceMessage != null)
				serviceMessage = null;

			if (dbConn != null)
				dbConn = null;

		}
	}

	public String refreshPage() {
		log.debug("Entered Inventory refreshPage....");
		try {
			if (neInventoryList.size() > 0)
				neInventoryList.clear();

			retrieveNeInventoryList();
		} catch (Exception e) {
			log.debug("Error !!Exception in Inventory refreshPage : ", e);
		}
		return Pages.INVENTORY_LIST_PAGE + Constants.FACES_REDIR_TRUE;
	}

	// Developer: eshuber
	// Functional Details: retrieveNeInventoryList() is used get all available NE
	// Inventory from Database and
	// add them in 'templateList' to display on UI
	//
	// Result: Na
	public void retrieveNeInventoryList() {
		log.debug("retrieveNeInventoryList");
		Inventory_DBConnection dbConn = null;
		try {
			dbConn = new Inventory_DBConnection();
			List<InventoryModel> list = dbConn.getNeInventoryList();
			neInventoryList.clear();
			log.debug("Number of rows to be displayed: " + list.size());

			neInventoryList.addAll(list);

			log.debug("Number of rows to be show in list: " + neInventoryList.size());
		} catch (Exception e) {
			log.debug("Error !!Exception in retrieveNeInventoryList : ", e);
		} finally {
			if (dbConn != null)
				dbConn = null;
		}
	}

	public String showInventoryResult(String type, String neIp, String neLocation) {
		log.debug("showInventoryResult : " + type + " / " + neIp + " / " + neLocation);
		Inventory_DBConnection dbConn = null;
		inventResultNeIp = neIp;
		try {
			dbConn = new Inventory_DBConnection();

			List<InventoryModel> list = dbConn.getNeInventoryResult(type, neIp, neLocation);
			neInventoryResult.clear();
			log.debug("Number of rows to be displayed: " + list.size());

			neInventoryResult.addAll(list);
		} catch (Exception e) {
			log.debug("Error !!Exception in showInventoryResult : ", e);
		} finally {
			if (dbConn != null)
				dbConn = null;
		}
		return Pages.INVENTORY_RESULT_PAGE + Constants.FACES_REDIR_TRUE;
	}

	public boolean isAdminCurrentUser() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		UserSession currSession = (UserSession) session.getAttribute("userSession");
		if (currSession.getUserType().equals("NWT_RADIUS"))
			return true;

		return currSession != null && currSession.isUserAdmin();
	}

	/*
	 * *****************************************************************************
	 * *********************************** All Operations related to
	 * select/un-select and Toggle Select
	 * *****************************************************************************
	 * ***********************************
	 */
	public void onRowSelect(SelectEvent event) {
		InventoryModel model = (InventoryModel) event.getObject();
		log.debug("onRowSelect -- selected row with id=" + model.getNeIpAddress() + Constants.NAME_IS
				+ model.getNeName() + Constants.SEL_SIZE_IS + selectedNeInventoryList.size());
	}

	public void onRowUnSelect(UnselectEvent event) {
		InventoryModel model = (InventoryModel) event.getObject();
		log.debug("onRowUnSelect -- unselected row with id=" + model.getNeIpAddress() + Constants.NAME_IS
				+ model.getNeName() + Constants.SEL_SIZE_IS + selectedNeInventoryList.size());
	}

	public void onRowSelectCheckBox(SelectEvent event) {
		InventoryModel model = (InventoryModel) event.getObject();
		log.debug("onRowSelectCheckBox -- selected row with id=" + model.getNeIpAddress() + Constants.NAME_IS
				+ model.getNeName() + Constants.SEL_SIZE_IS + selectedNeInventoryList.size());
	}

	public void onRowUnSelectCheckBox(UnselectEvent event) {
		InventoryModel model = (InventoryModel) event.getObject();
		log.debug("onRowUnSelectCheckBox -- selected row with id=" + model.getNeIpAddress() + Constants.NAME_IS
				+ model.getNeName() + Constants.SEL_SIZE_IS + selectedNeInventoryList.size());
	}

	public void onToggleSelect(ToggleSelectEvent event) {
		log.debug("onToggleSelect " + event.isSelected() + Constants.SEL_SIZE_IS + selectedNeInventoryList.size());
	}

	
	
	public void renameSelectedInterface() {
		
		log.debug("RenameSelectedInterface...");
	    String neName =selectedNeInventoryList1.getNeName();
	    log.debug("neName"+neName);
	    String newNeName = newInterfaceNameFromUser;
	    inventoryDbConnection.updateNeName(neName, newNeName);
	    neInventoryList = inventoryDbConnection.getInventoryList();
	    selectedNeInventoryList = null;
	    newInterfaceNameFromUser = "";
	}
	
	
	
	public void updateSelectedInterfaceLocation() {
		
		log.debug("updateSelectedInterfaceLocation.....");
	    String neName =selectedNeInventoryList1.getNeName();
	    String neLocation = selectedNeInventoryList1.getNeLocation();
	    String newNeLocation = newInterfaceLocationFromUser;
	    inventoryDbConnection.updateNeLocation(neName, neLocation, newNeLocation);
	    neInventoryList = inventoryDbConnection.getInventoryList();
	    selectedNeInventoryList = null;
	    newInterfaceLocationFromUser = null;
	}

	
//*********************
	
	// getter and setter for update function above

	public List<InventoryModel> getNeInventoryList() {
		return neInventoryList;
	}

	public String getUserAction() {
		return userAction;
	}

	public void setUserAction(String userAction) {
		this.userAction = userAction;
	}

	public String getUserWarningMsg() {
		return userWarningMsg;
	}

	public void setUserWarningMsg(String userWarningMsg) {
		this.userWarningMsg = userWarningMsg;
	}

	public void setNeInventoryList(List<InventoryModel> neInventoryList) {
		this.neInventoryList = neInventoryList;
	}

	public List<InventoryModel> getSelectedNeInventoryList() {
		return selectedNeInventoryList;
	}

	public void setSelectedNeInventoryList(List<InventoryModel> selectedNeInventoryList) {
		this.selectedNeInventoryList = selectedNeInventoryList;
	}

	public List<InventoryModel> getNeInventoryResult() {
		return neInventoryResult;
	}

	public void setNeInventoryResult(List<InventoryModel> neInventoryResult) {
		this.neInventoryResult = neInventoryResult;
	}

	public List<InventoryModel> getSelectedNeInventoryResult() {
		return selectedNeInventoryResult;
	}

	public void setSelectedNeInventoryResult(List<InventoryModel> selectedNeInventoryResult) {
		this.selectedNeInventoryResult = selectedNeInventoryResult;
	}

	public String getInventResultNeIp() {
		return inventResultNeIp;
	}

	public void setInventResultNeIp(String inventResultNeIp) {
		this.inventResultNeIp = inventResultNeIp;
	}

	// getter and setter for private InventoryModel selectedItem;

	/*
	 * private InventoryModel getSelectedItem() { return selectedItem; }
	 * 
	 * 
	 * private void setSelectedItem(InventoryModel selectedItem) { this.selectedItem
	 * = selectedItem; }
	 */

	/**
	 * It will be needed to display logbook actions for every ne scan happen for
	 * individual adapter. Currently this method is not in use as Inventory scan is
	 * done on Adapter basis only and not for all ne for a particular adapter.
	 * 
	 * @param pageName
	 * @param entryRef
	 * @return
	 */
	public String showLogbookResult(String pageName, String entryRef) {
		log.debug("Inventory Controller showLogbookResult : " + pageName + " / " + entryRef);

		LogbookController lbc = null;
		try {
			lbc = new LogbookController();
			LogbookController.reqPageName = pageName;
			LogbookController.reqEntryRef = entryRef;
			// return lbc.loadPage();
			return lbc.loadPageNWH();
		} catch (Exception e) {
			log.debug("Error !!Exception in showLastResult : ", e);
			// e.printStackTrace();
		} finally {
			// After that function all entries are not selected. => clear Selection List
//			if (this.selectedCESEndpoint != null) {
//				this.selectedCESEndpoint.clear();
//			}
			if (lbc != null)
				lbc = null;

//			this.loadPageRetainFilters();
		}
		return Pages.LOGBOOK_PAGE + Constants.FACES_REDIR_TRUE;
	}

	/**
	 * This method gets called for each row in the Inventory Scan page and display
	 * the last updated time for that particular ne ip of a adapter.
	 * 
	 * @param adapterType
	 * @param neIp
	 * @param neName
	 * @param neLoc
	 * @return strTime latest time.
	 */
	public String showLastUpdatedTime(String adapterType, String neIp, String neName, String neLoc) {
		log.debug("Inventory Controller showLastUpdatedTime neIp= " + neIp + " / neName=" + neName + " /neLoc=" + neLoc
				+ " /adapterType=" + adapterType);
		Inventory_DBConnection dbConn = null;
		Timestamp timestamp = null;
		String strTime = "";
		try {
			dbConn = new Inventory_DBConnection();
			timestamp = dbConn.getLatestUpdateTimeForNeIp(adapterType, neIp, neName, neLoc);
			if (timestamp != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				strTime = dateFormat.format(timestamp);
//				if(string != null && string.length() >= 19)
//					string = string.substring(0, 19);
			}
		} catch (Exception e) {
			log.debug("Error !!Exception in showInventoryResult : ", e);
		} finally {
			if (dbConn != null)
				dbConn = null;
		}
		log.debug("Inventory Controller lastestTime =" + strTime);
		return strTime;
	}

	public void handleCSVUpload(FileUploadEvent event) {
		log.debug("handleCSVUpload ++");
		this.uploadedFile = null;
		this.uploadedFile = event.getFile();

		if (this.uploadedFile != null) {
			log.debug("File Upload Successfull ...");
			log.debug("this.uploadedFile.getFileName() => " + this.uploadedFile.getFileName());
			validateCSV();
			Utils.addFacesMessage(FacesMessage.SEVERITY_INFO, "csvFileUploadSuccessful", true,
					this.uploadedFile.getFileName());

		} else {
			log.error("File Upload Failed !!!");
			Utils.clearCollection(this.importedInventoryBeans);
			Utils.addFacesMessage(FacesMessage.SEVERITY_ERROR, "csvFileUploadFailed", true);
		}
	}

	public void validateCSV() {
		log.debug("this.uploadedFile.getFileName() => " + this.uploadedFile.getFileName());

		Utils.clearCollection(importedInventoryBeans);
		Inventory_DBConnection dbConn = null;
		try (InputStream istream = uploadedFile.getInputStream();
				InputStreamReader reader = new InputStreamReader(istream)) {
			importedInventoryBeans = CsvUtility.prepareBeanFromCsv(reader, InventoryImportBean.class).stream()
					.map(csvBean -> (InventoryImportBean) csvBean).collect(Collectors.toList());

			log.debug("Creation of importedLspBeans is successful ... size -> "
					+ Utils.nullSafeSize(importedInventoryBeans));
			dbConn = new Inventory_DBConnection();
			dbConn.insertInventoryDataBulk(importedInventoryBeans);

		} catch (Exception ex) {
			log.error("Exception ... File Upload Failed !!!", ex);
			Utils.clearCollection(importedInventoryBeans);
			Utils.addFacesMessage(FacesMessage.SEVERITY_ERROR, "csvFileValidationFailed", false);
		}
	}

	public void saveUserInventoryFile() throws IOException {
		log.debug("saveUserInventoryFile");

		List<InventoryImportBean> inventoryListToSave = new ArrayList<InventoryImportBean>();
		inventoryListToSave = retrieveInventoryList();

		log.debug("InventoryListToSave retrieved size =  " + inventoryListToSave.size());

		String csvName = "Inventory_List.csv";
		String arguement2 = "attachment; " + "filename=" + csvName;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		externalContext.setResponseContentType("text/csv");
		externalContext.setResponseHeader("Content-Disposition", arguement2);

		OutputStream out = externalContext.getResponseOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(out);

		String[] columns = new String[] { "id", "type", "ne_ip_address", "ne_name", "ne_location", "module",
				"product_number", "release", "item_no", "manufact_no", "status", "rev", "position", "polarization",
				"update_time", "manufact_date_year" };

		String header = "ID;TYPE;NE_IP_ADDRESS;NE_NAME;NE_LOCATION;MODULE;PRODUCT_NUMBER;RELEASE;ITEM_NO;MANUFACT_NO;STATUS;REV;POSITION;POLARIZATION;UPDATE_TIME;MANUFACT_DATE_YEAR";

		ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
		mappingStrategy.setColumnMapping(columns);
		mappingStrategy.setType(InventoryImportBean.class);

		// Let's write the CSV content
		try {
			writer.write(header + "\n");
			StatefulBeanToCsv beanWriter = new StatefulBeanToCsvBuilder<InventoryImportBean>(writer)
					.withMappingStrategy(mappingStrategy).withSeparator(';').withApplyQuotesToAll(false).build();
			beanWriter.write(inventoryListToSave);
			writer.close();
		} catch (Exception e) {
			log.error("Error while creating CSV for Inventory Scan ", e);
		}
		facesContext.responseComplete();
	}

	public List<InventoryImportBean> retrieveInventoryList() {

		List<InventoryImportBean> inventoryListToSave = new ArrayList<InventoryImportBean>();
		Inventory_DBConnection dbConn = null;

		try {
			dbConn = new Inventory_DBConnection();

			inventoryListToSave = dbConn.getInventoryResultForCSV();
			log.debug("Number of rows to be displayed: " + inventoryListToSave.size());

		} catch (Exception e) {
			log.debug("Error !!Exception in showInventoryResult : ", e);
		} finally {
			if (dbConn != null)
				dbConn = null;
		}

		return inventoryListToSave;
	}

}