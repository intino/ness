package io.intino.datahub.box.ui.displays.templates;

import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.ui.displays.HtmlViewer;
import io.intino.datahub.datalake.regenerator.Mapper;
import io.intino.datahub.datalake.regenerator.MapperLoader;
import io.intino.datahub.datalake.regenerator.Regenerator;

import java.io.File;
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
		htmlViewer = new HtmlViewer(box());
		table.display(htmlViewer);
		review.onExecute(e -> htmlViewer.content(calculateContent(mapper.value())));
	}


	private String calculateContent(String mapperCode) {
		DataHubBox box = box();
		try {
			Mapper mapper = new MapperLoader(box.configuration().home()).compileAndLoad(mapperCode);
			List<File> review = new Regenerator(box.datalake(), new File(box.graph().datalake().backup().path(), "sessions"), new File(box.configuration().home(), "reviews")).review(mapper);
			return Files.readString(review.get(0).toPath());
		} catch (Exception e) {
			return e.getMessage();
		}
	}


}