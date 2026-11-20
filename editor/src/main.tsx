import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './editor/App.tsx'
// oxlint-disable-next-line import/no-unassigned-import -- global stylesheet
import './editor/styles.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
