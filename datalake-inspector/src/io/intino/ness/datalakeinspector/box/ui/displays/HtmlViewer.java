package io.intino.ness.datalakeinspector.box.ui.displays;


import io.intino.ness.datalakeinspector.box.DatalakeInspectorBox;

public class HtmlViewer extends AbstractHtmlViewer<DatalakeInspectorBox> {

	private String content;

	public HtmlViewer(DatalakeInspectorBox box) {
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