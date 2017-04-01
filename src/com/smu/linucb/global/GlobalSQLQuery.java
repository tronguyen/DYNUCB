/**
*  Copyright 2014 Singapore Management University (SMU). All Rights Reserved. 
*
* Permission to use, copy, modify and distribute this software and 
* its documentation for purposes of research, teaching and general
* academic pursuits, without fee and without a signed licensing
* agreement, is hereby granted, provided that the above copyright
* statement, this paragraph and the following paragraph on disclaimer
* appear in all copies, modifications, and distributions.  Contact
* Singapore Management University, Intellectual Property Management
* Office at iie@smu.edu.sg, for commercial licensing opportunities.
*
* This software is provided by the copyright holder and creator "as is"
* and any express or implied warranties, including, but not Limited to,
* the implied warranties of merchantability and fitness for a particular 
* purpose are disclaimed.  In no event shall SMU or the creator be 
* liable for any direct, indirect, incidental, special, exemplary or 
* consequential damages, however caused arising in any way out of the
* use of this software.
*/

package com.smu.linucb.global;

import java.io.File;

public class GlobalSQLQuery {
	public static String GETVIEWTAG = "SELECT * FROM view_bookmark_tag";
	public static String GETBOOKMARK_TAG = "SELECT bookmarkID, tagID, tagWeight, value FROM tbl_bookmark_tags btl, tbl_tags tg where btl.tagID = tg.id";
	public static String GETTAG = "SELECT * FROM tbl_tags";
	// change user_taggedbookmarks to tbl_user_taggedbookmarks
	public static String GETUSER;
	public static String GETSUGGESTION4USER = "";

	public static String GETVIEW_ARTIST_TAG = "SELECT * FROM view_artist_tagvalue";
	// public static String GETUSERFM =
	// "SELECT distinct userID from tbl_user_artists";
	public static File fMatrix;
	public static String outputFile = "Output4Stats/Output4CIKM/";

	static {
		switch (Environment.DATASOURCE) {
		case DELICIOUS:
			GETSUGGESTION4USER = "SELECT distinct bookmarkID FROM tbl_user_taggedbookmarks where userID = ?";
			GETUSER = "SELECT distinct userID from tbl_user_taggedbookmarks";
			fMatrix = new File("Output4Stats/norm_matrix_ejml_full_delicious");
			outputFile += "Delicious/";
			break;
		case LASTFM:
			GETSUGGESTION4USER = "SELECT distinct artistID FROM tbl_user_taggedartists where userID = ?";
			GETUSER = "SELECT distinct userID from tbl_user_artists";
			fMatrix = new File("Output4Stats/norm_matrix_ejml_full_lastfm");
			outputFile += "Lastfm/";
			break;
		}
		outputFile = (Environment.randomGraph && Environment.runningAlgType
				.equals(AlgorithmType.CLUB)) ? (outputFile + "RandomGraph/")
				: outputFile;
	}
}
