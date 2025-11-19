import React, { ReactNode } from 'react'
import clsx from 'clsx'

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  title?: string
  description?: string
  clickable?: boolean
  onClick?: () => void
  children?: ReactNode
  variant?: 'default' | 'elevated' | 'outlined'
}

const Card = ({
  title,
  description,
  clickable = false,
  onClick,
  children,
  variant = 'default',
  className,
  ...props
}: CardProps) => {
  const baseStyles = 'rounded-2xl transition-all duration-300'

  const variantStyles = {
    default: 'bg-glass-light backdrop-blur-md border border-glass-border shadow-glass hover:shadow-glass-hover',
    elevated: 'bg-white shadow-lg hover:shadow-xl',
    outlined: 'bg-transparent border-2 border-primary hover:border-secondary',
  }

  const interactiveStyles = clickable && 'cursor-pointer hover:scale-105'

  return (
    <div
      className={clsx(
        baseStyles,
        variantStyles[variant],
        interactiveStyles,
        'p-6',
        className
      )}
      onClick={onClick}
      role={clickable ? 'button' : undefined}
      tabIndex={clickable ? 0 : undefined}
      onKeyDown={(e) => {
        if (clickable && (e.key === 'Enter' || e.key === ' ')) {
          onClick?.()
        }
      }}
      {...props}
    >
      {(title || description) && (
        <div className="mb-4">
          {title && (
            <h3 className="text-lg font-semibold text-text-primary mb-1">
              {title}
            </h3>
          )}
          {description && (
            <p className="text-sm text-text-secondary">
              {description}
            </p>
          )}
        </div>
      )}
      {children}
    </div>
  )
}

export default Card
