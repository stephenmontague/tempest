import { BaseServiceClient } from "./base-client";

/**
 * Facility entity from WMS.
 */
export interface Facility {
     id: number;
     tenantId: string;
     code: string;
     name: string;
     facilityType: string;
     addressLine1?: string;
     addressLine2?: string;
     city?: string;
     state?: string;
     postalCode?: string;
     country?: string;
     active: boolean;
     createdAt: string;
     updatedAt: string;
}

/**
 * Wave entity from WMS.
 */
export interface Wave {
     id: number;
     tenantId: string;
     facilityId: number;
     waveNumber: string;
     status: string;
     createdByUserId?: string;
     createdAt: string;
     updatedAt: string;
}

/**
 * Pick task entity from WMS.
 */
export interface PickTask {
     id: number;
     tenantId: string;
     waveId?: number;
     orderId: number;
     orderLineId: number;
     sku: string;
     quantity: number;
     fromLocationId?: number;
     status: string;
     assignedUserId?: string;
     pickedAt?: string;
     createdAt: string;
     updatedAt: string;
}

/**
 * Request to create a wave.
 */
export interface CreateWaveRequest {
     facilityId: number;
     waveNumber: string;
}

/**
 * WMS (Warehouse Management System) service client.
 * Used server-side only - never expose to browser.
 */
export class WmsClient extends BaseServiceClient {
     constructor() {
          const baseUrl = process.env.WMS_URL;
          if (!baseUrl) {
               throw new Error("WMS_URL environment variable is not set");
          }
          super(baseUrl);
     }

     /**
      * Get all facilities for the authenticated tenant.
      */
     async getFacilities(accessToken: string): Promise<Facility[]> {
          return this.get<Facility[]>("/facilities", accessToken);
     }

     /**
      * Get a specific facility by ID.
      */
     async getFacility(id: number, accessToken: string): Promise<Facility> {
          return this.get<Facility>(`/facilities/${id}`, accessToken);
     }

     /**
      * Get all waves for the authenticated tenant.
      */
     async getWaves(accessToken: string): Promise<Wave[]> {
          return this.get<Wave[]>("/waves", accessToken);
     }

     /**
      * Get a specific wave by ID.
      */
     async getWave(id: number, accessToken: string): Promise<Wave> {
          return this.get<Wave>(`/waves/${id}`, accessToken);
     }

     /**
      * Create a new wave.
      */
     async createWave(request: CreateWaveRequest, accessToken: string): Promise<Wave> {
          return this.post<Wave, CreateWaveRequest>("/waves", accessToken, request);
     }

     /**
      * Get pick tasks for a wave.
      */
     async getPickTasks(waveId: number, accessToken: string): Promise<PickTask[]> {
          return this.get<PickTask[]>(`/waves/${waveId}/pick-tasks`, accessToken);
     }
}

// Singleton instance
let wmsClient: WmsClient | null = null;

/**
 * Get the WMS client instance.
 */
export function getWmsClient(): WmsClient {
     if (!wmsClient) {
          wmsClient = new WmsClient();
     }
     return wmsClient;
}
