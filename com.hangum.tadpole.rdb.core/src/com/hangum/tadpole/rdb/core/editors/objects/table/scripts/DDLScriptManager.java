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
package com.hangum.tadpole.rdb.core.editors.objects.table.scripts;

import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine.DB_ACTION;
import com.hangum.tadpole.commons.sql.define.DBDefine;
import com.hangum.tadpole.dao.mysql.InformationSchemaDAO;
import com.hangum.tadpole.dao.mysql.ProcedureFunctionDAO;
import com.hangum.tadpole.dao.mysql.TableDAO;
import com.hangum.tadpole.dao.mysql.TriggerDAO;
import com.hangum.tadpole.dao.system.UserDBDAO;

/**
 * DDLScript Mananager
 * 
 * @author hangum
 *
 */
public class DDLScriptManager {
	protected UserDBDAO userDB;
	protected DB_ACTION actionType;
	
	public DDLScriptManager(UserDBDAO userDB, PublicTadpoleDefine.DB_ACTION actionType) {
		this.userDB = userDB;
		this.actionType = actionType;
	}
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	public String getScript(Object obj) throws Exception {
		String retStr = "";
		
		// find DB
		AbstractRDBDDLScript rdbScript = null;
		if(DBDefine.getDBDefine(userDB.getDbms_types()) == DBDefine.SQLite_DEFAULT) {
			rdbScript = new SQLiteDDLScript(userDB, actionType);
		} else if(DBDefine.getDBDefine(userDB.getDbms_types()) == DBDefine.ORACLE_DEFAULT ) {
			rdbScript = new OracleDDLScript(userDB, actionType);
		} else if(DBDefine.getDBDefine(userDB.getDbms_types()) == DBDefine.MSSQL_8_LE_DEFAULT ||
				DBDefine.getDBDefine(userDB.getDbms_types()) == DBDefine.MSSQL_DEFAULT ) {
			rdbScript = new MSSQL_8_LE_DDLScript(userDB, actionType);
		} else {
			throw new Exception("Not support Database");
		}
		
		// find DDL Object
		if(PublicTadpoleDefine.DB_ACTION.TABLES == actionType) {
			retStr = rdbScript.getTableScript((TableDAO)obj);
		} else if(PublicTadpoleDefine.DB_ACTION.VIEWS == actionType) {
			retStr = rdbScript.getViewScript(obj.toString());
		} else if(PublicTadpoleDefine.DB_ACTION.INDEXES == actionType) {
			retStr = rdbScript.getIndexScript((InformationSchemaDAO)obj);
		} else if(PublicTadpoleDefine.DB_ACTION.FUNCTIONS == actionType) {
			retStr = rdbScript.getFunctionScript((ProcedureFunctionDAO)obj);
		} else if(PublicTadpoleDefine.DB_ACTION.PROCEDURES == actionType) {
			retStr = rdbScript.getProcedureScript((ProcedureFunctionDAO)obj);
		} else if(PublicTadpoleDefine.DB_ACTION.TRIGGERS == actionType) {
			retStr = rdbScript.getTriggerScript((TriggerDAO)obj);
		} else {
			throw new Exception("Not support Database");
		}
		
		return retStr + PublicTadpoleDefine.SQL_DILIMITER;
	}
}
