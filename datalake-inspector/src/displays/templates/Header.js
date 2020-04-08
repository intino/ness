import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractHeader from "../../../gen/displays/templates/AbstractHeader";
import HeaderNotifier from "../../../gen/displays/notifiers/HeaderNotifier";
import HeaderRequester from "../../../gen/displays/requesters/HeaderRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';

const styles = theme => ({});

class Header extends AbstractHeader {

	constructor(props) {
		super(props);
		this.notifier = new HeaderNotifier(this);
		this.requester = new HeaderRequester(this);
	};


}

export default withStyles(styles, { withTheme: true })(withSnackbar(Header));
DisplayFactory.register("Header", withStyles(styles, { withTheme: true })(withSnackbar(Header)));