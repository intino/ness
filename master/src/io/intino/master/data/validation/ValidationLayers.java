package io.intino.master.data.validation;

import io.intino.master.data.validation.validators.DuplicatedTripleRecordValidator;
import io.intino.master.data.validation.validators.SyntaxTripleValidator;

public class ValidationLayers {

	public static ValidationLayers createDefault() {
		ValidationLayers validationLayers = new ValidationLayers();
		validationLayers.tripleValidationLayer.addValidator(new SyntaxTripleValidator());
		validationLayers.recordValidationLayer.addValidator(new DuplicatedTripleRecordValidator());
		return validationLayers;
	}

	private volatile TripleValidationLayer tripleValidationLayer = new TripleValidationLayer();
	private volatile RecordValidationLayer recordValidationLayer = new RecordValidationLayer();

	public TripleValidationLayer tripleValidationLayer() {
		return tripleValidationLayer;
	}

	public ValidationLayers tripleValidationLayer(TripleValidationLayer tripleValidationLayer) {
		this.tripleValidationLayer = tripleValidationLayer == null ? new TripleValidationLayer() : tripleValidationLayer;
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
