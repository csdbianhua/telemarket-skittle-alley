import HeaderAsideLayout from './layouts/HeaderAsideLayout';
import Dashboard from './pages/Dashboard';
import Drawguess from './pages/Drawguess';
import NotFound from './pages/NotFound';

const routerConfig = [
  {
    path: '/games',
    layout: HeaderAsideLayout,
    component: Dashboard,
    children: [
      {
        path: '/games/draw_guess',
        layout: HeaderAsideLayout,
        component: Drawguess,
      },
    ],
  },
  {
    path: '*',
    layout: HeaderAsideLayout,
    component: NotFound,
  },
];

export default routerConfig;
