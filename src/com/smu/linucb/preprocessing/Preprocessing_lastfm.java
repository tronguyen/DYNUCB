package com.smu.linucb.preprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smu.linucb.global.Environment;

public class Preprocessing_lastfm extends Preprocessing {

	private static final Map<String, Double> artist_tfidf = new HashMap<String, Double>();

	private void addArtist2Tag(Map<String, Set<Integer>> obj, int artist,
			String tag) {
		Set<Integer> artistSet = null;
		if (!obj.containsKey(tag)) {
			artistSet = new HashSet<Integer>();
			artistSet.add(artist);
			obj.put(tag, artistSet);
		} else {
			obj.get(tag).add(artist);
		}
	}

	public void buildTags(ResultSet resultSet) throws SQLException {
		int artistID;
		Set<String> setToken;
		String tagValue;
		String string;
		while (resultSet.next()) {
			artistID = resultSet.getInt("artistID");
			tagValue = resultSet.getString("tagValue");
			setToken = isSplit(tagValue.trim());
			for (Iterator<String> i = setToken.iterator(); i.hasNext();) {
				string = i.next().trim();
				addArtistTags(artistID, string, 1);
				if (!string.equals("")) {
					addArtist2Tag(Environment.hm_tag_artistset, artistID,
							string);
				}
			}
		}
	}

	public void calTF_IDF() {
		Set<Integer> artists = Environment.hm_artist_tag.keySet();
		Map<String, Double> hmTag;
		Set<String> tags;
		String tgItem;
		Integer artistID;
		double val_TFIDF;
		for (Iterator<Integer> i = artists.iterator(); i.hasNext();) {
			artistID = i.next();
			hmTag = Environment.hm_artist_tag.get(artistID);
			tags = hmTag.keySet();
			for (Iterator<String> j = tags.iterator(); j.hasNext();) {
				tgItem = j.next();
				val_TFIDF = getTf_Idf(hmTag.get(tgItem),
						Environment.hm_tag_artistset.get(tgItem).size());
				hmTag.put(tgItem, val_TFIDF);
				addTags(artist_tfidf, String.valueOf(artistID),
						Math.pow(val_TFIDF, 2));
			}
		}
	}

	public void writeBookmark_Tags(File f) {
		Set<String> tags = Environment.hm_tag_artistset.keySet();
		Set<Integer> artists = Environment.hm_artist_tag.keySet();
		Map<String, Double> hmTag;
		System.out.println(tags.size());
		BufferedWriter bw = null;
		Integer artistID;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			for (Iterator<Integer> i = artists.iterator(); i.hasNext();) {
				// c++;
				artistID = i.next();
				bw.write(String.valueOf(artistID));
				// System.out.print(bmid);
				hmTag = Environment.hm_artist_tag.get(artistID);
				for (String tg : tags) {
					// Write matrix to file
					// 0: for tags which do not appear in hmTag
					// norm_tfidf: otherwise
					if (hmTag.containsKey(tg)) {
						bw.write("\t"
								+ normalizeTF_IDF(hmTag.get(tg), artist_tfidf
										.get(String.valueOf(artistID))));
						// System.out.print("\t" +
						// normalizeTF_IDF(hmTag.get(tg),
						// bookmark_tfidf.get(String.valueOf(bmid))));
					} else {
						bw.write("\t" + 0);
						// System.out.print("\t" + 0);
					}
				}
				bw.write("\n");
				// System.out.print("\n");
				// if(c==500){
				// break;
				// }
			}

			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static double getTf_Idf(double tf, int df) {
		return tf
				* Math.log((double) Environment.hm_artist_tag.keySet().size()
						/ (1 + df));
	}

	private void addArtistTags(int artistID, String item, double count) {
		Map<String, Double> m;
		if (Environment.hm_artist_tag.containsKey(artistID)) {
			m = Environment.hm_artist_tag.get(artistID);
			addTags(m, item, count);
		} else {
			m = new HashMap<String, Double>();
			m.put(item, (double) count);
			Environment.hm_artist_tag.put(artistID, m);
		}
	}
}
