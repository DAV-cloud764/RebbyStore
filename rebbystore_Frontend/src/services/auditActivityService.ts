import { apiClient } from './apiClient';

export interface AuditActivity {
  id: number;
  actorUsername: string | null;
  actorRoles: string | null;
  moduleName: string;
  action: string;
  httpMethod: string;
  requestPath: string;
  httpStatus: number;
  outcome: 'SUCCESS' | 'FAILURE';
  ipAddress: string | null;
  durationMs: number | null;
  createdAt: string;
}

export interface AuditActivitySummary {
  month: string;
  totalActivities: number;
  successfulActivities: number;
  failedActivities: number;
  activitiesByModule: Record<string, number>;
}

export interface AuditActivityPage {
  content: AuditActivity[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface AuditActivityFilters {
  month: string;
  moduleName?: string;
  outcome?: '' | 'SUCCESS' | 'FAILURE';
  actorUsername?: string;
  search?: string;
  page?: number;
  size?: number;
}

function buildQuery(filters: AuditActivityFilters): string {
  const params = new URLSearchParams();

  params.set('month', filters.month);
  params.set('page', String(filters.page ?? 0));
  params.set('size', String(filters.size ?? 20));

  if (filters.moduleName) {
    params.set('moduleName', filters.moduleName);
  }

  if (filters.outcome) {
    params.set('outcome', filters.outcome);
  }

  if (filters.actorUsername?.trim()) {
    params.set('actorUsername', filters.actorUsername.trim());
  }

  if (filters.search?.trim()) {
    params.set('search', filters.search.trim());
  }

  return params.toString();
}

export const auditActivityService = {
  getActivities(
    filters: AuditActivityFilters,
  ): Promise<AuditActivityPage> {
    return apiClient<AuditActivityPage>(
      `/api/reporting/activities?${buildQuery(filters)}`,
    );
  },

  getMonthlySummary(
    month: string,
  ): Promise<AuditActivitySummary> {
    return apiClient<AuditActivitySummary>(
      `/api/reporting/activities/summary?month=${encodeURIComponent(month)}`,
    );
  },

  async getAllActivities(
    filters: AuditActivityFilters,
  ): Promise<AuditActivity[]> {
    const pageSize = 100;

    const firstPage = await this.getActivities({
      ...filters,
      page: 0,
      size: pageSize,
    });

    const activities = [...firstPage.content];

    for (
      let page = 1;
      page < firstPage.totalPages;
      page += 1
    ) {
      const result = await this.getActivities({
        ...filters,
        page,
        size: pageSize,
      });

      activities.push(...result.content);
    }

    return activities;
  },
};
