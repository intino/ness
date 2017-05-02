package io.intino.ness.konos.actions;

import io.intino.ness.Tank;
import io.intino.ness.konos.NessBox;

import static io.intino.ness.konos.slack.Helper.findTank;


public class SealAction extends Action{

	public NessBox box;
	public String tank;

	public String execute() {
		Tank tank = findTank(box, this.tank);
		datalake(box).seal(tank);
		return OK;
	}


}