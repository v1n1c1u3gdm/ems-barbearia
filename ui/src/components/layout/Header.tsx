import { Link } from 'react-router-dom';

export function Header() {
  return (
    <header className="bg-white shadow-sm border-b">
      <nav className="container mx-auto px-4 py-4 flex gap-6">
        <Link to="/" className="text-lg font-semibold text-gray-800 hover:text-gray-600">
          EMS Barbearia
        </Link>
        <Link to="/examples" className="text-gray-600 hover:text-gray-800">
          Examples
        </Link>
      </nav>
    </header>
  );
}
