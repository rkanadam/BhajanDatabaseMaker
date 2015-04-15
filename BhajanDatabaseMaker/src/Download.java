import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hslf.model.AutoShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Download {

	public static void main(String[] args) throws Throwable {
		final String homeDirectory = System.getProperty("user.home");

		System.out
				.printf("User Home Directory is %s. Everything will be saved to %s/bhajans \n",
						homeDirectory, homeDirectory);

		final List<Bhajan> bhajans = new ArrayList<Bhajan>();
		downloadFromBhajanBrinda(bhajans);
		downloadRegion7EnglishBhajans(bhajans);
		downloadFromSaiDarshan(bhajans);
		downloadFromSaiScotland(bhajans);
		// downloadFromSathyaSaiOrg(bhajans);
		for (Bhajan bhajan : bhajans) {
			if (!bhajan.getLyrics().trim().contains("\n")) {
				System.out.printf("One line %s!\n", bhajan.getLyrics());
			}
		}

		final Workbook workbook = new HSSFWorkbook();
		final String opFile = homeDirectory + "/bhajans/bhajans.xls";
		final FileOutputStream os = new FileOutputStream(opFile);

		Sheet sheet = workbook.createSheet("bhajans");

		int rowNum = 0;
		for (Bhajan bhajan : bhajans) {
			// Create a row and put some cells in it. Rows are 0 based.
			Row row = sheet.createRow(rowNum++);
			// Create a cell and put a value in it.
			row.createCell(0).setCellValue(
					trimBhajanContent(bhajan.getLyrics()));
			row.createCell(1).setCellValue(
					trimBhajanContent(bhajan.getMeaning()));

		}

		workbook.write(os);
		workbook.close();
		os.close();
		System.out.printf("Writing file to %s.\n", opFile);
	}

	private static final String trimBhajanContent(final String bhajan) {
		String[] lines = bhajan.trim().split("\n", -1);
		final StringBuilder builder = new StringBuilder();
		for (String line : lines) {
			builder.append(line.trim()).append("\n");
		}
		return builder.toString();
	}

	private static void downloadFromSaiDarshan(List<Bhajan> bhajans)
			throws Throwable {
		final InputStream page = Download.class
				.getResourceAsStream("/english/saidarshan_english.html");
		final Document doc = Jsoup.parse(page, "UTF-8",
				"http://sathyasai.org/songs/");
		Elements elements = doc.getElementsByTag("LI");
		for (Element element : elements) {
			final String lyric = htmlToText(element.outerHtml());
			bhajans.add(new Bhajan(lyric, ""));
		}
	}

	private static void downloadFromSaiScotland(List<Bhajan> bhajans)
			throws Throwable {
		final InputStream page = Download.class
				.getResourceAsStream("/english/saiscotland_english.html");
		final Document doc = Jsoup.parse(page, "UTF-8",
				"http://sathyasai.org/songs/");
		Elements elements = doc.getElementsByTag("LI");
		for (Element element : elements) {
			final String lyric = htmlToText(element.outerHtml());
			bhajans.add(new Bhajan(lyric, ""));
		}
	}

	private static void downloadRegion7EnglishBhajans(List<Bhajan> bhajans)
			throws Throwable {
		final XMLSlideShow ppt = new XMLSlideShow(
				Download.class.getResourceAsStream("/english/region7.pptx"));
		for (XSLFSlide slide : ppt.getSlides()) {
			final StringBuilder lyrics = new StringBuilder();
			for (XSLFShape shape : slide.getShapes()) {
				if (shape instanceof XSLFAutoShape) {
					lyrics.append(((XSLFAutoShape) shape).getText());
				}
			}
			bhajans.add(new Bhajan(lyrics.toString(), ""));
		}
	}

	private static void downloadFromSathyaSaiOrg(final List<Bhajan> bhajans)
			throws Exception {
		final int numPages = 4;
		for (int pageNum = 1; pageNum <= numPages; ++pageNum) {
			final InputStream page1 = Download.class
					.getResourceAsStream("/sathyasai/page" + pageNum + ".html");
			final Document doc = Jsoup.parse(page1, "UTF-8",
					"http://sathyasai.org/songs/");
			Elements elements = doc.getElementsByTag("li");
			for (Element element : elements) {
				final String lyric = htmlToText(element.child(0).outerHtml());
				final String meaning = htmlToText(element.child(1).child(1)
						.outerHtml());
				bhajans.add(new Bhajan(lyric, meaning));
			}
		}
	}

	private static void downloadFromBhajanBrinda(List<Bhajan> bhajans)
			throws Exception {
		// TODO Auto-generated method stub
		final int numPages = 2;
		for (int pageNum = 1; pageNum <= numPages; ++pageNum) {
			final InputStream page1 = Download.class
					.getResourceAsStream("/bhajanabrinda/page" + pageNum
							+ ".html");
			final Document doc = Jsoup.parse(page1, "UTF-8",
					"http://sathyasai.org/songs/");
			final Elements anchors = doc.getElementsByTag("a");
			final String homeDirectory = System.getProperty("user.home");
			final File bhajanDirectory = new File(homeDirectory
					+ "/bhajans/bhajanabrinda.com");
			if (!bhajanDirectory.exists()) {
				bhajanDirectory.mkdirs();
			}
			for (final Element anchor : anchors) {
				final String href = anchor.attr("href");
				if (href.startsWith("http://bhajanabrinda.com/mod/data/view.php?")
						&& href.contains("rid")) {
					final String bhajanName = anchor.text();

					InputStream is = Download.class
							.getResourceAsStream("/bhajanabrinda/downloaded/"
									+ bhajanName + ".html");
					final boolean isBundled = is != null;
					if (!isBundled) {
						final File bhajanFile = new File(bhajanDirectory,
								bhajanName + ".html");
						if (!bhajanFile.exists() || bhajanFile.length() <= 0) {
							// This file is neither bundled nor already
							// downloaded
							bhajanFile.createNewFile();
							final FileOutputStream os = new FileOutputStream(
									bhajanFile);
							final URL url = new URL(href);
							System.out.printf("Downloading [%s] from [%s]. \n",
									bhajanName, href);
							IOUtils.copy(url.openStream(), os);
							os.close();
							is = new FileInputStream(bhajanFile);
						}
					}
					final Document document = Jsoup.parse(is, "UTF-8",
							"http://www.bhajanabrinda.com");
					final Elements nodes = document.getElementsByTag("td");
					final String lyric = htmlToText(nodes.get(1).outerHtml()
							.replace("<strong>", "").replace("</strong>", ""));
					final String meaning = htmlToText(nodes.get(3).outerHtml()
							.replace("<em>", "").replace("</em>", ""));
					bhajans.add(new Bhajan(lyric, meaning));
				}
			}
		}
	}

	private static String htmlToText(final String html) {
		return html.replace("<br>", "\n").replace("</p>", "\n")
				.replace("</div>", "\n").replaceAll("\\<[^>]+\\>", "")
				.replace("&amp;", "&").replace("&gt;", ">")
				.replace("&lt;", "<");
	}

}
