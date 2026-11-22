import type {
  ActivityType,
  CropStatus,
  MeasurementUnit,
  ProductCategory,
  Role,
  SoilColor,
  SoilDrainage,
  SoilOrganicMatter,
  SoilPhLevel,
  SoilSlope,
  SoilTexture,
  SoilWaterRetention,
} from '../types';

export const ROLE_LABELS: Record<Role, string> = {
  PRODUCTOR: 'Productor',
  COMPRADOR: 'Comprador',
  ADMINISTRADOR: 'Administrador',
};

export function canUseChat(role?: Role): boolean {
  return role === 'COMPRADOR' || role === 'PRODUCTOR';
}

export const CROP_STATUS_LABELS: Record<CropStatus, string> = {
  PLANIFICADO: 'Planificado',
  EN_CRECIMIENTO: 'En crecimiento',
  LISTO_PARA_COSECHA: 'Listo para cosecha',
  COSECHADO: 'Cosechado',
  PERDIDO: 'Perdido',
};

export const ACTIVITY_TYPE_LABELS: Record<ActivityType, string> = {
  SIEMBRA: 'Siembra',
  RIEGO: 'Riego',
  FERTILIZACION: 'Fertilización',
  PODA: 'Poda',
  COSECHA: 'Cosecha',
  OTRO: 'Otro',
};

export const MEASUREMENT_UNIT_LABELS: Record<MeasurementUnit, string> = {
  KILOGRAMO: 'Kilogramo',
  TONELADA: 'Tonelada',
  LIBRA: 'Libra',
  BULTO: 'Bulto',
  UNIDAD: 'Unidad',
};

export const PRODUCT_CATEGORY_LABELS: Record<ProductCategory, string> = {
  FRUTAS: 'Frutas',
  HORTALIZAS: 'Hortalizas',
  GRANOS: 'Granos',
  TUBERCULOS: 'Tubérculos',
  CAFE: 'Café',
  OTROS: 'Otros',
};

function toOptions<T extends string>(labels: Record<T, string>) {
  return (Object.keys(labels) as T[]).map((value) => ({ value, label: labels[value] }));
}

export const ROLE_OPTIONS = toOptions(ROLE_LABELS).filter((option) => option.value !== 'ADMINISTRADOR');
export const CROP_STATUS_OPTIONS = toOptions(CROP_STATUS_LABELS);
export const ACTIVITY_TYPE_OPTIONS = toOptions(ACTIVITY_TYPE_LABELS);
export const MEASUREMENT_UNIT_OPTIONS = toOptions(MEASUREMENT_UNIT_LABELS);
export const PRODUCT_CATEGORY_OPTIONS = toOptions(PRODUCT_CATEGORY_LABELS);

export const SOIL_TEXTURE_LABELS: Record<SoilTexture, string> = {
  ARENOSO: 'Arenoso',
  FRANCO_ARENOSO: 'Franco arenoso',
  FRANCO: 'Franco',
  FRANCO_ARCILLOSO: 'Franco arcilloso',
  ARCILLOSO: 'Arcilloso',
  LIMOSO: 'Limoso',
  ORGANICO: 'Orgánico',
};

export const SOIL_DRAINAGE_LABELS: Record<SoilDrainage, string> = {
  RAPIDO: 'Rápido',
  MODERADO: 'Moderado',
  LENTO: 'Lento',
  ANEGADO: 'Anegado',
};

export const SOIL_COLOR_LABELS: Record<SoilColor, string> = {
  OSCURO: 'Oscuro',
  ROJIZO: 'Rojizo',
  CLARO: 'Claro',
  AMARILLENTO: 'Amarillento',
  GRISACEO: 'Grisáceo',
};

export const SOIL_WATER_RETENTION_LABELS: Record<SoilWaterRetention, string> = {
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
};

export const SOIL_SLOPE_LABELS: Record<SoilSlope, string> = {
  PLANO: 'Plano',
  SUAVE: 'Suave',
  MODERADO: 'Moderado',
  PRONUNCIADO: 'Pronunciado',
};

export const SOIL_PH_LABELS: Record<SoilPhLevel, string> = {
  MUY_ACIDO: 'Muy ácido',
  ACIDO: 'Ácido',
  OPTIMO: 'Óptimo',
  ALCALINO: 'Alcalino',
  MUY_ALCALINO: 'Muy alcalino',
};

export const SOIL_ORGANIC_MATTER_LABELS: Record<SoilOrganicMatter, string> = {
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
};

export const SOIL_TEXTURE_OPTIONS = toOptions(SOIL_TEXTURE_LABELS);
export const SOIL_DRAINAGE_OPTIONS = toOptions(SOIL_DRAINAGE_LABELS);
export const SOIL_COLOR_OPTIONS = toOptions(SOIL_COLOR_LABELS);
export const SOIL_WATER_RETENTION_OPTIONS = toOptions(SOIL_WATER_RETENTION_LABELS);
export const SOIL_SLOPE_OPTIONS = toOptions(SOIL_SLOPE_LABELS);
export const SOIL_PH_OPTIONS = toOptions(SOIL_PH_LABELS);
export const SOIL_ORGANIC_MATTER_OPTIONS = toOptions(SOIL_ORGANIC_MATTER_LABELS);

export const CROP_STATUS_TONES: Record<CropStatus, 'green' | 'blue' | 'amber' | 'red' | 'slate'> = {
  PLANIFICADO: 'slate',
  EN_CRECIMIENTO: 'green',
  LISTO_PARA_COSECHA: 'amber',
  COSECHADO: 'blue',
  PERDIDO: 'red',
};
