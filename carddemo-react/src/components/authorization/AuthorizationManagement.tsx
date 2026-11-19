import React, { useState } from 'react'
import { Card, CardBody, CardHeader } from '@/components/common/Card'
import { MainLayout } from '@/components/layout/MainLayout'
import { AuthorizationNav, AuthorizationView } from './AuthorizationNav'
import { AuthorizationForm } from '@/components/forms/AuthorizationForm'
import { AuthorizationList } from './AuthorizationList'
import { AuthorizationReports } from './AuthorizationReports'
import { AuthorizationDetail } from './AuthorizationDetail'
import { Authorization } from '@/types'

export const AuthorizationManagement: React.FC = () => {
  const [currentView, setCurrentView] = useState<AuthorizationView>('list')
  const [selectedAuthorization, setSelectedAuthorization] = useState<Authorization | null>(null)
  const [showDetail, setShowDetail] = useState(false)

  const handleSelectAuthorization = (auth: Authorization) => {
    setSelectedAuthorization(auth)
    setShowDetail(true)
  }

  const handleCloseDetail = () => {
    setShowDetail(false)
    setSelectedAuthorization(null)
  }

  return (
    <MainLayout title="Authorization Management">
      <div className="space-y-6">
        {/* Navigation */}
        <Card>
          <CardBody>
            <AuthorizationNav activeView={currentView} onViewChange={setCurrentView} />
          </CardBody>
        </Card>

        {/* Content Area */}
        <div>
          {currentView === 'create' && (
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Create New Authorization</h2>
              </CardHeader>
              <CardBody>
                <AuthorizationForm
                  onSuccess={() => {
                    // Optionally switch to list view after successful creation
                    setCurrentView('list')
                  }}
                />
              </CardBody>
            </Card>
          )}

          {currentView === 'list' && (
            <AuthorizationList onSelectAuthorization={handleSelectAuthorization} />
          )}

          {currentView === 'reports' && (
            <AuthorizationReports />
          )}
        </div>
      </div>

      {/* Detail Modal */}
      {showDetail && selectedAuthorization && (
        <AuthorizationDetail
          authorization={selectedAuthorization}
          onClose={handleCloseDetail}
          onSuccess={() => {
            // List will be refreshed automatically
          }}
        />
      )}
    </MainLayout>
  )
}
