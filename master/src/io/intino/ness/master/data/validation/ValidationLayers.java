package io.intino.ness.master.data.validation;

import io.intino.ness.master.data.validation.validators.DuplicatedTripletRecordValidator;
import io.intino.ness.master.data.validation.validators.SyntaxTripletValidator;

public class ValidationLayers {

	public static ValidationLayers createDefault() {
		ValidationLayers validationLayers = new ValidationLayers();
		validationLayers.tripletValidationLayer.addValidator(new SyntaxTripletValidator());
		validationLayers.recordValidationLayer.addValidator(new DuplicatedTripletRecordValidator());
		return validationLayers;
	}

	private volatile TripletValidationLayer tripletValidationLayer = new TripletValidationLayer();
	private volatile RecordValidationLayer recordValidationLayer = new RecordValidationLayer();

	public TripletValidationLayer tripleValidationLayer() {
		return tripletValidationLayer;
	}

	public ValidationLayers tripleValidationLayer(TripletValidationLayer tripletValidationLayer) {
		this.tripletValidationLayer = tripletValidationLayer == null ? new TripletValidationLayer() : tripletValidationLayer;
		return this;
	}

	public RecordValidationLayer recordValidationLayer() {
		return recordValidationLayer;
	}

	public ValidationLayers recordValidationLayer(RecordValidationLayer recordValidationLayer) {
		this.recordValidationLayer = recordValidationLayer == null ? new RecordValidationLayer() : recordValidationLayer;
		return this;
	}

	public enum Scope {
		TRIPLES, RECORDS
	}
}
