import { CheckCircle, XCircle, Info, X } from 'lucide-react';
import { useToast } from '../../contexts/ToastContext';

export function ToastContainer() {
  const { toasts, removeToast } = useToast();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2" role="alert" aria-live="polite">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`flex items-center gap-3 px-4 py-3 min-w-[260px] max-w-sm shadow-lg text-sm font-medium transition-all duration-300 ${
            toast.type === 'success' ? 'bg-rs-ink text-white' :
            toast.type === 'error' ? 'bg-red-600 text-white' :
            'bg-rs-surface text-rs-ink border border-rs-border'
          }`}
        >
          {toast.type === 'success' && <CheckCircle size={16} className="shrink-0" />}
          {toast.type === 'error' && <XCircle size={16} className="shrink-0" />}
          {toast.type === 'info' && <Info size={16} className="shrink-0" />}
          <span className="flex-1">{toast.message}</span>
          <button onClick={() => removeToast(toast.id)} className="ml-2 opacity-70 hover:opacity-100" aria-label="Dismiss">
            <X size={14} />
          </button>
        </div>
      ))}
    </div>
  );
}
