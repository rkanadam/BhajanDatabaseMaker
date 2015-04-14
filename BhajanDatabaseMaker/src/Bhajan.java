public class Bhajan {
	public Bhajan(String lyrics, String meaning) {
		super();
		this.lyrics = lyrics;
		this.meaning = meaning;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	private String lyrics, meaning;
}
