import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  Activity,
  CalendarDays,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Download,
  RefreshCw,
  Search,
  ShieldAlert,
  XCircle,
} from 'lucide-react';

import { AdminHeader } from '../../components/admin/AdminSidebar';
import {
  auditActivityService,
} from '../../services/auditActivityService';

import type {
  AuditActivity,
  AuditActivityFilters,
  AuditActivityPage,
  AuditActivitySummary,
} from '../../services/auditActivityService';

interface OutletCtx {
  onMenuClick: () => void;
}

const PAGE_SIZE = 20;

function getCurrentMonth(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');

  return `${now.getFullYear()}-${month}`;
}

function formatDateTime(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('en-TZ', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'Africa/Dar_es_Salaam',
  }).format(date);
}

function formatDuration(value: number | null): string {
  if (value === null || value === undefined) {
    return '—';
  }

  return `${value.toLocaleString()} ms`;
}

function csvCell(value: unknown): string {
  let text = value === null || value === undefined
    ? ''
    : String(value);

  // Reduce spreadsheet formula-injection risk.
  if (/^[=+\-@\t\r]/.test(text)) {
    text = `'${text}`;
  }

  return `"${text.replace(/"/g, '""')}"`;
}

function downloadCsv(
  activities: AuditActivity[],
  month: string,
): void {
  const headers = [
    'ID',
    'Timestamp',
    'Username',
    'Roles',
    'Module',
    'Action',
    'HTTP Method',
    'Request Path',
    'HTTP Status',
    'Outcome',
    'IP Address',
    'Duration (ms)',
  ];

  const rows = activities.map((item) => [
    item.id,
    item.createdAt,
    item.actorUsername,
    item.actorRoles,
    item.moduleName,
    item.action,
    item.httpMethod,
    item.requestPath,
    item.httpStatus,
    item.outcome,
    item.ipAddress,
    item.durationMs,
  ]);

  const csv = [
    headers,
    ...rows,
  ]
    .map((row) => row.map(csvCell).join(','))
    .join('\r\n');

  const blob = new Blob(['\uFEFF', csv], {
    type: 'text/csv;charset=utf-8;',
  });

  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = url;
  link.download = `rebbystore-activity-audit-${month}.csv`;

  document.body.appendChild(link);
  link.click();
  link.remove();

  URL.revokeObjectURL(url);
}

