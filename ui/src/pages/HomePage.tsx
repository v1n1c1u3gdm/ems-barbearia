import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <div className="max-w-2xl mx-auto text-center py-12">
      <h1 className="text-4xl font-bold text-gray-900 mb-4">Hello, World!</h1>
      <p className="text-gray-600 mb-8">
        EMS Barbearia — React + Vite + Tailwind. API em <code className="bg-gray-200 px-1 rounded">/api</code>.
      </p>
      <Link
        to="/examples"
        className="inline-block px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
      >
        Ver Examples (API)
      </Link>
    </div>
  );
}
