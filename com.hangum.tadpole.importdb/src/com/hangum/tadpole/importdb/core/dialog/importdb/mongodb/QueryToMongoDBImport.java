/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.importdb.core.dialog.importdb.mongodb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;

import com.hangum.tadpole.dao.system.UserDBDAO;
import com.hangum.tadpole.importdb.Activator;
import com.hangum.tadpole.importdb.core.Messages;
import com.hangum.tadpole.importdb.core.dialog.importdb.utils.SQLQueryUtil;
import com.hangum.tadpole.mongodb.core.query.MongoDBQuery;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * query를 몽고디비로 임포트합니다.
 * 
 * @author hangum
 *
 */
public class QueryToMongoDBImport extends DBImport {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(QueryToMongoDBImport.class);
	
	private String colName;
	private String userQuery; 
	private boolean isExistOnDelete;
	
	public QueryToMongoDBImport(UserDBDAO importUserDB, String colName,  String userQuery, UserDBDAO exportUserDB, boolean isExistOnDelete) {
		super(importUserDB, exportUserDB);
		
		this.colName = colName;
		this.userQuery = userQuery;
		this.isExistOnDelete = isExistOnDelete;
	}
	
	/**
	 * table import
	 */
	public Job workTableImport() {
		if("".equals(userQuery.trim())) { //$NON-NLS-1$
			MessageDialog.openInformation(null, Messages.QueryToMongoDBImport_1, Messages.QueryToMongoDBImport_2);			
			return null;		
		}
		if("".equals(colName.trim())) {
			MessageDialog.openInformation(null, Messages.QueryToMongoDBImport_1, Messages.QueryToMongoDBImport_5);			
			return null;		
		}
		
		// job
		Job job = new Job("Execute data Import.") { //$NON-NLS-1$
			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Start import....", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
				
				try {
					monitor.subTask(userQuery + " table importing..."); //$NON-NLS-1$
					
					if(isExistOnDelete) MongoDBQuery.existOnDelete(importUserDB, colName);
					
					insertMongoDB();
				} catch(Exception e) {
					logger.error("press ok button", e);						 //$NON-NLS-1$
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); //$NON-NLS-1$
				} finally {
					monitor.done();
				}
				
				return Status.OK_STATUS;
			}
		};
		
		return job;
	}
	
	/**
	 * 데이터를 입력합니다.
	 * 
	 * @param modTableDAO
	 * @param userDBDAO
	 * @throws Exception
	 */
	private void insertMongoDB() throws Exception {
		SQLQueryUtil qu = new SQLQueryUtil(exportUserDB, userQuery);
		while(qu.hasNext()) {
			qu.nextQuery();
			HashMap<Integer, String> mapCNameToIndex = qu.getMapColumns();
			List<HashMap<Integer, Object>> listTableData = qu.getTableDataList();
			
			// row 단위
			List<DBObject> listDBObject = new ArrayList<DBObject>();
			for (int i=0; i<listTableData.size(); i++) {
				HashMap<Integer, Object> resultMap = listTableData.get(i);					
				
				DBObject insertObject = new BasicDBObject();
				// column 단위
				for(int j=0; j<mapCNameToIndex.size(); j++) {
					// BigDecimal is not support mongodb 
					if(resultMap.get(j) instanceof java.math.BigDecimal) {
						BigDecimal db = (BigDecimal)resultMap.get(j);
						insertObject.put(mapCNameToIndex.get(j), db.longValue());	
					} else {
						insertObject.put(mapCNameToIndex.get(j), resultMap.get(j));
					}						
				}
				
				listDBObject.add(insertObject);
			}	// end for
		
			MongoDBQuery.insertDocument(importUserDB, colName, listDBObject);
		}
	}
}
