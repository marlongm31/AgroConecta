import { ClipboardList, Info, Leaf, ShieldCheck } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Badge, PageHeader } from '../components/ui/Display';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Button } from '../components/ui/Button';
import { SelectField } from '../components/ui/FormFields';
import { soilService } from '../services/soilService';
import { parcelService } from '../services/parcelService';
import { useToast } from '../context/ToastContext';
import { useOffline } from '../context/OfflineContext';
import { getErrorMessage } from '../lib/errors';
import { formatDate } from '../utils/format';
import {
  SOIL_COLOR_OPTIONS,
  SOIL_DRAINAGE_OPTIONS,
  SOIL_ORGANIC_MATTER_OPTIONS,
  SOIL_PH_OPTIONS,
  SOIL_SLOPE_OPTIONS,
  SOIL_TEXTURE_OPTIONS,
  SOIL_WATER_RETENTION_OPTIONS,
} from '../utils/labels';
import type {
  Parcel,
  SoilAssessment,
  SoilAssessmentRequest,
  SoilColor,
  SoilDrainage,
  SoilOrganicMatter,
  SoilPhLevel,
  SoilSlope,
  SoilTexture,
  SoilWaterRetention,
} from '../types';

const EMPTY_FORM: SoilAssessmentRequest = {
  parcelId: null,
  texture: 'FRANCO',
  drainage: 'MODERADO',
  color: 'OSCURO',
  waterRetention: 'MEDIA',
  slope: 'PLANO',
  phLevel: 'OPTIMO',
  organicMatter: 'MEDIA',
  compaction: false,
  salinity: false,
  erosion: false,
};

function GuidanceList({ title, items, tone = 'green' }: { title: string; items: string[]; tone?: 'green' | 'amber' | 'red' | 'blue' }) {
  if (items.length === 0) return null;
  return (
    <section>
      <h3 className="text-sm font-semibold text-slate-800">{title}</h3>
      <ul className="mt-2 space-y-2">
        {items.map((item) => (
          <li key={item} className={`rounded-lg border-l-4 bg-slate-50 px-3 py-2 text-sm text-slate-600 ${
            tone === 'red' ? 'border-red-400' : tone === 'amber' ? 'border-amber-400' : tone === 'blue' ? 'border-sky-400' : 'border-agro-500'
          }`}>
            {item}
          </li>
        ))}
      </ul>
    </section>
  );
}

