package sam.anime.entities;

import static sam.anime.meta.AnimesMeta.AIRED;
import static sam.anime.meta.AnimesMeta.EPISODES;
import static sam.anime.meta.AnimesMeta.GENRES;
import static sam.anime.meta.AnimesMeta./** myanimelist_id */MAL_ID;
import static sam.anime.meta.AnimesMeta.SYNOPSIS;
import static sam.anime.meta.AnimesMeta.TITLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import sam.anime.db.AnimeDB;
import sam.anime.meta.TitleSynonymsMeta;
import sam.anime.meta.AnimeDirsMeta;
import sam.anime.meta.AnimeToshoMeta;

public class Anime {
	private final int mal_id;
	private String title;
	private String episodes;
	private String aired;
	private String genre;
	private String synopsis;
	
	private String jikanJson;

	private final AnimeDB db;

	private final AnimeList<String> title_synonyms;
	private final AnimeList<AnimeDir> dirs;
	private final AnimeList<String> links;

	public Anime(int mal_id) {
		this.db = null;
		this.mal_id = mal_id;

		title_synonyms = new  AnimeList<>(mal_id); 
		dirs = new  AnimeList<>(mal_id);
		links  = new  AnimeList<>(mal_id);
	}
	public Anime(ResultSet rs0, AnimeDB db) throws SQLException {
		this.mal_id = rs0.getInt(MAL_ID);
		this.title = rs0.getString(TITLE);
		this.episodes = rs0.getString(EPISODES);
		this.aired = rs0.getString(AIRED);
		this.genre = rs0.getString(GENRES);
		this.synopsis = rs0.getString(SYNOPSIS);
		this.db = db;

		this.title_synonyms = new AnimeList<>(mal_id, TitleSynonymsMeta.TITLE_SYNONYMS, TitleSynonymsMeta.TABLE_NAME, rs -> rs.getString(TitleSynonymsMeta.TITLE_SYNONYMS));
		this.dirs = new AnimeList<>(mal_id, new String[] {AnimeDirsMeta.PATH, AnimeDirsMeta.LAST_MODIFIED}, AnimeDirsMeta.TABLE_NAME, AnimeDir::new);
		this.links = new AnimeList<>(mal_id, AnimeToshoMeta.LINK, AnimeToshoMeta.TABLE_NAME, rs -> rs.getString(AnimeToshoMeta.LINK));
	}

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getEpisodes() { return episodes; }
	public void setEpisodes(String episodes) { this.episodes = episodes; }

	public String getAired() { return aired; }
	public void setAired(String aired) { this.aired = aired; }

	public String getGenre() { return genre; }
	public void setGenre(String genre) { this.genre = genre; }
	
	public int getMalId() {
		return mal_id;
	}
	public AnimeList<String> getTitleSynonyms() {
		return title_synonyms;
	}
	public AnimeList<AnimeDir> getDirs() {
		return dirs;
	}
	public AnimeList<String> getLinks() {
		return links;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}
	public String getJikanJson() {
		return jikanJson;
	}
	public void setJikanJson(String jikanJson) {
		this.jikanJson = jikanJson;
	}
	@Override
	public String toString() {
		return "Anime [mal_id=" + mal_id + ", title=" + title + ", episodes=" + episodes + ", aired=" + aired
				+ ", genre=" + genre + ", synopsis=" + synopsis + ", jikanJson=" + jikanJson + ", db=" + db
				+ ", title_synonyms=" + title_synonyms + ", dirs=" + dirs + ", links=" + links + "]";
	}
}
