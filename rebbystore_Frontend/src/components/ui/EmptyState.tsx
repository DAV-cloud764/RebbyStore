import { Button } from './Button';

interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: { label: string; onClick: () => void };
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-20 px-6 text-center">
      {icon && <div className="text-rs-border mb-4">{icon}</div>}
      <h3 className="text-lg font-display font-semibold text-rs-ink mb-2">{title}</h3>
      {description && <p className="text-sm text-rs-muted max-w-sm mb-6">{description}</p>}
      {action && (
        <Button onClick={action.onClick} size="sm">
          {action.label}
        </Button>
      )}
    </div>
  );
}
