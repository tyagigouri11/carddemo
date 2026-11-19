import React from 'react'

interface Column<T> {
  key: keyof T
  label: string
  render?: (value: unknown, row: T) => React.ReactNode
}

interface TableProps<T> {
  columns: Column<T>[]
  data: T[]
  loading?: boolean
  empty?: boolean
  emptyMessage?: string
  className?: string
}

export function Table<T extends { id?: string | number }>({
  columns,
  data,
  loading = false,
  empty = data.length === 0,
  emptyMessage = 'No data available',
  className = '',
}: TableProps<T>) {
  if (loading) {
    return (
      <div className="flex justify-center items-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600" />
      </div>
    )
  }

  if (empty) {
    return (
      <div className="text-center py-8 text-neutral-500 dark:text-neutral-400">
        <p>{emptyMessage}</p>
      </div>
    )
  }

  return (
    <div className={`overflow-x-auto ${className}`}>
      <table className="w-full">
        <thead>
          <tr className="border-b border-neutral-200 dark:border-neutral-700 bg-neutral-50 dark:bg-neutral-800">
            {columns.map((col) => (
              <th key={String(col.key)} className="px-4 py-3 text-left text-sm font-semibold text-neutral-900 dark:text-neutral-100">
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, idx) => (
            <tr key={row.id || idx} className="border-b border-neutral-200 dark:border-neutral-700 hover:bg-neutral-50 dark:hover:bg-neutral-700 transition-colors">
              {columns.map((col) => (
                <td key={String(col.key)} className="px-4 py-3 text-sm text-neutral-700 dark:text-neutral-300">
                  {col.render ? col.render(row[col.key], row) : String(row[col.key] || '-')}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
