import { useState } from "react";
import type { FormEvent } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { login } from "../../services/authService";

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const from =
    (location.state as { from?: { pathname?: string } } | null)?.from
      ?.pathname || "/admin";

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      await login({
        identifier: identifier.trim(),
        password,
      });

      navigate(from, { replace: true });
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Invalid username/email or password.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#1C1817] p-4 md:p-6">
      <div className="mx-auto flex min-h-[calc(100vh-2rem)] max-w-7xl overflow-hidden rounded-[30px] bg-[#F5EFE6] shadow-[0_10px_50px_rgba(0,0,0,0.35)] md:min-h-[calc(100vh-3rem)]">
        {/* LEFT BRAND PANEL */}
        <aside className="relative hidden w-[34%] overflow-hidden bg-[#1B1918] lg:flex lg:flex-col lg:justify-between">
          {/* Decorative shapes */}
          <div className="absolute -right-12 -top-8 h-32 w-32 rounded-[30px] bg-[#C5A059]" />
          <div className="absolute right-8 top-16 h-12 w-12 rounded-full bg-[#CBA368]" />
          <div className="absolute -left-10 top-24 h-24 w-24 rounded-full bg-[#C5A059]" />
          <div className="absolute -right-10 top-[46%] h-10 w-10 rounded-full bg-[#CBA368]" />
          <div className="absolute -bottom-24 -left-20 h-72 w-[130%] rounded-[50%_50%_0_0] bg-[#2A2522]" />

          {/* Brand */}
          <div className="relative z-10 p-10">
            <div className="flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#C5A059] text-[#1C1817] shadow-sm">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-6 w-6">
                  <path d="M4 19V9" />
                  <path d="M10 19V5" />
                  <path d="M16 19V11" />
                  <path d="M22 19V2" />
                </svg>
              </div>
              <span className="text-xl font-bold text-[#F5EFE6]">RebbyStore</span>
            </div>
          </div>

          {/* Marketing text */}
          <div className="relative z-10 px-10 pb-28">
            <h2 className="max-w-xs text-5xl font-extrabold leading-[0.98] tracking-tight text-[#F5EFE6]">
              Get Started
              <br />
              With Your
              <br />
              <span className="text-[#C5A059]">Account</span>
            </h2>
            <p className="mt-7 max-w-xs text-base text-[#CFC5B7]">
              Manage your products, inventory, orders and customers from one place.
            </p>
          </div>

          {/* Bottom text */}
          <div className="relative z-10 px-10 pb-8">
            <p className="text-sm text-[#AFA399]">RebbyStore Administration</p>
            <p className="mt-1 text-sm font-semibold text-[#CBA368]">
              Smart management. Better business.
            </p>
          </div>
        </aside>

        {/* RIGHT LOGIN PANEL */}
        <main className="flex flex-1 items-center justify-center px-5 py-10 sm:px-10 md:px-16">
          <div className="w-full max-w-xl">
            <div className="mb-10 text-center">
              <h1 className="text-4xl font-extrabold tracking-tight text-[#1C1817] sm:text-5xl">
                Welcome Back
              </h1>
              <p className="mt-3 text-base text-[#6F655D] sm:text-lg">
                Log in to your account to continue
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* IDENTIFIER */}
              <div>
                <label htmlFor="identifier" className="mb-2 block text-sm font-semibold text-[#1C1817]">
                  Username or Email
                </label>

                <div className="relative">
                  <span className="pointer-events-none absolute inset-y-0 left-4 flex items-center text-[#8C8177]">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
                      <path d="M4 6.5A2.5 2.5 0 0 1 6.5 4h11A2.5 2.5 0 0 1 20 6.5v11a2.5 2.5 0 0 1-2.5 2.5h-11A2.5 2.5 0 0 1 4 17.5v-11Z" />
                      <path d="m5 5 7 6 7-6" />
                    </svg>
                  </span>

                  <input
                    id="identifier"
                    type="text"
                    value={identifier}
                    onChange={(event) => setIdentifier(event.target.value)}
                    placeholder="Enter your username or email"
                    autoComplete="username"
                    required
                    className="h-16 w-full rounded-2xl border border-[#C9BDB0] bg-[#FCF8F2] pl-14 pr-4 text-base text-[#1C1817] outline-none transition focus:border-[#C5A059] focus:ring-4 focus:ring-[#C5A059]/20"
                  />
                </div>
              </div>

              {/* PASSWORD */}
              <div>
                <label htmlFor="password" className="mb-2 block text-sm font-semibold text-[#1C1817]">
                  Password
                </label>

                <div className="relative">
                  <span className="pointer-events-none absolute inset-y-0 left-4 flex items-center text-[#8C8177]">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
                      <rect x="5" y="10" width="14" height="10" rx="2" />
                      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
                    </svg>
                  </span>

                  <input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    placeholder="Enter your password"
                    autoComplete="current-password"
                    required
                    className="h-16 w-full rounded-2xl border border-[#C9BDB0] bg-[#FCF8F2] pl-14 pr-14 text-base text-[#1C1817] outline-none transition focus:border-[#C5A059] focus:ring-4 focus:ring-[#C5A059]/20"
                  />

                  <button
                    type="button"
                    onClick={() => setShowPassword((value) => !value)}
                    className="absolute inset-y-0 right-4 flex items-center text-[#8C8177] transition hover:text-[#1C1817]"
                    aria-label={showPassword ? "Hide password" : "Show password"}
                  >
                    {showPassword ? (
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-5 w-5">
                        <path d="M3 3l18 18" />
                        <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" />
                        <path d="M9.9 4.2A11.5 11.5 0 0 1 12 4c5 0 8.5 4 10 8-0.6 1.7-1.7 3.4-3.2 4.7" />
                        <path d="M6.2 6.2C4.5 7.4 3.3 9.5 2 12c1.5 4 5 8 10 8 1.1 0 2.2-.2 3.2-.6" />
                      </svg>
                    ) : (
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-5 w-5">
                        <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" />
                        <circle cx="12" cy="12" r="2.5" />
                      </svg>
                    )}
                  </button>
                </div>
              </div>

              {/* OPTIONS */}
              <div className="flex items-center justify-between gap-4">
                <label className="flex cursor-pointer items-center gap-3 text-sm text-[#4F4740]">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(event) => setRememberMe(event.target.checked)}
                    className="h-5 w-5 rounded border-[#C9BDB0] accent-[#C5A059]"
                  />
                  Remember me
                </label>

                <button
                  type="button"
                  className="text-sm font-semibold text-[#9D7E48] hover:text-[#7D6338]"
                  onClick={() => {
                    // Password reset will be implemented separately.
                  }}
                >
                  Forgot password?
                </button>
              </div>

              {/* ERROR */}
              {error && (
                <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                  {error}
                </div>
              )}

              {/* LOGIN */}
              <button
                type="submit"
                disabled={loading}
                className="h-16 w-full rounded-2xl bg-[#C5A059] text-lg font-bold text-[#1C1817] shadow-md shadow-[#C5A059]/20 transition hover:bg-[#B58F4E] hover:shadow-lg hover:shadow-[#C5A059]/25 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {loading ? "Signing in..." : "Log in"}
              </button>
            </form>

            <p className="mt-8 text-center text-sm text-[#7D7269]">
              Authorized RebbyStore staff only
            </p>
          </div>
        </main>
      </div>
    </div>
  );
}
