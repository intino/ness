package io.intino.datahub.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.spark.UIFile;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.ui.displays.HtmlViewer;
import io.intino.datahub.datalake.regenerator.Mapper;
import io.intino.datahub.datalake.regenerator.MapperLoader;
import io.intino.datahub.datalake.regenerator.Regenerator;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.List;

public class HomeTemplate extends AbstractHomeTemplate<DataHubBox> {

	private HtmlViewer htmlViewer;

	public HomeTemplate(DataHubBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		reviewDialog.onOpen(e -> {
			htmlViewer = new HtmlViewer(box());
			table.display(htmlViewer);
			loadContent();
			table.visible(true);
			htmlViewer.refresh();
			loading.visible(false);
		});
	}

	private void loadContent() {
		try {
			File reviewResult = calculateReview(mapper.value());
			if (reviewResult != null) {
				downloadReview.onExecute(event -> loadFile(reviewResult));
				if (reviewResult.length() > 2000000) //2MB
					htmlViewer.content(wrap("Review report is too long. Download to inspect it."));
				else {
					String content = Files.readString(reviewResult.toPath());
					htmlViewer.content(content.substring(content.indexOf("<table"), content.indexOf("</table>")) + "</table>");
				}
			} else htmlViewer.content(wrap("Impossible to execute mapper."));
		} catch (MapperLoader.CompilationException | IOException ex) {
			htmlViewer.content(wrap(ex.getMessage()));
		}
	}

	private File calculateReview(String mapperCode) throws MapperLoader.CompilationException {
		DataHubBox box = box();
		try {
			Mapper mapper = new MapperLoader(box.configuration().home()).compileAndLoad(mapperCode);
			List<File> review = new Regenerator(box.datalake(), new File(box.graph().datalake().backup().path(), "sessions"), new File(box.configuration().home(), "reviews")).review(mapper);
			return review.get(0);
		} catch (IOException | InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException ex) {
			Logger.error(ex);
			return null;
		}
	}

	private String wrap(String message) {
		return "<div style=\"text-align:center\">" + message + "</div>";
	}

	private UIFile loadFile(File reviewResult) {
		return new UIFile() {
			@Override
			public String label() {
				return reviewResult.getName();
			}

			@Override
			public InputStream content() {
				try {
					return new FileInputStream(reviewResult);
				} catch (FileNotFoundException ex) {
					return null;
				}
			}
		};
	}


}