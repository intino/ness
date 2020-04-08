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
	    return (
	        <div style={{width:'100%'}}>
	        	{this.state.content}
            </div>
        );
	};

	refresh = (content) => {
	    this.setState({content});
	};
}
export default withStyles(styles, { withTheme: true })(withSnackbar(HtmlViewer));
DisplayFactory.register("HtmlViewer", withStyles(styles, { withTheme: true })(withSnackbar(HtmlViewer)));