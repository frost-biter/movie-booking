import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Film, MapPin, Home, User, Calendar, Plus } from 'lucide-react';

const AdminDashboard: React.FC<{ onLogout: () => void }> = ({ onLogout }) => {
  const [activeTab, setActiveTab] = useState('movies');
  const navigate = useNavigate();

  const tabs = [
    { id: 'movies', name: 'Movies', icon: Film },
    { id: 'theatres', name: 'Theatres', icon: Home },
    { id: 'shows', name: 'Shows', icon: Calendar },
    { id: 'cities', name: 'Cities', icon: MapPin },
    { id: 'users', name: 'Users', icon: User },
  ];

  const renderContent = () => {
    switch (activeTab) {
      case 'movies':
        return (
          <div className="bg-gray-800 p-6 rounded-lg">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-white">Movies</h2>
              <button
                onClick={() => navigate('/admin/movies/new')}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2"
              >
                <Plus size={16} />
                Add Movie
              </button>
            </div>
            <div className="bg-gray-900 rounded-lg p-4 text-gray-400 text-center">
              Movie list will be displayed here
            </div>
          </div>
        );
      case 'theatres':
        return (
          <div className="bg-gray-800 p-6 rounded-lg">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-white">Theatres</h2>
              <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2">
                <Plus size={16} />
                Add Theatre
              </button>
            </div>
            <div className="bg-gray-900 rounded-lg p-4 text-gray-400 text-center">
              Theatre list will be displayed here
            </div>
          </div>
        );
      case 'shows':
        return (
          <div className="bg-gray-800 p-6 rounded-lg">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-white">Shows</h2>
              <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2">
                <Plus size={16} />
                Add Show
              </button>
            </div>
            <div className="bg-gray-900 rounded-lg p-4 text-gray-400 text-center">
              Show schedule will be displayed here
            </div>
          </div>
        );
      case 'cities':
        return (
          <div className="bg-gray-800 p-6 rounded-lg">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-white">Cities</h2>
              <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2">
                <Plus size={16} />
                Add City
              </button>
            </div>
            <div className="bg-gray-900 rounded-lg p-4 text-gray-400 text-center">
              City list will be displayed here
            </div>
          </div>
        );
      case 'users':
        return (
          <div className="bg-gray-800 p-6 rounded-lg">
            <h2 className="text-xl font-semibold text-white mb-6">Users</h2>
            <div className="bg-gray-900 rounded-lg p-4 text-gray-400 text-center">
              User management will be displayed here
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div className="md:col-span-1">
        <div className="bg-gray-800 rounded-lg p-4 sticky top-6">
          <h3 className="text-lg font-semibold text-white mb-4 px-2">Admin Panel</h3>
          <nav className="space-y-1">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-left transition-colors ${
                    activeTab === tab.id
                      ? 'bg-blue-600 text-white'
                      : 'text-gray-300 hover:bg-gray-700'
                  }`}
                >
                  <Icon size={18} />
                  <span>{tab.name}</span>
                </button>
              );
            })}
            <button
              onClick={onLogout}
              className="w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-left text-gray-300 hover:bg-red-600 hover:text-white transition-colors mt-4"
            >
              <span>Logout</span>
            </button>
          </nav>
        </div>
      </div>
      <div className="md:col-span-3">
        {renderContent()}
      </div>
    </div>
  );
};

export default AdminDashboard;
