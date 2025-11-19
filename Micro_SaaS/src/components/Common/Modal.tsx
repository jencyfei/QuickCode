import React, { ReactNode, useEffect } from 'react'
import clsx from 'clsx'
import Button from './Button'

interface ModalProps {
  title: string
  isOpen: boolean
  onClose: () => void
  onConfirm?: () => void
  children: ReactNode
  confirmText?: string
  cancelText?: string
  size?: 'sm' | 'md' | 'lg'
  showFooter?: boolean
}

const Modal = ({
  title,
  isOpen,
  onClose,
  onConfirm,
  children,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  size = 'md',
  showFooter = true,
}: ModalProps) => {
  // 防止背景滚动
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = 'unset'
    }
    return () => {
      document.body.style.overflow = 'unset'
    }
  }, [isOpen])

  if (!isOpen) return null

  const sizeStyles = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
  }

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose()
    }
  }

  return (
    <div
      className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50 p-4 animate-fadeIn"
      onClick={handleBackdropClick}
    >
      <div
        className={clsx(
          'bg-white rounded-2xl shadow-2xl',
          'border border-glass-border',
          'backdrop-blur-md',
          'animate-slideUp',
          sizeStyles[size],
          'w-full'
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-glass-border">
          <h2 className="text-xl font-semibold text-text-primary">
            {title}
          </h2>
          <button
            onClick={onClose}
            className="text-text-secondary hover:text-text-primary transition-colors"
            aria-label="Close modal"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          {children}
        </div>

        {/* Footer */}
        {showFooter && (
          <div className="flex gap-3 p-6 border-t border-glass-border justify-end">
            <Button
              variant="ghost"
              onClick={onClose}
            >
              {cancelText}
            </Button>
            {onConfirm && (
              <Button
                variant="primary"
                onClick={onConfirm}
              >
                {confirmText}
              </Button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default Modal
