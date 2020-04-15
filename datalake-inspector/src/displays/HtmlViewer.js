import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractHtmlViewer from "../../gen/displays/AbstractHtmlViewer";
import HtmlViewerNotifier from "../../gen/displays/notifiers/HtmlViewerNotifier";
import HtmlViewerRequester from "../../gen/displays/requesters/HtmlViewerRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';

const styles = theme => ({});

class HtmlViewer extends AbstractHtmlViewer {

	constructor(props) {
		super(props);
		this.notifier = new HtmlViewerNotifier(this);
		this.requester = new HtmlViewerRequester(this);
		this.state = {
		    ...this.state,
		    content: ''
		}
	};

	render() {
	    if (this.state.content == null || this.state.content === "") return (<React.Fragment/>);
	    const htmlStyle = "<style>html, body {height: 100%;} table {width: 100%;}table, th, td {border: 1px solid black;}table tr td { width: 50%; overflow-y: scroll;} table tr td div {width:1px;} td { vertical-align: top;}</style>"
		return (<div style={{width:"100%",height:"100%"}} dangerouslySetInnerHTML={{__html: htmlStyle + this.state.content}}></div>);
	};

	refresh = (content) => {
	    this.setState({content});
	};
}
export default withStyles(styles, { withTheme: true })(withSnackbar(HtmlViewer));
DisplayFactory.register("HtmlViewer", withStyles(styles, { withTheme: true })(withSnackbar(HtmlViewer)));