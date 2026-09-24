import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { hasAuthToken } from "../../services/apiClient";

interface ProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({
  children,
}: ProtectedRouteProps) {
  const location = useLocation();

  if (!hasAuthToken()) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location }}
      />
    );
  }

  return <>{children}</>;
}