function OutcomeBadge({
  outcome,
}: {
  outcome: 'SUCCESS' | 'FAILURE';
}) {
  const successful = outcome === 'SUCCESS';

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-1 text-[10px] font-semibold tracking-wide ${
        successful
          ? 'bg-green-50 text-green-700 border border-green-200'
          : 'bg-red-50 text-red-700 border border-red-200'
      }`}
    >
      {successful
        ? <CheckCircle2 size={12} />
        : <XCircle size={12} />}
      {outcome}
    </span>
  );
}

function SummaryCard({
  label,
  value,
  description,
  icon,
  tone = 'default',
}: {
  label: string;
  value: number;
  description: string;
  icon: React.ReactNode;
  tone?: 'default' | 'success' | 'danger';
}) {
  const toneClasses = {
    default: 'text-rs-ink bg-rs-surface',
    success: 'text-green-700 bg-green-50',
    danger: 'text-red-700 bg-red-50',
  };

  return (
    <div className="bg-white border border-rs-border p-4 md:p-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs text-rs-muted uppercase tracking-wide">
            {label}
          </p>

          <p className="mt-2 text-2xl font-display font-bold text-rs-ink">
            {value.toLocaleString()}
          </p>

          <p className="mt-1 text-xs text-rs-muted">
            {description}
          </p>
        </div>

        <div className={`p-2 ${toneClasses[tone]}`}>
          {icon}
        </div>
      </div>
    </div>
  );
}

export default function ActivityAudit() {
  const { onMenuClick } = useOutletContext<OutletCtx>();

  const initialMonth = getCurrentMonth();

  const [monthInput, setMonthInput] = useState(initialMonth);
  const [moduleInput, setModuleInput] = useState('');
  const [outcomeInput, setOutcomeInput] = useState<
    '' | 'SUCCESS' | 'FAILURE'
  >('');
  const [actorInput, setActorInput] = useState('');
  const [searchInput, setSearchInput] = useState('');

  const [filters, setFilters] = useState<AuditActivityFilters>({
    month: initialMonth,
  });

  const [page, setPage] = useState(0);
  const [data, setData] = useState<AuditActivityPage | null>(null);
  const [summary, setSummary] =
    useState<AuditActivitySummary | null>(null);

  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadReport() {
      setLoading(true);
      setError('');

      try {
        const [activityPage, monthlySummary] = await Promise.all([
          auditActivityService.getActivities({
            ...filters,
            page,
            size: PAGE_SIZE,
          }),
          auditActivityService.getMonthlySummary(filters.month),
        ]);

        if (!active) return;

        setData(activityPage);
        setSummary(monthlySummary);
      } catch (requestError) {
        if (!active) return;

        setError(
          requestError instanceof Error
            ? requestError.message
            : 'Unable to load activity audit records.',
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void loadReport();

    return () => {
      active = false;
    };
  }, [filters, page, refreshKey]);

  function applyFilters(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!monthInput) {
      setError('Select a month before generating the report.');
      return;
    }

    setPage(0);

    setFilters({
      month: monthInput,
      moduleName: moduleInput || undefined,
      outcome: outcomeInput || undefined,
      actorUsername: actorInput.trim() || undefined,
      search: searchInput.trim() || undefined,
    });
  }

  async function handleExport() {
    setExporting(true);
    setError('');

    try {
      const activities = await auditActivityService.getAllActivities(
        filters,
      );

      if (activities.length === 0) {
        setError('No activity records are available to export.');
        return;
      }

      downloadCsv(activities, filters.month);
    } catch (exportError) {
      setError(
        exportError instanceof Error
          ? exportError.message
          : 'Unable to export the activity report.',
      );
    } finally {
      setExporting(false);
    }
  }

  return (
    <>
      <AdminHeader
        title="Activity Audit"
        onMenuClick={onMenuClick}
        actions={
          <button
            type="button"
            onClick={() => setRefreshKey((value) => value + 1)}
            disabled={loading}
            className="inline-flex items-center gap-2 border border-rs-border px-3 py-2 text-sm text-rs-muted hover:text-rs-ink disabled:opacity-50"
          >
            <RefreshCw
              size={15}
              className={loading ? 'animate-spin' : ''}
            />
            Refresh
          </button>
        }
      />

      <main className="p-4 md:p-6 space-y-6">
        {/* Report introduction */}
        <section className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-rs-accent">
              <ShieldAlert size={18} />
              <p className="text-xs uppercase tracking-[0.14em] font-semibold">
                System accountability
              </p>
            </div>

            <h2 className="mt-2 text-2xl font-display font-bold text-rs-ink">
              Activity Audit Report
            </h2>

            <p className="mt-1 text-sm text-rs-muted max-w-2xl">
              Review recorded changes across RebbyStore, identify
              failed requests, and export activity for a selected month.
            </p>
          </div>

          <button
            type="button"
            onClick={() => void handleExport()}
            disabled={exporting || loading}
            className="inline-flex shrink-0 items-center justify-center gap-2 bg-rs-ink text-white px-4 py-2.5 text-sm font-medium hover:opacity-90 disabled:opacity-50"
          >
            <Download size={16} />
            {exporting ? 'Preparing CSV…' : 'Export CSV'}
          </button>
        </section>

        {/* Summary cards */}
        <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-3">
          <SummaryCard
            label="Total Activities"
            value={summary?.totalActivities ?? 0}
            description="Recorded this month"
            icon={<Activity size={18} />}
          />

          <SummaryCard
            label="Successful"
            value={summary?.successfulActivities ?? 0}
            description="Requests with status below 400"
            icon={<CheckCircle2 size={18} />}
            tone="success"
          />

          <SummaryCard
            label="Failed"
            value={summary?.failedActivities ?? 0}
            description="Requests with status 400 or higher"
            icon={<XCircle size={18} />}
            tone="danger"
          />

          <SummaryCard
            label="Modules Active"
            value={Object.keys(summary?.activitiesByModule ?? {}).length}
            description="Modules with recorded activity"
            icon={<CalendarDays size={18} />}
          />
        </section>

        {/* Module breakdown */}
        {summary &&
          Object.keys(summary.activitiesByModule).length > 0 && (
            <section className="bg-white border border-rs-border p-4 md:p-5">
              <h3 className="font-display font-semibold text-rs-ink">
                Activity by Module
              </h3>

              <div className="mt-4 flex flex-wrap gap-2">
                {Object.entries(summary.activitiesByModule).map(
                  ([moduleName, count]) => (
                    <div
                      key={moduleName}
                      className="flex items-center gap-3 border border-rs-border px-3 py-2"
                    >
                      <span className="text-xs font-medium text-rs-muted">
                        {moduleName}
                      </span>

                      <span className="text-sm font-semibold text-rs-ink">
                        {count.toLocaleString()}
                      </span>
                    </div>
                  ),
                )}
              </div>
            </section>
          )}

        {/* Filters */}
        <section className="bg-white border border-rs-border p-4 md:p-5">
          <div className="mb-4">
            <h3 className="font-display font-semibold text-rs-ink">
              Filter Activity
            </h3>
            <p className="mt-1 text-xs text-rs-muted">
              Choose a month and narrow the report to the records you need.
            </p>
          </div>

          <form onSubmit={applyFilters}>
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-5 gap-3">
              <label className="block">
                <span className="mb-1.5 block text-xs font-medium text-rs-muted">
                  Reporting month
                </span>
                <input
                  type="month"
                  required
                  value={monthInput}
                  onChange={(event) => setMonthInput(event.target.value)}
                  className="w-full border border-rs-border bg-white px-3 py-2.5 text-sm text-rs-ink outline-none focus:border-rs-accent"
                />
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-medium text-rs-muted">
                  Module
                </span>
                <select
                  value={moduleInput}
                  onChange={(event) => setModuleInput(event.target.value)}
                  className="w-full border border-rs-border bg-white px-3 py-2.5 text-sm text-rs-ink outline-none focus:border-rs-accent"
                >
                  <option value="">All modules</option>
                  <option value="PRODUCTS">Products</option>
                  <option value="CATALOG">Catalog</option>
                  <option value="INVENTORY">Inventory</option>
                  <option value="PURCHASES">Purchases</option>
                  <option value="ORDERS">Orders</option>
                  <option value="CUSTOMERS">Customers</option>
                  <option value="SUPPLIERS">Suppliers</option>
                  <option value="CHECKOUT">Checkout</option>
                  <option value="OTHER">Other</option>
                </select>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-medium text-rs-muted">
                  Outcome
                </span>
                <select
                  value={outcomeInput}
                  onChange={(event) =>
                    setOutcomeInput(
                      event.target.value as '' | 'SUCCESS' | 'FAILURE',
                    )
                  }
                  className="w-full border border-rs-border bg-white px-3 py-2.5 text-sm text-rs-ink outline-none focus:border-rs-accent"
                >
                  <option value="">All outcomes</option>
                  <option value="SUCCESS">Success</option>
                  <option value="FAILURE">Failure</option>
                </select>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-medium text-rs-muted">
                  Username
                </span>
                <input
                  type="text"
                  value={actorInput}
                  onChange={(event) => setActorInput(event.target.value)}
                  placeholder="Search user"
                  className="w-full border border-rs-border bg-white px-3 py-2.5 text-sm text-rs-ink outline-none focus:border-rs-accent"
                />
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-medium text-rs-muted">
                  Action or API path
                </span>
                <input
                  type="search"
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                  placeholder="e.g. purchases"
                  className="w-full border border-rs-border bg-white px-3 py-2.5 text-sm text-rs-ink outline-none focus:border-rs-accent"
                />
              </label>
            </div>

            <div className="mt-4 flex flex-wrap items-center gap-3">
              <button
                type="submit"
                disabled={loading}
                className="inline-flex items-center gap-2 bg-rs-accent px-4 py-2.5 text-sm font-semibold text-white hover:opacity-90 disabled:opacity-50"
              >
                <Search size={15} />
                Apply Filters
              </button>

              <button
                type="button"
                onClick={() => {
                  const month = getCurrentMonth();

                  setMonthInput(month);
                  setModuleInput('');
                  setOutcomeInput('');
                  setActorInput('');
                  setSearchInput('');
                  setPage(0);
                  setFilters({ month });
                }}
                className="px-3 py-2.5 text-sm text-rs-muted hover:text-rs-ink"
              >
                Reset
              </button>
            </div>
          </form>
        </section>

        {/* Errors */}
        {error && (
          <div
            role="alert"
            className="border border-red-200 bg-red-50 p-4 text-sm text-red-700"
          >
            {error}
          </div>
        )}

        {/* Activity table */}
        <section className="bg-white border border-rs-border">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 p-4 md:p-5 border-b border-rs-border">
            <div>
              <h3 className="font-display font-semibold text-rs-ink">
                Recorded Activities
              </h3>
              <p className="mt-1 text-xs text-rs-muted">
                Reporting period: {filters.month}
              </p>
            </div>

            <p className="text-xs text-rs-muted">
              {data
                ? `${data.totalElements.toLocaleString()} records found`
                : 'Loading records…'}
            </p>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full min-w-[1100px] text-left">
              <thead className="bg-rs-surface border-b border-rs-border">
                <tr className="text-[10px] uppercase tracking-wider text-rs-muted">
                  <th className="px-4 py-3 font-semibold">Date / Time</th>
                  <th className="px-4 py-3 font-semibold">Actor</th>
                  <th className="px-4 py-3 font-semibold">Module / Action</th>
                  <th className="px-4 py-3 font-semibold">Request</th>
                  <th className="px-4 py-3 font-semibold">Outcome</th>
                  <th className="px-4 py-3 font-semibold">Duration</th>
                  <th className="px-4 py-3 font-semibold">IP Address</th>
                </tr>
              </thead>

              <tbody className="divide-y divide-rs-border">
                {loading ? (
                  <tr>
                    <td
                      colSpan={7}
                      className="py-14 text-center text-sm text-rs-muted"
                    >
                      Loading activity records…
                    </td>
                  </tr>
                ) : data?.content.length ? (
                  data.content.map((item) => (
                    <tr
                      key={item.id}
                      className="hover:bg-rs-surface/60 transition-colors"
                    >
                      <td className="px-4 py-4 text-xs text-rs-muted whitespace-nowrap">
                        {formatDateTime(item.createdAt)}
                      </td>

                      <td className="px-4 py-4">
                        <p className="text-sm font-medium text-rs-ink">
                          {item.actorUsername || 'Anonymous'}
                        </p>
                        <p className="mt-1 text-[10px] text-rs-muted">
                          {item.actorRoles || 'No authenticated role'}
                        </p>
                      </td>

                      <td className="px-4 py-4">
                        <p className="text-xs font-semibold text-rs-ink">
                          {item.moduleName}
                        </p>
                        <p className="mt-1 text-[10px] text-rs-muted">
                          {item.action}
                        </p>
                      </td>

                      <td className="px-4 py-4 max-w-[320px]">
                        <div className="flex items-start gap-2">
                          <span className="shrink-0 border border-rs-border px-1.5 py-0.5 text-[10px] font-semibold text-rs-muted">
                            {item.httpMethod}
                          </span>
                          <span className="break-all text-xs text-rs-ink">
                            {item.requestPath}
                          </span>
                        </div>
                        <p className="mt-1 text-[10px] text-rs-muted">
                          HTTP {item.httpStatus}
                        </p>
                      </td>

                      <td className="px-4 py-4">
                        <OutcomeBadge outcome={item.outcome} />
                      </td>

                      <td className="px-4 py-4 text-xs text-rs-muted whitespace-nowrap">
                        {formatDuration(item.durationMs)}
                      </td>

                      <td className="px-4 py-4 text-xs text-rs-muted">
                        {item.ipAddress || '—'}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td
                      colSpan={7}
                      className="py-14 text-center"
                    >
                      <Activity
                        size={24}
                        className="mx-auto text-rs-muted/60"
                      />
                      <p className="mt-3 text-sm font-medium text-rs-ink">
                        No activity records found
                      </p>
                      <p className="mt-1 text-xs text-rs-muted">
                        Try another month or adjust your filters.
                      </p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-t border-rs-border px-4 py-4">
            <p className="text-xs text-rs-muted">
              {data && data.totalElements > 0
                ? `Showing ${page * PAGE_SIZE + 1}–${Math.min(
                    (page + 1) * PAGE_SIZE,
                    data.totalElements,
                  )} of ${data.totalElements.toLocaleString()} records`
                : 'No records to display'}
            </p>

            <div className="flex items-center gap-2">
              <button
                type="button"
                disabled={loading || !data || data.first}
                onClick={() => setPage((current) => Math.max(0, current - 1))}
                className="inline-flex items-center gap-1 border border-rs-border px-3 py-2 text-xs text-rs-muted hover:text-rs-ink disabled:opacity-40"
              >
                <ChevronLeft size={14} />
                Previous
              </button>

              <span className="text-xs text-rs-muted">
                Page {data ? data.number + 1 : 1} of{' '}
                {Math.max(data?.totalPages ?? 0, 1)}
              </span>

              <button
                type="button"
                disabled={loading || !data || data.last || data.empty}
                onClick={() => setPage((current) => current + 1)}
                className="inline-flex items-center gap-1 border border-rs-border px-3 py-2 text-xs text-rs-muted hover:text-rs-ink disabled:opacity-40"
              >
                Next
                <ChevronRight size={14} />
              </button>
            </div>
          </div>
        </section>

      </main>
    </>
  );
}
