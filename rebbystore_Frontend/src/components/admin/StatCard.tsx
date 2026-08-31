
interface StatCardProps {
  label: string;
  value: string | number;
  sub?: string;
  icon?: React.ReactNode;
  trend?: 'up' | 'down' | 'neutral';
  accent?: boolean;
}

export function StatCard({ label, value, sub, icon, accent }: StatCardProps) {
  return (
    <div className={`p-5 border flex flex-col gap-3 ${accent ? 'bg-rs-ink border-rs-ink text-white' : 'bg-white border-rs-border'}`}>
      <div className="flex items-start justify-between">
        <p className={`text-xs tracking-wider uppercase font-medium ${accent ? 'text-white/60' : 'text-rs-muted'}`}>{label}</p>
        {icon && <span className={accent ? 'text-rs-accent' : 'text-rs-accent'}>{icon}</span>}
      </div>
      <div>
        <p className={`text-3xl font-display font-bold ${accent ? 'text-white' : 'text-rs-ink'}`}>{value}</p>
        {sub && <p className={`text-xs mt-1 ${accent ? 'text-white/50' : 'text-rs-muted'}`}>{sub}</p>}
      </div>
    </div>
  );
}
