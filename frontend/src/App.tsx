import { Routes, Route } from 'react-router-dom'
import Layout from '@components/layout/Layout'
import Dashboard from '@pages/dashboard/Dashboard'
import Settings from '@pages/settings/Settings'
import NotFound from '@pages/common/NotFound'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="settings" element={<Settings />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}

export default App
