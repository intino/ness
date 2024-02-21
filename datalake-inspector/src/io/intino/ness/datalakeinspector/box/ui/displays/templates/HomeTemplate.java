package io.intino.ness.datalakeinspector.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.spark.UIFile;
import io.intino.ness.datalakeinspector.box.DatalakeInspectorBox;
import io.intino.ness.datalakeinspector.box.regenerator.Mapper;
import io.intino.ness.datalakeinspector.box.regenerator.MapperLoader;
import io.intino.ness.datalakeinspector.box.regenerator.Regenerator;
import io.intino.ness.datalakeinspector.box.ui.displays.HtmlViewer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.List;

public class HomeTemplate extends AbstractHomeTemplate<DatalakeInspectorBox> {
	private static String template;
	private HtmlViewer htmlViewer;
	private File file;

	static {
		try {
			template = new String(HomeTemplate.class.getResourceAsStream("/mapper.template").readAllBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public HomeTemplate(DatalakeInspectorBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		mapper.value(template);
		reviewDialog.onOpen(e -> {
			htmlViewer = new HtmlViewer(box());
			table.display(htmlViewer);
			file = loadContent();
			table.visible(true);
			htmlViewer.refresh();
			loading.visible(false);
		});
		reviewDialog.onClose(event -> {
			if (file != null && file.exists()) file.delete();

		});
	}

	private File loadContent() {
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
			return reviewResult;
		} catch (MapperLoader.CompilationException | IOException ex) {
			htmlViewer.content(wrap(ex.getMessage()));
			return null;
		}
	}

	private File calculateReview(String mapperCode) throws MapperLoader.CompilationException {
		DatalakeInspectorBox box = box();
		try {
			MapperLoader mapperLoader = new MapperLoader(box.configuration().home());
			Mapper mapper = mapperLoader.compileAndLoad(mapperCode);
			List<File> review = new Regenerator(box.datalake(), null, new File(box.configuration().home(), "reviews")).review(mapper);
			mapperLoader.delete(mapperCode);
			return review.get(0);
		} catch (IOException | InstantiationException | InvocationTargetException | IllegalAccessException |
				 ClassNotFoundException ex) {
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