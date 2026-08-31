
interface PageContainerProps {
  children: React.ReactNode;
  className?: string;
}

export function PageContainer({ children, className = '' }: PageContainerProps) {
  return (
    <div className={`page-container py-8 md:py-12 ${className}`}>
      {children}
    </div>
  );
}
