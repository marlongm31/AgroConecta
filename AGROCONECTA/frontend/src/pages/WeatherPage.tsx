import { AlertTriangle, CalendarDays, CloudSun, Droplets, MapPin, Thermometer, Wind } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Badge, PageHeader } from '../components/ui/Display';
import { EmptyState, PageLoader } from '../components/ui/Feedback';
import { Button } from '../components/ui/Button';
import { SelectField, TextField } from '../components/ui/FormFields';
import { weatherService } from '../services/weatherService';
import { parcelService } from '../services/parcelService';
import { cropService } from '../services/cropService';
import { useToast } from '../context/ToastContext';
import { getErrorMessage } from '../lib/errors';
import { formatDate, formatDateTime } from '../utils/format';
import type { Crop, Parcel, WeatherDay, WeatherReport } from '../types';

function dayLabel(value: string): string {
  return new Date(`${value}T00:00:00`).toLocaleDateString('es-CO', { weekday: 'short', day: '2-digit', month: 'short' });
}

function WeatherDayCard({ day }: { day: WeatherDay }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-3">
      <p className="text-xs font-semibold text-slate-700">{dayLabel(day.date)}</p>
      <p className="mt-1 text-sm text-slate-500">{day.condition}</p>
      <div className="mt-3 flex items-center gap-3 text-sm text-slate-700">
        <span className="flex items-center gap-1 font-semibold text-slate-900">
          <Thermometer className="h-4 w-4 text-amber-500" />
          {Math.round(day.temperatureMax)}°
        </span>
        <span className="text-xs text-slate-400">{Math.round(day.temperatureMin)}°</span>
      </div>
      <div className="mt-2 flex flex-wrap gap-x-3 gap-y-1 text-xs text-slate-500">
        <span className="flex items-center gap-1"><Droplets className="h-3.5 w-3.5 text-sky-500" />{Math.round(day.precipitationMm)} mm</span>
        <span>{day.precipitationProbability}% lluvia</span>
        <span className="flex items-center gap-1"><Wind className="h-3.5 w-3.5 text-slate-400" />{Math.round(day.windMaxKmh)} km/h</span>
      </div>
    </div>
  );
}

export function WeatherPage() {
  const toast = useToast();
  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [crops, setCrops] = useState<Crop[]>([]);
  const [parcelId, setParcelId] = useState('');
  const [location, setLocation] = useState('Bogotá');
  const [report, setReport] = useState<WeatherReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    Promise.all([parcelService.list(), cropService.list()])
      .then(([parcelPage, cropPage]) => {
        if (!active) return;
        setParcels(parcelPage.content);
        setCrops(cropPage.content);
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

  const handleSearch = async (event: FormEvent) => {
    event.preventDefault();
    setSearching(true);
    setError('');
    try {
      const next = parcelId
        ? await weatherService.byParcel(Number(parcelId))
        : await weatherService.byLocation(location.trim());
      setReport(next);
      if (!parcelId) toast.success('Consulta climática actualizada');
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setSearching(false);
    }
  };

  const upcomingCrops = crops.filter((crop) => {
    if (!report || !report.days.length) return false;
    const finalDate = report.days[report.days.length - 1].date;
    return crop.plantingDate >= report.days[0].date && crop.plantingDate <= finalDate;
  });

  return (
    <>
      <PageHeader
        title="Clima y labores"
        subtitle="Consulta el pronóstico de tu zona y recibe alertas para programar siembras y labores de campo."
      />

      <section className="card p-5">
        <form className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto]" onSubmit={handleSearch} noValidate>
          <div className="grid gap-4 sm:grid-cols-2">
            <SelectField
              label="Parcela"
              name="parcelId"
              value={parcelId}
              onChange={(event) => setParcelId(event.target.value)}
              options={parcels.map((parcel) => ({ value: String(parcel.id), label: parcel.name }))}
              placeholder="Buscar por ubicación"
            />
            {!parcelId ? (
              <TextField
                label="Ubicación"
                name="location"
                required
                value={location}
                onChange={(event) => setLocation(event.target.value)}
                placeholder="Ciudad o municipio"
              />
            ) : (
              <div className="flex items-end">
                <p className="input-base flex items-center gap-2 text-sm text-slate-500">
                  <MapPin className="h-4 w-4" /> Se usará la ubicación de la parcela
                </p>
              </div>
            )}
          </div>
          <div className="flex items-end">
            <Button type="submit" loading={searching} icon={<CloudSun className="h-4 w-4" />} className="w-full sm:w-auto">
              Consultar
            </Button>
          </div>
        </form>
        {error ? <p className="mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
      </section>

      {loading ? (
        <div className="mt-6">
          <PageLoader />
        </div>
      ) : report ? (
        <div className="mt-6 space-y-6">
          <section className="card p-5">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <h2 className="text-lg font-semibold text-slate-900">{report.name}</h2>
                <p className="mt-1 text-sm text-slate-500">
                  {[report.region, report.country].filter(Boolean).join(', ')} · {report.latitude.toFixed(4)}, {report.longitude.toFixed(4)}
                </p>
              </div>
              <Badge tone="blue">Datos al {formatDateTime(report.fetchedAt)}</Badge>
            </div>
            <div className="mt-4 grid gap-3 sm:grid-cols-3 lg:grid-cols-7">
              {report.days.map((day) => <WeatherDayCard key={day.date} day={day} />)}
            </div>
          </section>

          <section className="card p-5">
            <h2 className="flex items-center gap-2 text-lg font-semibold text-slate-900">
              <AlertTriangle className="h-5 w-5 text-amber-500" />
              Alertas y recomendaciones
            </h2>
            {report.advisories.length === 0 ? (
              <p className="mt-3 text-sm text-slate-500">No hay alertas climáticas relevantes para los próximos días.</p>
            ) : (
              <ul className="mt-3 space-y-2">
                {report.advisories.map((advisory) => (
                  <li key={advisory} className="rounded-lg border-l-4 border-amber-400 bg-amber-50 px-3 py-2 text-sm text-amber-900">
                    {advisory}
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="card p-5">
            <h2 className="flex items-center gap-2 text-lg font-semibold text-slate-900">
              <CalendarDays className="h-5 w-5 text-agro-600" />
              Siembras en el periodo del pronóstico
            </h2>
            {upcomingCrops.length === 0 ? (
              <p className="mt-3 text-sm text-slate-500">No tienes siembras programadas dentro del pronóstico consultado.</p>
            ) : (
              <ul className="mt-3 space-y-2">
                {upcomingCrops.map((crop) => (
                  <li key={crop.id} className="flex flex-col gap-1 rounded-lg bg-slate-50 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
                    <span className="text-sm font-medium text-slate-800">{crop.name}{crop.variety ? ` · ${crop.variety}` : ''}</span>
                    <span className="text-sm text-slate-500">Siembra: {formatDate(crop.plantingDate)}</span>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </div>
      ) : (
        <div className="mt-6">
          <EmptyState
            icon={<CloudSun className="h-8 w-8" />}
            title="Consulta el clima de tu zona"
            description="El pronóstico se usa para avisarte de lluvias, heladas, calor extremo y viento antes de salir al campo."
          />
        </div>
      )}
    </>
  );
}
