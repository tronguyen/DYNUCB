package com.smu.linucb.preprocessing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.smu.linucb.global.DATASET;
import com.smu.linucb.global.Environment;

public class Dbconnection {
	private static Dbconnection conn = new Dbconnection();
	private static Connection connection;
	private static PreparedStatement pStmt = null;
	private static final Object lock = new Object();

	private Dbconnection() {
		try {
			String dataset = (Environment.DATASOURCE.equals(DATASET.LASTFM)) ? "lastfm"
					: "delicious";
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/" + dataset, "root", "");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Dbconnection _getConn() {
		return conn;
	}

	// public ResultSet getCountTags() {
	// Statement statement;
	// ResultSet resultSet = null;
	// try {
	// statement = connection.createStatement();
	// resultSet = statement
	// .executeQuery();
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return resultSet;
	// }

	public synchronized ResultSet getResultSet(String sql) {
		Statement statement;
		ResultSet rs = null;
		try {
			statement = connection.createStatement();
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}

	public synchronized List<Integer> getBookmark4User(String q, int usr)
			throws SQLException {
		List<Integer> result = new ArrayList<Integer>();
		pStmt = connection.prepareStatement(q);
		pStmt.setInt(1, usr);
		int bmid;
		ResultSet res = pStmt.executeQuery();
		while (res.next()) {
			bmid = res.getInt(1);
			if (!Environment.removedBM.contains(bmid)) {
				result.add(bmid);
			}
		}
		return result;
	}
}
