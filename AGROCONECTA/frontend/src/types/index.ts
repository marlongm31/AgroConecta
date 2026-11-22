export type Role = 'PRODUCTOR' | 'COMPRADOR' | 'ADMINISTRADOR';

export type CropStatus =
  | 'PLANIFICADO'
  | 'EN_CRECIMIENTO'
  | 'LISTO_PARA_COSECHA'
  | 'COSECHADO'
  | 'PERDIDO';

export type ActivityType =
  | 'SIEMBRA'
  | 'RIEGO'
  | 'FERTILIZACION'
  | 'PODA'
  | 'COSECHA'
  | 'OTRO';

export type MeasurementUnit = 'KILOGRAMO' | 'TONELADA' | 'LIBRA' | 'BULTO' | 'UNIDAD';

export type ProductCategory =
  | 'FRUTAS'
  | 'HORTALIZAS'
  | 'GRANOS'
  | 'TUBERCULOS'
  | 'CAFE'
  | 'OTROS';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: Record<string, string> | null;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  phone?: string | null;
  location?: string | null;
  avatarUrl?: string | null;
  active: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: Role;
  phone?: string;
  location?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UpdateProfileRequest {
  fullName: string;
  phone?: string;
  location?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface Parcel {
  id: number;
  ownerId: number;
  name: string;
  location: string;
  area: number;
  description?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface ParcelRequest {
  name: string;
  location: string;
  area: number;
  description?: string;
}

export interface Crop {
  id: number;
  parcelId: number;
  name: string;
  variety?: string | null;
  plantingDate: string;
  estimatedHarvestDate?: string | null;
  status: CropStatus;
  observations?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CropRequest {
  parcelId: number;
  name: string;
  variety?: string;
  plantingDate: string;
  estimatedHarvestDate?: string | null;
  status: CropStatus;
  observations?: string;
}

export interface AgriculturalActivity {
  id: number;
  cropId: number;
  type: ActivityType;
  activityDate: string;
  description: string;
  observations?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface ActivityRequest {
  cropId: number;
  type: ActivityType;
  activityDate: string;
  description: string;
  observations?: string;
}

export interface Expense {
  id: number;
  ownerId: number;
  parcelId?: number | null;
  cropId?: number | null;
  concept: string;
  amount: number;
  expenseDate: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ExpenseRequest {
  parcelId?: number | null;
  cropId?: number | null;
  concept: string;
  amount: number;
  expenseDate: string;
}

export interface ProductionRecord {
  id: number;
  cropId: number;
  quantity: number;
  unit: MeasurementUnit;
  harvestDate: string;
  observations?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductionRequest {
  cropId: number;
  quantity: number;
  unit: MeasurementUnit;
  harvestDate: string;
  observations?: string;
}

export interface Product {
  id: number;
  ownerId: number;
  name: string;
  description: string;
  characteristics?: string | null;
  category: ProductCategory;
  price: number;
  availableQuantity: number;
  unit: MeasurementUnit;
  contactPhone: string;
  contactEmail: string;
  active: boolean;
  soldOut: boolean;
  imageUrls: string[];
  producerName?: string | null;
  producerLocation?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductRequest {
  name: string;
  description: string;
  characteristics?: string;
  category: ProductCategory;
  price: number;
  availableQuantity: number;
  unit: MeasurementUnit;
  contactPhone: string;
  contactEmail: string;
  active: boolean;
}

export interface ProductImage {
  id: number;
  productId: number;
  imageUrl: string;
  contentType: string;
  sizeBytes: number;
  position: number;
  createdAt: string;
}

export interface ProducerDashboard {
  parcelCount: number;
  activeCropCount: number;
  totalExpenses: number;
  productionRecordCount: number;
  publishedProductCount: number;
  recentActivities: AgriculturalActivity[];
}

export interface BuyerDashboard {
  availableProductCount: number;
  recentProducts: Product[];
}

export type SoilTexture =
  | 'ARENOSO'
  | 'FRANCO_ARENOSO'
  | 'FRANCO'
  | 'FRANCO_ARCILLOSO'
  | 'ARCILLOSO'
  | 'LIMOSO'
  | 'ORGANICO';

export type SoilDrainage = 'RAPIDO' | 'MODERADO' | 'LENTO' | 'ANEGADO';
export type SoilColor = 'OSCURO' | 'ROJIZO' | 'CLARO' | 'AMARILLENTO' | 'GRISACEO';
export type SoilWaterRetention = 'BAJA' | 'MEDIA' | 'ALTA';
export type SoilSlope = 'PLANO' | 'SUAVE' | 'MODERADO' | 'PRONUNCIADO';
export type SoilPhLevel = 'MUY_ACIDO' | 'ACIDO' | 'OPTIMO' | 'ALCALINO' | 'MUY_ALCALINO';
export type SoilOrganicMatter = 'BAJA' | 'MEDIA' | 'ALTA';

export interface SoilAssessmentRequest {
  parcelId?: number | null;
  texture: SoilTexture;
  drainage: SoilDrainage;
  color: SoilColor;
  waterRetention: SoilWaterRetention;
  slope: SoilSlope;
  phLevel: SoilPhLevel;
  organicMatter: SoilOrganicMatter;
  compaction: boolean;
  salinity: boolean;
  erosion: boolean;
}

export interface SoilAssessment {
  id: number;
  parcelId?: number | null;
  texture: SoilTexture;
  textureLabel: string;
  drainage: SoilDrainage;
  drainageLabel: string;
  color: SoilColor;
  colorLabel: string;
  waterRetention: SoilWaterRetention;
  waterRetentionLabel: string;
  slope: SoilSlope;
  slopeLabel: string;
  phLevel: SoilPhLevel;
  phLevelLabel: string;
  organicMatter: SoilOrganicMatter;
  organicMatterLabel: string;
  compaction: boolean;
  salinity: boolean;
  erosion: boolean;
  soilType: string;
  fertilityScore: number;
  fertilityLabel: string;
  recommendedCrops: string[];
  discouragedCrops: string[];
  cropCare: string[];
  amendments: string[];
  warnings: string[];
  disclaimer: string;
  createdAt: string;
}

export interface WeatherDay {
  date: string;
  weatherCode: number;
  condition: string;
  temperatureMax: number;
  temperatureMin: number;
  precipitationProbability: number;
  precipitationMm: number;
  windMaxKmh: number;
}

export interface WeatherReport {
  name: string;
  region?: string | null;
  country?: string | null;
  latitude: number;
  longitude: number;
  timezone: string;
  days: WeatherDay[];
  advisories: string[];
  fetchedAt: string;
}

export interface ChatConversation {
  id: number;
  productId?: number | null;
  productName?: string | null;
  counterpartId: number;
  counterpartName: string;
  counterpartAvatarUrl?: string | null;
  lastMessage: string;
  lastMessageAt: string;
  unreadCount: number;
}

export interface ChatAttachment {
  id: number;
  name: string;
  contentType: string;
  sizeBytes: number;
  downloadUrl: string;
}

export interface ChatMessage {
  id: number;
  conversationId: number;
  senderId: number;
  senderName: string;
  senderAvatarUrl?: string | null;
  content: string | null;
  clientMessageId?: string | null;
  attachment?: ChatAttachment | null;
  createdAt: string;
  readAt?: string | null;
  mine: boolean;
  replyToMessageId?: number | null;
}
