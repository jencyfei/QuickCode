import React, { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import clsx from 'clsx'

interface SidebarItem {
  icon: string
  label: string
  href: string
}

const Sidebar = () => {
  const [collapsed, setCollapsed] = useState(false)
  const location = useLocation()

  const menuItems: SidebarItem[] = [
    { icon: '📊', label: 'Dashboard', href: '/dashboard' },
    { icon: '📋', label: 'Templates', href: '/templates' },
    { icon: '📤', label: 'Upload', href: '/upload' },
    { icon: '📈', label: 'Results', href: '/results' },
  ]

  const isActive = (href: string) => {
    return location.pathname.startsWith(href)
  }

  return (
    <aside
      className={clsx(
        'bg-glass-light backdrop-blur-md border-r border-glass-border',
        'transition-all duration-300 ease-in-out',
        'hidden md:flex flex-col',
        collapsed ? 'w-20' : 'w-64'
      )}
    >
      {/* Collapse Button */}
      <div className="p-4 flex justify-end">
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-2 rounded-lg hover:bg-glass-border transition-colors"
          title={collapsed ? 'Expand' : 'Collapse'}
        >
          <svg
            className={clsx(
              'w-5 h-5 transition-transform',
              collapsed && 'rotate-180'
            )}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
        </button>
      </div>

      {/* Menu Items */}
      <nav className="flex-1 px-2 space-y-2">
        {menuItems.map((item) => (
          <Link
            key={item.href}
            to={item.href}
            className={clsx(
              'flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200',
              'hover:bg-glass-border',
              isActive(item.href)
                ? 'bg-primary text-white shadow-lg'
                : 'text-text-secondary hover:text-text-primary'
            )}
            title={collapsed ? item.label : undefined}
          >
            <span className="text-xl flex-shrink-0">{item.icon}</span>
            {!collapsed && (
              <span className="font-medium text-sm">{item.label}</span>
            )}
          </Link>
        ))}
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-glass-border">
        <button
          className={clsx(
            'w-full flex items-center gap-3 px-4 py-3 rounded-lg',
            'text-text-secondary hover:text-text-primary',
            'hover:bg-glass-border transition-colors'
          )}
          title={collapsed ? 'Settings' : undefined}
        >
          <span className="text-xl flex-shrink-0">⚙️</span>
          {!collapsed && (
            <span className="font-medium text-sm">Settings</span>
          )}
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
