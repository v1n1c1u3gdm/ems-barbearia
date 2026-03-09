import { useState } from 'react';
import { Globe, Apple, MessageCircle } from 'lucide-react';
import { register, login, requestOtp, verifyOtp, getGoogleOAuthUrl, getAppleOAuthUrl } from './api';
import { setPublicToken } from './auth';
type Tab = 'choose' | 'register' | 'login' | 'phone';

type Props = { onAuthenticated: () => void };

export function AgendarAuthGate({ onAuthenticated }: Props) {
  const [tab, setTab] = useState<Tab>('choose');
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [telefone, setTelefone] = useState('');
  const [code, setCode] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [otpSent, setOtpSent] = useState(false);

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    if (senha !== confirmarSenha) {
      setError('As senhas não coincidem. Digite a mesma senha nos dois campos.');
      return;
    }
    setLoading(true);
    try {
      const { token } = await register({ nome, email, senha });
      setPublicToken(token);
      onAuthenticated();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao cadastrar');
    } finally {
      setLoading(false);
    }
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const { token } = await login({ email, senha });
      setPublicToken(token);
      onAuthenticated();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao entrar');
    } finally {
      setLoading(false);
    }
  }

  async function handleRequestOtp(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await requestOtp({ telefone });
      setOtpSent(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao enviar código');
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyOtp(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const { token } = await verifyOtp({ telefone, code });
      setPublicToken(token);
      onAuthenticated();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Código inválido');
    } finally {
      setLoading(false);
    }
  }

  const inputClass =
    'w-full rounded-md border border-zinc-600 bg-zinc-900 px-3 py-2 text-zinc-100 focus:border-amber-500 focus:outline-none focus:ring-1 focus:ring-amber-500';
  const btnClass =
    'rounded-md bg-amber-500 px-4 py-2 font-medium text-zinc-950 hover:bg-amber-400 disabled:opacity-50';

  return (
    <div className="mx-auto max-w-md rounded-xl border border-zinc-700 bg-zinc-900/80 p-6">
      <h2 className="mb-4 text-xl font-semibold text-zinc-100">Entrar ou cadastrar</h2>
      {error && (
        <div className="mb-4 rounded-lg border border-red-500/50 bg-red-500/10 px-3 py-2 text-sm text-red-400">
          {error}
        </div>
      )}

      {tab === 'choose' && (
        <div className="flex flex-col gap-3">
          <a
            href={getGoogleOAuthUrl()}
            className="flex items-center justify-center gap-2 rounded-md border border-zinc-600 bg-zinc-800 px-4 py-3 text-zinc-200 hover:bg-zinc-700"
          >
            <Globe className="size-5 shrink-0" aria-hidden />
            Continuar com Google
          </a>
          <a
            href={getAppleOAuthUrl()}
            className="flex items-center justify-center gap-2 rounded-md border border-zinc-600 bg-zinc-800 px-4 py-3 text-zinc-200 hover:bg-zinc-700"
          >
            <Apple className="size-5 shrink-0" aria-hidden />
            Continuar com Apple
          </a>
          <button
            type="button"
            onClick={() => setTab('phone')}
            className="flex items-center justify-center gap-2 rounded-md border border-zinc-600 bg-zinc-800 px-4 py-3 text-zinc-200 hover:bg-zinc-700"
          >
            <MessageCircle className="size-5 shrink-0" aria-hidden />
            Continuar com telefone / WhatsApp
          </button>
          <div className="my-2 border-t border-zinc-700" />
          <button
            type="button"
            onClick={() => setTab('register')}
            className="text-sm text-amber-400 hover:underline"
          >
            Criar conta com email
          </button>
          <button
            type="button"
            onClick={() => setTab('login')}
            className="text-sm text-zinc-400 hover:underline"
          >
            Já tenho conta
          </button>
        </div>
      )}

      {tab === 'register' && (
        <form onSubmit={handleRegister} className="flex flex-col gap-3">
          <input
            type="text"
            placeholder="Nome"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            className={inputClass}
            required
          />
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={inputClass}
            required
          />
          <input
            type="password"
            placeholder="Senha (mín. 6 caracteres)"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            className={inputClass}
            required
            minLength={6}
          />
          <input
            type="password"
            placeholder="Confirmar senha"
            value={confirmarSenha}
            onChange={(e) => setConfirmarSenha(e.target.value)}
            className={inputClass}
            required
            minLength={6}
          />
          <div className="flex gap-2">
            <button type="submit" disabled={loading} className={btnClass}>
              {loading ? 'Cadastrando...' : 'Cadastrar'}
            </button>
            <button type="button" onClick={() => setTab('choose')} className="text-zinc-400 hover:underline">
              Voltar
            </button>
          </div>
        </form>
      )}

      {tab === 'login' && (
        <form onSubmit={handleLogin} className="flex flex-col gap-3">
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={inputClass}
            required
          />
          <input
            type="password"
            placeholder="Senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            className={inputClass}
            required
          />
          <div className="flex gap-2">
            <button type="submit" disabled={loading} className={btnClass}>
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
            <button type="button" onClick={() => setTab('choose')} className="text-zinc-400 hover:underline">
              Voltar
            </button>
          </div>
        </form>
      )}

      {tab === 'phone' && (
        <div className="flex flex-col gap-3">
          {!otpSent ? (
            <form onSubmit={handleRequestOtp}>
              <input
                type="tel"
                placeholder="Telefone (ex: 11999999999)"
                value={telefone}
                onChange={(e) => setTelefone(e.target.value)}
                className={inputClass}
                required
              />
              <div className="mt-2 flex gap-2">
                <button type="submit" disabled={loading} className={btnClass}>
                  {loading ? 'Enviando...' : 'Enviar código'}
                </button>
                <button type="button" onClick={() => setTab('choose')} className="text-zinc-400 hover:underline">
                  Voltar
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp}>
              <input
                type="text"
                inputMode="numeric"
                placeholder="Código recebido"
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 8))}
                className={inputClass}
                required
              />
              <div className="mt-2 flex gap-2">
                <button type="submit" disabled={loading} className={btnClass}>
                  {loading ? 'Verificando...' : 'Confirmar'}
                </button>
                <button
                  type="button"
                  onClick={() => { setOtpSent(false); setCode(''); }}
                  className="text-zinc-400 hover:underline"
                >
                  Reenviar código
                </button>
              </div>
            </form>
          )}
        </div>
      )}
    </div>
  );
}
