/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.db.sqltrace;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class TracePreparedStatement
		extends TraceStatement
		implements PreparedStatement, Comparator
{
	protected HashMap parameters = new HashMap();

	public Object unwrap(Class iface) throws SQLException {	
		return ((PreparedStatement)stmt).unwrap(iface); 
	}

	public boolean	isWrapperFor(Class iface) throws SQLException {
		return ((PreparedStatement)stmt).isWrapperFor(iface); 
        }

	public TracePreparedStatement(PreparedStatement pstm, String sql)
	{
		super(pstm);
		parameters.put("SQL",sql);
	}
	
	public ResultSet executeQuery() throws SQLException
	{ 
		trace(parameters);
		return ((PreparedStatement)stmt).executeQuery(); 
	}
	
	public int executeUpdate() throws SQLException
	{ 
		trace(parameters);
		return ((PreparedStatement)stmt).executeUpdate(); 
	}
	
	public boolean execute() throws SQLException
	{ 
		trace(parameters);
		return ((PreparedStatement)stmt).execute(); 
	}
	
	public void addBatch() throws SQLException
	{
		if (batch==null) batch = new Vector();
		batch.add(parameters.clone());
	
		((PreparedStatement)stmt).addBatch();
	}
	
	public void clearParameters() throws SQLException						
	{ 
		parameters.clear();
		((PreparedStatement)stmt).clearParameters(); 
	}
	
	public ResultSetMetaData getMetaData() throws SQLException 				
	{ return ((PreparedStatement)stmt).getMetaData(); 	}
	
	public void setArray(int i, Array x) throws SQLException				
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setArray(i,x); 
	}				
	
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException				
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setAsciiStream(parameterIndex,x,length); 
	} 

	public void	setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setAsciiStream(parameterIndex,x); 
	}

	public void	setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setAsciiStream(parameterIndex,x,length); 

	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException 
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setBigDecimal(parameterIndex,x); 
	}
	
	public void	setBinaryStream(int parameterIndex, InputStream x)   throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setBinaryStream(parameterIndex,x); 
	}
 
	public void setBinaryStream(int parameterIndex, InputStream x, int length)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setBinaryStream(parameterIndex,x,length); 
	}

	public void	setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setBinaryStream(parameterIndex,x,length); 
	}
	
	// In the interface defintion TWO methods appear. The first one added
 	public void setBlob(int i, InputStream x, long length)  throws SQLException
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setBlob(i,x, length); 
	}
	
  	public void setBlob(int i, InputStream x)  throws SQLException
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setBlob(i,x); 
	}
	
 	public void setBlob(int i, Blob x)  throws SQLException
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setBlob(i,x); 
	}
	
	public void setBoolean(int parameterIndex, boolean x)  throws SQLException
	{ 	
		setParameter(parameterIndex,new Boolean(x));
		((PreparedStatement)stmt).setBoolean(parameterIndex,x); 
	}
	
	public void setByte(int parameterIndex, byte x)  throws SQLException
	{ 
		setParameter(parameterIndex,new Byte(x));
		((PreparedStatement)stmt).setByte(parameterIndex,x); 
	}
	
	public void setBytes(int parameterIndex, byte[] x)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setBytes(parameterIndex,x); 
	}
	
	//Two methods for setNCharacterStream added
	public void setNCharacterStream(int parameterIndex, Reader value)  throws SQLException
	{ 
		setParameter(parameterIndex,value);
		((PreparedStatement)stmt).setNCharacterStream(parameterIndex,value); 
	}

	public void setNCharacterStream(int parameterIndex, Reader value, long length)  throws SQLException
	{ 
		setParameter(parameterIndex,value);
		((PreparedStatement)stmt).setNCharacterStream(parameterIndex,value,length); 
	}

	//Tow variants of setCharacterStream added
	public void setCharacterStream(int parameterIndex, Reader reader)  throws SQLException
	{ 
		setParameter(parameterIndex,reader);
		((PreparedStatement)stmt).setCharacterStream(parameterIndex,reader); 
	}
	public void setCharacterStream(int parameterIndex, Reader reader, int length)  throws SQLException
	{ 
		setParameter(parameterIndex,reader);
		((PreparedStatement)stmt).setCharacterStream(parameterIndex,reader,length); 
	}
	public void setCharacterStream(int parameterIndex, Reader reader, long length)  throws SQLException
	{ 
		setParameter(parameterIndex,reader);
		((PreparedStatement)stmt).setCharacterStream(parameterIndex,reader,length); 
	}

	// In the interface defintion TWO methods appear. The first one added
	public void setClob(int i, Reader x)  throws SQLException
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setClob(i,x); 
	}

	public void setClob(int i, Clob x)  throws SQLException
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setClob(i,x); 
	}

	public void setClob(int i, Reader x, long length)  throws SQLException
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setClob(i,x, length); 
	}
	
	
	public void setDate(int parameterIndex, java.sql.Date x)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setDate(parameterIndex,x); 
	}
	
	public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setDate(parameterIndex,x,cal); 
	}
	
	public void setDouble(int parameterIndex, double x)  throws SQLException
	{ 
		setParameter(parameterIndex,new Double(x));
		((PreparedStatement)stmt).setDouble(parameterIndex,x); 
	}
	
	public void setFloat(int parameterIndex, float x)  throws SQLException
	{ 
		setParameter(parameterIndex,new Float(x));
		((PreparedStatement)stmt).setFloat(parameterIndex,x); 
	}
	
	public void setInt(int parameterIndex, int x)  throws SQLException
	{ 
		setParameter(parameterIndex,new Integer(x));
		((PreparedStatement)stmt).setInt(parameterIndex,x); 
	}
	
	public void setLong(int parameterIndex, long x)  throws SQLException
	{ 
		setParameter(parameterIndex,new Long(x));
		((PreparedStatement)stmt).setLong(parameterIndex,x); 
	}
	
	// In the interface defintion TWO methods appear. The first one added
 	public void setNClob(int parameterIndex, NClob value) {
	}

	public void setNClob(int parameterIndex, Reader reader) {
	}

	public void setNClob(int parameterIndex, Reader reader, long length) {
	}

	public void setNull(int parameterIndex, int sqlType)  throws SQLException
	{ 
		setParameter(parameterIndex,null);
		((PreparedStatement)stmt).setNull(parameterIndex,sqlType); 
	}
	
	public void setNull(int paramIndex, int sqlType, String typeName)  throws SQLException
	{ 
		setParameter(paramIndex,null);
		((PreparedStatement)stmt).setNull(paramIndex,sqlType,typeName); 
	}

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return ((PreparedStatement)stmt).getParameterMetaData();
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        setParameter(parameterIndex,x);
        ((PreparedStatement)stmt).setURL(parameterIndex,x);
    }

    public void setObject(int parameterIndex, Object x)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setObject(parameterIndex,x); 
	}
	
	public void setObject(int parameterIndex, Object x, int targetSqlType)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setObject(parameterIndex,x,targetSqlType); 
	}
	
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setObject(parameterIndex,x,targetSqlType,scale); 
	}
	
	public void setRef(int i, Ref x)  throws SQLException
	{ 
		setParameter(i,x);
		((PreparedStatement)stmt).setRef(i,x); 
	}
	
	public void setShort(int parameterIndex, short x)  throws SQLException
	{ 
		setParameter(parameterIndex,new Short(x));
		((PreparedStatement)stmt).setShort(parameterIndex,x); 
	}
	
	public void setString(int parameterIndex, String x)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setString(parameterIndex,x); 
	}
	
	public void setTime(int parameterIndex, Time x)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setTime(parameterIndex,x); 
	}
	
	public void setTime(int parameterIndex, Time x, Calendar cal)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setTime(parameterIndex,x,cal); 
	}
	
	public void setTimestamp(int parameterIndex, Timestamp x)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setTimestamp(parameterIndex,x); 
	}
	
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setTimestamp(parameterIndex,x,cal); 
	}

	/**	@deprecated	 */
	public void setUnicodeStream(int parameterIndex, InputStream x, int length)  throws SQLException
	{ 
		setParameter(parameterIndex,x);
		((PreparedStatement)stmt).setUnicodeStream(parameterIndex,x,length); 
	}

	protected void setParameter(int i, Object o)
	{
		parameters.put(new Integer(i),o);
	}

    protected void setParameter(String name, Object o)
    {
        parameters.put(name,o);
    }

	protected void trace(Object obj)
	{
		if (TraceDriverManager.traceEnabled()) {
			if (obj instanceof String)
				TraceDriverManager.println((String)obj);
			else if (obj instanceof Map) {
				//	trace with parameters
				Map map = (Map)obj;
				StringBuffer buf = new StringBuffer();
				buf.append(map.get("SQL"));
				buf.append(" (");

				Object[] keys = map.keySet().toArray();
				boolean any = false;
				Arrays.sort(keys,this);
				for (int i=0; i<keys.length; i++) {
					if (keys[i].equals("SQL")) continue;
					Object value = map.get(keys[i]);

					if (any) buf.append(",");
					buf.append(":");
					buf.append(keys[i]);
					buf.append("=");
					buf.append(value);
					any = true;
				}
				buf.append(")");
				TraceDriverManager.println(buf.toString());
			}
		}
	}

	public int compare(Object a, Object b)
	{
		if ((a instanceof Integer) && (b instanceof Integer))
			return ((Integer)a).intValue() - ((Integer)b).intValue();

		return a.toString().compareTo(b.toString());
	}

	public void	setSQLXML(int parameterIndex, SQLXML xmlObject) {
	}

//	public SQLXML	createSQLXML() {
//		((PreparedStatement)stmt).createSQLXML(); 
//	}

 	public  void setNString(int parameterIndex, String value) throws SQLException {
	}

	public  void setRowId(int parameterIndex,   RowId x) throws SQLException {
	}


}
