ALTER TABLE soil_assessments
    ADD COLUMN discouraged_crops TEXT NOT NULL DEFAULT '',
    ADD COLUMN crop_care TEXT NOT NULL DEFAULT '';
