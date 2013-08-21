package org.wso2.carbon.connector.googlespreadsheet;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheetCreateSpreadsheet extends AbstractConnector  {
	
	private static final int DEFAULT_ROW_COUNT = 100;
	private static final int DEFAULT_COLUMN_COUNT = 20;
	private static final int DEFAULT_WORKSHEET_COUNT = 3;
	public static final String SPREADSHEET_NAME = "spreadsheetName";
	public static final String WORKSHEET_COUNT = "worksheetCount";
	private int rowCount = DEFAULT_ROW_COUNT;
	private int columnCount = DEFAULT_COLUMN_COUNT;
	private int worksheetCountInt = DEFAULT_WORKSHEET_COUNT;
	private static Log log = LogFactory
			.getLog(GoogleSpreadsheetCreateSpreadsheet.class);

	public void connect(MessageContext messageContext) throws ConnectException {
		
		try {
			
			String spreadsheetName = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, SPREADSHEET_NAME);
			String worksheetCount = GoogleSpreadsheetUtils
					.lookupFunctionParam(messageContext, WORKSHEET_COUNT);
			
			if (spreadsheetName == null
					|| "".equals(spreadsheetName.trim())) {				
				log.info("Please make sure you have given a name for the new spreadsheet");
				return;
			}

			try {
			if(worksheetCount != null) {
				worksheetCountInt = Integer.parseInt(worksheetCount);
			}

			} catch(NumberFormatException ex) {
				System.out.println("Please enter a valid number");
			}
			
			SpreadsheetService ssService = new GoogleSpreadsheetClientLoader(
					messageContext).loadSpreadsheetService();

			GoogleSpreadsheet gss = new GoogleSpreadsheet(ssService);

			SpreadsheetEntry ssEntry = gss
					.getSpreadSheetsByTitle(spreadsheetName);			

			GoogleSpreadsheetWorksheet gssWorksheet = new GoogleSpreadsheetWorksheet(
					ssService, ssEntry.getWorksheetFeedUrl());
			for(int i=0; i<worksheetCountInt; i++) {
				gssWorksheet.createWorksheet("Sheet"+i, rowCount, columnCount);
			}
				

		} catch (IOException te) {
			log.error("Failed to show status: " + te.getMessage(), te);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, te);
		} catch (ServiceException te) {
			log.error("Failed to show status: " + te.getMessage(), te);
			GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, te);
		}
	}

}