export function SoilQuestionnairePage() {
  const toast = useToast();
  const { online } = useOffline();
  const [form, setForm] = useState<SoilAssessmentRequest>(EMPTY_FORM);
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [history, setHistory] = useState<SoilAssessment[]>([]);
  const [result, setResult] = useState<SoilAssessment | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    Promise.all([parcelService.list(), soilService.list()])
      .then(([parcelPage, assessmentPage]) => {
        if (!active) return;
        setParcels(parcelPage.content);
        setHistory(assessmentPage.content);
        setResult(assessmentPage.content[0] ?? null);
      })
      .catch((caught) => {
        if (active) setError(getErrorMessage(caught));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!online) {
      setError('El cuestionario necesita conexión para calcular la orientación.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const assessment = await soilService.assess(form);
      setResult(assessment);
      setHistory((current) => [assessment, ...current]);
      toast.success('Orientación de suelo generada');
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setSaving(false);
    }
  };

  const toggle = (field: 'compaction' | 'salinity' | 'erosion') => {
    setForm((current) => ({ ...current, [field]: !current[field] }));
  };

  return (
    <>
      <PageHeader
        title="Cuestionario de suelo"
        subtitle="Responde con lo que observas en tu parcela para recibir una orientación agrícola inicial."
      />

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.1fr)]">
        <section className="card p-5">
          <h2 className="flex items-center gap-2 text-lg font-semibold text-slate-900">
            <ClipboardList className="h-5 w-5 text-agro-600" />
            Observaciones
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            No necesitas equipo de laboratorio; usa lo que puedes ver y sentir en el suelo.
          </p>

          {loading ? (
            <div className="mt-6">
              <PageLoader />
            </div>
          ) : (
            <form className="mt-5 space-y-4" onSubmit={handleSubmit} noValidate>
              {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
              {!online ? (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-800">
                  Sin conexión. Podrás consultar orientaciones guardadas, pero no calcular una nueva hasta volver a conectarte.
                </p>
              ) : null}
              <SelectField
                label="Parcela (opcional)"
                name="parcelId"
                value={form.parcelId ? String(form.parcelId) : ''}
                onChange={(event) => setForm({ ...form, parcelId: event.target.value ? Number(event.target.value) : null })}
                options={parcels.map((parcel) => ({ value: String(parcel.id), label: parcel.name }))}
                placeholder="No asociar a una parcela"
              />
              <div className="grid gap-4 sm:grid-cols-2">
                <SelectField
                  label="Textura al tacto"
                  name="texture"
                  required
                  value={form.texture}
                  onChange={(event) => setForm({ ...form, texture: event.target.value as SoilTexture })}
                  options={SOIL_TEXTURE_OPTIONS}
                />
                <SelectField
                  label="Drenaje del agua"
                  name="drainage"
                  required
                  value={form.drainage}
                  onChange={(event) => setForm({ ...form, drainage: event.target.value as SoilDrainage })}
                  options={SOIL_DRAINAGE_OPTIONS}
                />
                <SelectField
                  label="Color del suelo"
                  name="color"
                  required
                  value={form.color}
                  onChange={(event) => setForm({ ...form, color: event.target.value as SoilColor })}
                  options={SOIL_COLOR_OPTIONS}
                />
                <SelectField
                  label="Retención de agua"
                  name="waterRetention"
                  required
                  value={form.waterRetention}
                  onChange={(event) => setForm({ ...form, waterRetention: event.target.value as SoilWaterRetention })}
                  options={SOIL_WATER_RETENTION_OPTIONS}
                />
                <SelectField
                  label="Pendiente del terreno"
                  name="slope"
                  required
                  value={form.slope}
                  onChange={(event) => setForm({ ...form, slope: event.target.value as SoilSlope })}
                  options={SOIL_SLOPE_OPTIONS}
                />
                <SelectField
                  label="Nivel de pH estimado"
                  name="phLevel"
                  required
                  value={form.phLevel}
                  onChange={(event) => setForm({ ...form, phLevel: event.target.value as SoilPhLevel })}
                  options={SOIL_PH_OPTIONS}
                />
                <SelectField
                  label="Materia orgánica"
                  name="organicMatter"
                  required
                  value={form.organicMatter}
                  onChange={(event) => setForm({ ...form, organicMatter: event.target.value as SoilOrganicMatter })}
                  options={SOIL_ORGANIC_MATTER_OPTIONS}
                  className="sm:col-span-2"
                />
              </div>
              <div className="space-y-2">
                {([
                  ['compaction', 'El suelo está compactado o muy duro'],
                  ['salinity', 'Se observa salinidad o costra blanca'],
                  ['erosion', 'Hay erosión o pérdida visible de suelo'],
                ] as const).map(([field, label]) => (
                  <label key={field} className="flex items-center gap-2 text-sm text-slate-700">
                    <input
                      type="checkbox"
                      checked={form[field]}
                      onChange={() => toggle(field)}
                      className="h-4 w-4 rounded border-slate-300 text-agro-600"
                    />
                    {label}
                  </label>
                ))}
              </div>
              <Button type="submit" loading={saving} icon={<Leaf className="h-4 w-4" />} className="w-full">
                Generar orientación
              </Button>
            </form>
          )}
        </section>

        <section className="card p-5">
          {result ? (
            <div className="space-y-5">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                <div>
                  <h2 className="text-lg font-semibold text-slate-900">Orientación para tu suelo</h2>
                  <p className="mt-1 text-sm text-slate-500">Registro del {formatDate(result.createdAt)}</p>
                </div>
                <Badge tone={result.fertilityScore >= 75 ? 'green' : result.fertilityScore >= 50 ? 'amber' : 'red'}>
                  Fertilidad {result.fertilityLabel} · {result.fertilityScore}/100
                </Badge>
              </div>
              <div className="rounded-xl bg-agro-50 p-4">
                <p className="text-xs font-medium uppercase tracking-wide text-agro-700">Tipo estimado</p>
                <p className="mt-1 text-base font-semibold text-agro-900">{result.soilType}</p>
                <p className="mt-2 text-xs text-agro-800">
                  Textura {result.textureLabel.toLowerCase()} · drenaje {result.drainageLabel.toLowerCase()} · materia orgánica {result.organicMatterLabel.toLowerCase()}
                </p>
              </div>
              <GuidanceList title="Cómo tratarlo y prepararlo" items={result.amendments} />
              <GuidanceList title="Cultivos adecuados" items={result.recommendedCrops} />
              <GuidanceList title="Cuidados recomendados por cultivo" items={result.cropCare} tone="blue" />
              <GuidanceList title="Cultivos no recomendables y por qué" items={result.discouragedCrops} tone="amber" />
              <GuidanceList title="Precauciones" items={result.warnings} tone="red" />
              <p className="flex items-start gap-2 rounded-lg bg-slate-50 p-3 text-xs text-slate-600">
                <Info className="mt-0.5 h-4 w-4 shrink-0" />
                {result.disclaimer}
              </p>
            </div>
          ) : (
            <EmptyState
              icon={<ShieldCheck className="h-8 w-8" />}
              title="Aún no hay orientaciones guardadas"
              description="Responde el cuestionario para ver aquí la guía de tu suelo."
            />
          )}
        </section>
      </div>

      {history.length > 1 ? (
        <section className="card mt-6 p-5">
          <h2 className="text-sm font-semibold text-slate-800">Orientaciones anteriores</h2>
          <div className="mt-3 flex flex-wrap gap-2">
            {history.map((assessment) => (
              <Button
                key={assessment.id}
                variant={result?.id === assessment.id ? 'primary' : 'secondary'}
                onClick={() => setResult(assessment)}
              >
                {formatDate(assessment.createdAt)} · {assessment.soilType}
              </Button>
            ))}
          </div>
        </section>
      ) : null}
    </>
  );
}
