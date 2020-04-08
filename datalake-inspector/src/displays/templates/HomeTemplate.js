import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractHomeTemplate from "../../../gen/displays/templates/AbstractHomeTemplate";
import HomeTemplateNotifier from "../../../gen/displays/notifiers/HomeTemplateNotifier";
import HomeTemplateRequester from "../../../gen/displays/requesters/HomeTemplateRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';

const styles = theme => ({});

class HomeTemplate extends AbstractHomeTemplate {

	constructor(props) {
		super(props);
		this.notifier = new HomeTemplateNotifier(this);
		this.requester = new HomeTemplateRequester(this);
	};


}

export default withStyles(styles, { withTheme: true })(withSnackbar(HomeTemplate));
DisplayFactory.register("HomeTemplate", withStyles(styles, { withTheme: true })(withSnackbar(HomeTemplate)));