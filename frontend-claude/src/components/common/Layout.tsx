import React, { ReactNode } from 'react';
import { Home, LogOut, MapPin } from 'lucide-react';

interface LayoutProps {
  children: ReactNode;
  title: string;
  showHeader?: boolean;
  onHomeClick?: () => void;
  onLogout?: () => void;
  city?: string;
  onCityChange?: () => void;
  showBackButton?: boolean;
  onBackClick?: () => void;
  extraHeaderContent?: ReactNode;
}

const Layout: React.FC<LayoutProps> = ({
  children,
  title,
  showHeader = true,
  onHomeClick,
  onLogout,
  city,
  onCityChange,
  showBackButton = false,
  onBackClick,
  extraHeaderContent,
}) => {
  return (
    <div className="min-h-screen bg-gray-900">
      {showHeader && (
        <header className="bg-gray-800 shadow-lg">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center py-4">
              <div className="flex items-center space-x-4">
                {showBackButton && onBackClick && (
                  <button
                    onClick={onBackClick}
                    className="text-blue-500 hover:text-blue-400 flex items-center"
                  >
                    <span className="mr-1">←</span> Back
                  </button>
                )}
                <h1 className="text-xl md:text-2xl font-bold text-white">{title}</h1>
              </div>
              
              <div className="flex items-center space-x-4">
                {extraHeaderContent}
                
                {city && onCityChange && (
                  <button
                    onClick={onCityChange}
                    className="flex items-center space-x-1 bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-lg text-sm"
                  >
                    <MapPin className="w-4 h-4" />
                    <span>{city}</span>
                  </button>
                )}
                
                {onHomeClick && (
                  <button
                    onClick={onHomeClick}
                    className="p-2 text-gray-400 hover:text-white rounded-full hover:bg-gray-700"
                    title="Home"
                  >
                    <Home className="w-5 h-5" />
                  </button>
                )}
                
                {onLogout && (
                  <button
                    onClick={onLogout}
                    className="p-2 text-gray-400 hover:text-white rounded-full hover:bg-gray-700"
                    title="Logout"
                  >
                    <LogOut className="w-5 h-5" />
                  </button>
                )}
              </div>
            </div>
          </div>
        </header>
      )}
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {children}
      </main>
    </div>
  );
};

export default Layout;
