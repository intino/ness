package io.intino.datahub.box.ui.displays;

import io.intino.datahub.box.DataHubBox;

public class HtmlViewer extends AbstractHtmlViewer<DataHubBox> {

	private String content;

	public HtmlViewer(DataHubBox box) {
		super(box);
	}

	public void content(String content) {
		this.content = content;
	}

	@Override
	public void refresh() {
		super.refresh();
		notifier.refresh(content);
	}
}