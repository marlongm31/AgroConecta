-- Resultados orientativos calculados a partir del cuestionario de suelo.
ALTER TABLE soil_assessments
    ADD COLUMN fertility_score INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN recommended_crops TEXT NOT NULL DEFAULT '',
    ADD COLUMN amendments TEXT NOT NULL DEFAULT '',
    ADD COLUMN warnings TEXT NOT NULL DEFAULT '';
